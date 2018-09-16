package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.LiveData
import android.bluetooth.BluetoothProfile
import android.os.Bundle
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import sk.kotlin.sensebox.Constants
import sk.kotlin.sensebox.bl.bt.BleClient
import sk.kotlin.sensebox.bl.bt.BleResult
import sk.kotlin.sensebox.bl.db.daos.FileDao
import sk.kotlin.sensebox.bl.db.entities.File
import sk.kotlin.sensebox.events.BleConnectionEvent
import sk.kotlin.sensebox.events.RxBus
import sk.kotlin.sensebox.models.states.HistoryFragmentState
import sk.kotlin.sensebox.utils.SingleLiveEvent
import sk.kotlin.sensebox.utils.ValueInterpreter
import java.util.*
import javax.inject.Inject

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class HistoryFragmentViewModel @Inject constructor(
        private val bleClient: BleClient,
        private val fileDao: FileDao,
        private val rxBus: RxBus
) : BaseViewModel() {
    private val historyFragmentState = SingleLiveEvent<HistoryFragmentState>()

    private val isReading = SingleLiveEvent<Boolean>()

    private var refreshHistoryListDisposable: Disposable? = null

    override fun onViewCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            addDisposable(bleClient.connectionState()
                    .filter { it == BluetoothProfile.STATE_DISCONNECTED }
                    .subscribeOn(Schedulers.newThread())
                    .subscribe { disposeHistoryDataRefreshing() }
            )
        }

        addDisposable(
                fileDao.getAll()
                        .subscribeOn(Schedulers.newThread())
                        .subscribe { historyFragmentState.postValue(HistoryFragmentState.LocalData(it)) }
        )
    }

    fun refreshHistoryListData() {
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
                        is BleResult.Success -> processData(it)
                        is BleResult.Failure -> {
                            historyFragmentState.postValue(HistoryFragmentState.Error(it.bleFailState.name))
                            Maybe.empty()
                        }
                        else -> Maybe.empty()
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
                        { historyFragmentState.postValue(HistoryFragmentState.New(it)) },
                        { println("onError: ${it.message}") },
                        { println("onComplete") }
                )
                .also {
                    addDisposable(it)
                }
    }

    private fun processData(success: BleResult.Success): Maybe<File> {
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

    private fun disposeHistoryDataRefreshing() {
        refreshHistoryListDisposable?.let {
            removeDisposable(it)
            refreshHistoryListDisposable = null
        }
    }

    fun getHistoryFragmentState(): LiveData<HistoryFragmentState> = historyFragmentState

    fun getIsReading(): LiveData<Boolean> = isReading
}