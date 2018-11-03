package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.paging.PagedList
import android.os.Bundle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import sk.kotlin.sensebox.bl.bt.BleClient
import sk.kotlin.sensebox.bl.bt.BleResult
import sk.kotlin.sensebox.bl.db.entities.File
import sk.kotlin.sensebox.bl.repos.HistoryRepository
import sk.kotlin.sensebox.events.BleConnectionEvent
import sk.kotlin.sensebox.events.BleFailEvent
import sk.kotlin.sensebox.events.RxBus
import sk.kotlin.sensebox.events.SettingsChangedEvent
import sk.kotlin.sensebox.models.HistoryModel
import sk.kotlin.sensebox.models.ui_states.HistoryFragmentState
import sk.kotlin.sensebox.utils.SingleLiveEvent
import sk.kotlin.sensebox.utils.runOnUiThread
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class HistoryFragmentViewModel @Inject constructor(
        private val historyRepository: HistoryRepository,
        private val bleClient: BleClient,
        private val rxBus: RxBus
) : BaseViewModel() {

    private val historyFragmentState = SingleLiveEvent<HistoryFragmentState>()
    private val isReading = SingleLiveEvent<Boolean>()
    private val historyList = MutableLiveData<PagedList<File>>()

    private var refreshHistoryListDisposable: Disposable? = null
    private var downloadHistoryDisposable: Disposable? = null

    override fun onViewCreated(savedInstanceState: Bundle?) {
        if (!isInitialized) {
            addDisposable(historyRepository.onBleDisconnected()
                    .subscribe {
                        disposeHistoryDataRefreshing()
                        disposeDownloadHistory()
                    }
            )

            addDisposable(rxBus.ofType(SettingsChangedEvent::class.java)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { historyFragmentState.value = HistoryFragmentState.Refresh }
            )

            addDisposable(historyRepository.getHistoryList()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                when (it) {
                                    is HistoryModel.List -> historyList.value = it.data
                                    is HistoryModel.ListDownloaded -> Timber.i("Get history list - success")
                                }
                            },
                            { Timber.e(it, "Error get history list.") }
                    )
            )

            addDisposable(historyRepository.getHistoryMeasurements()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                when (it) {
                                    is HistoryModel.MeasurementsDownloaded -> historyFragmentState.value = HistoryFragmentState.HistoryDownloaded(it.file)
                                }
                            },
                            { Timber.e(it, "Error getting record.") }
                    )
            )

            isInitialized = true
        }
    }

    fun refreshHistoryListData() {
        Timber.i("Refresh history list data.")

        disposeHistoryDataRefreshing()

        refreshHistoryListDisposable = historyRepository.refreshHistoryList()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    if (!bleClient.isConnected()) {
                        rxBus.post(BleConnectionEvent(true))
                    }
                    runOnUiThread { isReading.value = true }
                }
                .doFinally { runOnUiThread { isReading.value = false } }
                .subscribe(
                        {
                            when (it) {
                                is BleResult.Failure -> {
                                    Timber.e("Failure refresh history list - ${it.bleFailType.name}")
                                    historyFragmentState.value = HistoryFragmentState.Error(it.bleFailType.name)
                                    rxBus.post(BleFailEvent(it.bleFailType.name))
                                    if (!bleClient.isConnected()) {
                                        rxBus.post(BleConnectionEvent(false))
                                    }
                                }
                                else -> {
                                    Timber.i("Response refresh history list - ${it::class.java.simpleName}")
                                }
                            }
                        },
                        { Timber.e(it, "Error refresh history list.") }
                ).also { addDisposable(it) }
    }

    fun downloadHistoryData(file: File) {
        Timber.i("Download history data.")
        disposeDownloadHistory()

        downloadHistoryDisposable = historyRepository.refreshHistoryMeasurements(file)
                .doOnSubscribe {
                    if (!bleClient.isConnected()) {
                        rxBus.post(BleConnectionEvent(true))
                    }
                    runOnUiThread { isReading.value = true }
                }
                .doFinally { runOnUiThread { isReading.value = false } }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            when (it) {
                                is BleResult.Failure -> {
                                    Timber.e("Failure download history data - ${it.bleFailType.name}")
                                    historyFragmentState.value = HistoryFragmentState.Error(it.bleFailType.name)
                                    rxBus.post(BleFailEvent(it.bleFailType.name))
                                    if (!bleClient.isConnected()) {
                                        rxBus.post(BleConnectionEvent(false))
                                    }
                                }
                                else -> {
                                    Timber.i("Response download history data - ${it::class.java.simpleName}")
                                }
                            }
                        },
                        { Timber.e(it, "Error download history data.") }
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
    fun getHistoryList(): LiveData<PagedList<File>> = historyList
}