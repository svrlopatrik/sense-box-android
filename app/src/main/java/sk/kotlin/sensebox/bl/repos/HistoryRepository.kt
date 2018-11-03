package sk.kotlin.sensebox.bl.repos

import android.arch.paging.PagedList
import android.arch.paging.RxPagedListBuilder
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.Function
import sk.kotlin.sensebox.Constants
import sk.kotlin.sensebox.bl.bt.BleClient
import sk.kotlin.sensebox.bl.bt.BleResult
import sk.kotlin.sensebox.bl.db.daos.FileDao
import sk.kotlin.sensebox.bl.db.daos.RecordDao
import sk.kotlin.sensebox.bl.db.entities.File
import sk.kotlin.sensebox.bl.db.entities.Record
import sk.kotlin.sensebox.models.ActualModel
import sk.kotlin.sensebox.models.HistoryModel
import sk.kotlin.sensebox.utils.BufferUntil
import sk.kotlin.sensebox.utils.ValueInterpreter
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class HistoryRepository(
        bleClient: BleClient,
        private val fileDao: FileDao,
        private val recordDao: RecordDao
) : BaseRepository(bleClient) {

    @Volatile
    private lateinit var requestedFile: File

    fun refreshHistoryList(): Single<BleResult> {
        return bleClient.sendCommand(Constants.BLE_DEVICE_MAC,
                Constants.BLE_UUID_SERVICE,
                Constants.BLE_UUID_CHARACTERISTIC,
                Constants.BLE_UUID_DESCRIPTOR,
                Constants.REQUEST_CODE_LIST
        )
    }

    fun getHistoryList(): Flowable<HistoryModel> {
        return Flowable.merge(pagedDbFiles(), handleReceivedFiles())
    }

    private fun pagedDbFiles(): Flowable<HistoryModel> {
        val pagingConfig = PagedList.Config.Builder()
                .setPageSize(20)
                .setInitialLoadSizeHint(40)
                .setPrefetchDistance(10)
                .setEnablePlaceholders(true)
                .build()

        return RxPagedListBuilder(fileDao.getAllPaged(), pagingConfig)
                .buildFlowable(BackpressureStrategy.BUFFER)
                .flatMap {
                    Flowable.fromCallable { HistoryModel.List(it) }
                }
    }

    private fun handleReceivedFiles(): Flowable<HistoryModel> {
        return bleClient.onDataReceived()
                .filter { it.requestCode == Constants.REQUEST_CODE_LIST }
                .flatMap { Flowable.fromCallable { it.data } }
                .lift(BufferUntil<ByteArray, HistoryModel.Item>(1,
                        Function { it.contentEquals(Constants.RESPONSE_FLAG_END) || it.contentEquals(Constants.RESPONSE_FLAG_UDEF) },
                        Function { parseHistoryList(it.first()) }
                ))
                .flatMap {
                    processHistoryList(it).toFlowable()
                }

    }

    private fun parseHistoryList(data: ByteArray): HistoryModel.Item {
        val fileName = ValueInterpreter.byteArrayToInt(data.copyOfRange(0, 4)).toString()
        val fileSize = ValueInterpreter.byteArrayToInt(data.copyOfRange(4, 8))

        return HistoryModel.Item(fileName, fileSize)
    }


    private fun processHistoryList(list: List<HistoryModel.Item>): Maybe<HistoryModel> {
        return fileDao.getAll()
                .flatMapMaybe {
                    val indexedResult = HashMap<String, File>()
                    val insertList = ArrayList<File>()
                    val updateList = ArrayList<File>()

                    //index results for quicker search
                    it.forEach { indexedResult[it.id] = it }

                    for (item in list) {
                        if (indexedResult.containsKey(item.name)) {
                            //file exists
                            val indexedFile = indexedResult[item.name]!!

                            if (indexedFile.size != item.size) {
                                //needs update
                                updateList.add(indexedFile.apply {
                                    size = item.size
                                    countAll = item.size / Constants.SINGLE_RECORD_SIZE
                                    isUpdated = false
                                })
                            }
                        } else {
                            //new file
                            val fileDate = ValueInterpreter.rawDateToCalendar(item.name.toInt())
                            insertList.add(File(item.size, item.size / Constants.SINGLE_RECORD_SIZE,
                                    0, fileDate.get(Calendar.DAY_OF_MONTH),
                                    fileDate.get(Calendar.MONTH), fileDate.get(Calendar.YEAR),
                                    null, null,
                                    false, true).apply { id = item.name }
                            )
                        }
                    }

                    fileDao.insertAndUpdate(insertList, updateList)

                    Maybe.fromCallable { HistoryModel.ListDownloaded }
                }
    }

    fun refreshHistoryMeasurements(file: File): Single<BleResult> {
        requestedFile = file

        //create characteristic data in form: flag + date in byte array
        val dateInBytes = ValueInterpreter.intToByteArray(file.id.toInt())
        val characteristicData = ByteArray(dateInBytes.size + 1).apply { set(0, Constants.REQUEST_CODE_HISTORY) }
        System.arraycopy(dateInBytes, 0, characteristicData, 1, dateInBytes.size)

        return bleClient.sendCommand(Constants.BLE_DEVICE_MAC,
                Constants.BLE_UUID_SERVICE,
                Constants.BLE_UUID_CHARACTERISTIC,
                Constants.BLE_UUID_DESCRIPTOR,
                *characteristicData
        )
    }

    fun getHistoryMeasurements(): Flowable<HistoryModel> {
        return bleClient.onDataReceived()
                .filter { it.requestCode == Constants.REQUEST_CODE_HISTORY }
                .flatMap { Flowable.fromCallable { it.data } }
                .lift(BufferUntil<ByteArray, ActualModel>(3,
                        Function { it.contentEquals(Constants.RESPONSE_FLAG_END) || it.contentEquals(Constants.RESPONSE_FLAG_UDEF) },
                        Function { parseHistoryMeasurements(it) }
                ))
                .flatMapMaybe {
                    processHistoryMeasurements(requestedFile, it)
                }
                .flatMapSingle { updateFileProperties(requestedFile) }
                .flatMap { Flowable.fromCallable { HistoryModel.MeasurementsDownloaded(requestedFile) } }
    }

    private fun parseHistoryMeasurements(data: List<ByteArray>): ActualModel {
        val actualModel = ActualModel()
        for (bytes in data) {
            when (bytes[0]) {
                Constants.RESPONSE_FLAG_TIMESTAMP -> actualModel.timestamp = ValueInterpreter.unixTimestampToMillis(ValueInterpreter.byteArrayToInt(bytes.copyOfRange(1, 5)))
                Constants.RESPONSE_FLAG_TEMPERATURE -> actualModel.temperature = ValueInterpreter.byteArrayToFloat(bytes.copyOfRange(1, 5))
                Constants.RESPONSE_FLAG_HUMIDITY -> actualModel.humidity = ValueInterpreter.byteArrayToFloat(bytes.copyOfRange(1, 5))
            }
        }

        return actualModel
    }

    private fun processHistoryMeasurements(file: File, data: List<ActualModel>): Maybe<Unit> {
        val recordsList = ArrayList<Record>()

        data.forEach {
            val recordDateCalendar = ValueInterpreter.rtcMillisToCalendar(it.timestamp!!)

            recordsList.add(Record(file.id, it.temperature!!, it.humidity!!, recordDateCalendar.get(Calendar.SECOND),
                    recordDateCalendar.get(Calendar.MINUTE), recordDateCalendar.get(Calendar.HOUR_OF_DAY)).apply { id = it.timestamp.toString() }
            )
        }

        recordDao.insert(recordsList)
        updateFileProperties(file)

        return Maybe.fromCallable { Unit }
    }

    private fun updateFileProperties(file: File): Single<Int> {
        return recordDao.getFileProperties(file.id)
                .flatMap {
                    Single.fromCallable {
                        fileDao.update(file.apply {
                            countDownloaded = it.count
                            averageTemperature = it.averageTemperature
                            averageHumidity = it.averageHumidity
                            isDownloaded = true
                            isUpdated = true
                        })
                    }
                }
    }
}