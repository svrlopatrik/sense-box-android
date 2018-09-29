package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.LiveData
import android.bluetooth.BluetoothProfile
import android.os.Bundle
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import sk.kotlin.sensebox.Constants
import sk.kotlin.sensebox.bl.PreferencesManager
import sk.kotlin.sensebox.bl.bt.BleClient
import sk.kotlin.sensebox.bl.bt.BleResult
import sk.kotlin.sensebox.bl.db.daos.FileDao
import sk.kotlin.sensebox.bl.db.daos.RecordDao
import sk.kotlin.sensebox.bl.db.entities.File
import sk.kotlin.sensebox.bl.db.entities.Record
import sk.kotlin.sensebox.events.BleConnectionEvent
import sk.kotlin.sensebox.events.BleFailEvent
import sk.kotlin.sensebox.events.RxBus
import sk.kotlin.sensebox.events.SettingsChangedEvent
import sk.kotlin.sensebox.models.states.HistoryFragmentState
import sk.kotlin.sensebox.utils.SingleLiveEvent
import sk.kotlin.sensebox.utils.ValueInterpreter
import timber.log.Timber
import java.util.*
import javax.inject.Inject

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class HistoryFragmentViewModel @Inject constructor(
        private val bleClient: BleClient,
        private val fileDao: FileDao,
        private val recordDao: RecordDao,
        private val rxBus: RxBus
) : BaseViewModel() {
    private val historyFragmentState = SingleLiveEvent<HistoryFragmentState>()
    private val isReading = SingleLiveEvent<Boolean>()

    private var refreshHistoryListDisposable: Disposable? = null
    private var downloadHistoryDisposable: Disposable? = null

    private var loadedFiles = mutableListOf<File>()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            addDisposable(bleClient.connectionState()
                    .filter { it == BluetoothProfile.STATE_DISCONNECTED }
                    .subscribeOn(Schedulers.newThread())
                    .subscribe { disposeHistoryDataRefreshing() }
            )

            addDisposable(loadFilesFromDb()
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(
                            {
                                loadedFiles.addAll(it)
                                historyFragmentState.postValue(HistoryFragmentState.LocalList(it))
                            },
                            {
                                Timber.e(it, "Error load files from DB.")
                                println("onError: ${it.message}")
                            }
                    )
            )

            addDisposable(rxBus.ofType(SettingsChangedEvent::class.java).subscribe {
                historyFragmentState.postValue(HistoryFragmentState.Refresh)
            })
        } else {
            historyFragmentState.postValue(HistoryFragmentState.LocalList(loadedFiles))
        }
    }

    private fun loadFilesFromDb(): Single<List<File>> {
        return fileDao.getAll()
                .flatMapIterable { it }
                .map { file ->
                    if (PreferencesManager.getByteValue(PreferencesManager.PreferenceKey.TEMPERATURE_UNIT) == Constants.UNIT_FLAG_TEMPERATURE_FAHRENHEIT) {
                        file.apply {
                            averageTemperature?.let {
                                averageTemperature = ValueInterpreter.celsiusToFahrenheit(it)
                            }
                        }
                    }
                    file
                }
                .toList()
    }

    fun refreshHistoryListData() {
        Timber.i("Refresh history list data.")

        disposeHistoryDataRefreshing()

        refreshHistoryListDisposable = bleClient.connect(Constants.BLE_DEVICE_MAC)
                .flatMap {
                    when (it) {
                        is BleResult.Connected -> bleClient.writeCharacteristic(Constants.BLE_UUID_SERVICE, Constants.BLE_UUID_CHARACTERISTIC, Constants.REQUEST_CODE_LIST)
                        else -> Single.fromCallable {
                            rxBus.post(BleConnectionEvent(false))
                            it
                        }
                    }
                }
                .toFlowable()
                .flatMap {
                    when (it) {
                        BleResult.CharacteristicsWritten -> bleClient.notifyCharacteristics(Constants.BLE_UUID_SERVICE, Constants.BLE_UUID_CHARACTERISTIC, Constants.BLE_UUID_DESCRIPTOR)
                        else -> Flowable.fromCallable { it }
                    }
                }
                .flatMapMaybe {
                    when (it) {
                        is BleResult.Success -> processHistoryListData(it)
                        is BleResult.Failure -> {
                            historyFragmentState.postValue(HistoryFragmentState.Error(it.bleFailState.name))
                            rxBus.post(BleFailEvent(it.bleFailState.name))
                            Maybe.empty()
                        }
                        else -> Maybe.empty()
                    }
                }
                .toList()
                .flatMap { loadFilesFromDb() }
                .doOnSubscribe {
                    if (!bleClient.isConnected()) {
                        rxBus.post(BleConnectionEvent(true))
                    }
                    isReading.postValue(true)
                }
                .doFinally { isReading.postValue(false) }
                .subscribeOn(Schedulers.newThread())
                .subscribe(
                        {
                            if (it.isNotEmpty()) {
                                loadedFiles.clear()
                                loadedFiles.addAll(it)
                                historyFragmentState.postValue(HistoryFragmentState.NewList(it))
                            }
                        },
                        {
                            Timber.e(it, "Error load history list.")
                            println("onError: ${it.message}")
                        }
                )
                .also {
                    addDisposable(it)
                }
    }

    private fun processHistoryListData(success: BleResult.Success): Maybe<File> {
        val fileName = ValueInterpreter.byteArrayToInt(success.data.copyOfRange(0, 4)).toString()
        val fileSize = ValueInterpreter.byteArrayToInt(success.data.copyOfRange(4, 8))
        val fileDate = ValueInterpreter.rawDateToCalendar(fileName.toInt())

        return fileDao.getById(fileName)
                .switchIfEmpty(Maybe.fromCallable { File() })
                .flatMap { file ->
                    if (file.id != fileName) {    // new
                        val newFile = File(fileSize, 0, fileDate.get(Calendar.DAY_OF_MONTH),
                                fileDate.get(Calendar.MONTH), fileDate.get(Calendar.YEAR),
                                null, null,
                                false, true)
                                .apply { id = fileName }

                        fileDao.insertSingle(newFile)
                        Maybe.fromCallable { newFile }
                    } else {
                        if (file.size != fileSize) { // updated
                            val updatedFile = file.apply {
                                size = fileSize
                                isUpdated = false
                            }
                            fileDao.update(updatedFile)
                            Maybe.fromCallable { updatedFile }
                        } else {    // unchanged
                            Maybe.empty()
                        }
                    }
                }
    }

    private fun processHistoryData(file: File, bleResults: List<BleResult>): Completable {
        var timestamp = 0
        var temperature = 0f
        var humidity = 0f

        if (bleResults.size == 3) {  //check if all data been received
            for (item in bleResults) {       //check if all data are of correct type
                if (item is BleResult.Success) {
                    when (item.data[0]) {
                        Constants.RESPONSE_FLAG_TIMESTAMP -> timestamp = ValueInterpreter.byteArrayToInt(item.data.copyOfRange(1, 5))
                        Constants.RESPONSE_FLAG_TEMPERATURE -> temperature = ValueInterpreter.byteArrayToFloat(item.data.copyOfRange(1, 5))
                        Constants.RESPONSE_FLAG_HUMIDITY -> humidity = ValueInterpreter.byteArrayToFloat(item.data.copyOfRange(1, 5))
                    }
                } else {
                    return Completable.complete()
                }
            }

            val calendar = ValueInterpreter.unixTimestampToCalendar(timestamp)
            val record = Record(file.id, temperature, humidity, calendar.get(Calendar.SECOND), calendar.get(Calendar.MINUTE), calendar.get(Calendar.HOUR_OF_DAY)).apply { id = timestamp.toString() }
            recordDao.insertSingle(record)
        }

        return Completable.complete()
    }

    fun downloadHistoryData(file: File) {
        Timber.i("Download history data.")
        disposeDownloadHistory()

        //create characteristic data in form: flag + date in byte array
        val dateInBytes = ValueInterpreter.intToByteArray(file.id.toInt())
        val characteristicData = ByteArray(dateInBytes.size + 1).apply { set(0, Constants.REQUEST_CODE_HISTORY) }
        System.arraycopy(dateInBytes, 0, characteristicData, 1, dateInBytes.size)

        downloadHistoryDisposable = bleClient.connect(Constants.BLE_DEVICE_MAC)
                .flatMap {
                    when (it) {
                        is BleResult.Connected -> bleClient.writeCharacteristic(Constants.BLE_UUID_SERVICE, Constants.BLE_UUID_CHARACTERISTIC, *characteristicData)
                        else -> Single.fromCallable {
                            rxBus.post(BleConnectionEvent(false))
                            it
                        }
                    }
                }
                .toFlowable()
                .flatMap {
                    when (it) {
                        BleResult.CharacteristicsWritten -> bleClient.notifyCharacteristics(Constants.BLE_UUID_SERVICE, Constants.BLE_UUID_CHARACTERISTIC, Constants.BLE_UUID_DESCRIPTOR)
                        else -> Flowable.fromCallable { it }
                    }
                }
                .buffer(3)
                .flatMapCompletable {
                    if (it.isNotEmpty()) {
                        val firstResult = it[0]
                        when (firstResult) {
                            is BleResult.Success -> processHistoryData(file, it)
                            is BleResult.Failure -> {
                                historyFragmentState.postValue(HistoryFragmentState.Error(firstResult.bleFailState.name))
                                rxBus.post(BleFailEvent(firstResult.bleFailState.name))
                                Completable.complete()
                            }
                            else -> Completable.complete()
                        }
                    } else {
                        Completable.complete()
                    }
                }
                .doOnSubscribe {
                    if (!bleClient.isConnected()) {
                        rxBus.post(BleConnectionEvent(true))
                    }
                    isReading.postValue(true)
                }
                .doFinally { isReading.postValue(false) }
                .subscribeOn(Schedulers.newThread())
                .subscribe(
                        { historyFragmentState.postValue(HistoryFragmentState.HistoryDownloaded(file)) },
                        {
                            Timber.e(it, "Error download history data.")
                            println("onError: ${it.message}")
                        }
                )
                .also { addDisposable(it) }
    }

    private fun disposeHistoryDataRefreshing() {
        refreshHistoryListDisposable?.let {
            removeDisposable(it)
            refreshHistoryListDisposable = null
        }
    }

    private fun disposeDownloadHistory() {
        downloadHistoryDisposable?.let {
            removeDisposable(it)
            downloadHistoryDisposable = null
        }
    }

    fun getHistoryFragmentState(): LiveData<HistoryFragmentState> = historyFragmentState

    fun getIsReading(): LiveData<Boolean> = isReading
}