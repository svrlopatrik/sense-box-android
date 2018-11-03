package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.LiveData
import android.os.Bundle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import sk.kotlin.sensebox.bl.bt.BleClient
import sk.kotlin.sensebox.bl.bt.BleResult
import sk.kotlin.sensebox.bl.repos.ActualRepository
import sk.kotlin.sensebox.events.BleConnectionEvent
import sk.kotlin.sensebox.events.BleFailEvent
import sk.kotlin.sensebox.events.RxBus
import sk.kotlin.sensebox.events.SettingsChangedEvent
import sk.kotlin.sensebox.models.ActualModel
import sk.kotlin.sensebox.utils.SingleLiveEvent
import sk.kotlin.sensebox.utils.runOnUiThread
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class ActualFragmentViewModel @Inject constructor(
        private val actualRepository: ActualRepository,
        private val bleClient: BleClient,
        private val rxBus: RxBus
) : BaseViewModel() {

    private val actualModel = SingleLiveEvent<ActualModel>()

    private val isReading = SingleLiveEvent<Boolean>()
    private val refresh = SingleLiveEvent<Unit>()

    private var refreshActualDisposable: Disposable? = null

    override fun onViewCreated(savedInstanceState: Bundle?) {
        if (!isInitialized) {
            addDisposable(actualRepository.onBleDisconnected().subscribe { disposeActualDataRefreshing() })

            addDisposable(rxBus.ofType(SettingsChangedEvent::class.java)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { refresh.value = Unit }
            )

            addDisposable(actualRepository.getActualData()
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            { actualModel.value = it },
                            { Timber.e(it, "Error get actual data.") }
                    )
            )

            isInitialized = true
        }
    }

    fun refreshActualData() {
        Timber.i("Refresh actual data.")

        disposeActualDataRefreshing()

        refreshActualDisposable = actualRepository.refreshActualData()
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
                                    Timber.e("Failure refresh actual data - ${it.bleFailType.name}")
                                    rxBus.post(BleFailEvent(it.bleFailType.name))
                                    if (!bleClient.isConnected()) {
                                        rxBus.post(BleConnectionEvent(false))
                                    }
                                }
                                else -> {
                                    Timber.i("Response refresh actual data - ${it::class.java.simpleName}")
                                }
                            }
                        },
                        { Timber.e(it, "Error refresh actual data") }
                ).also { addDisposable(it) }
    }

    private fun disposeActualDataRefreshing() {
        refreshActualDisposable?.let {
            removeDisposable(it)
            refreshActualDisposable = null
        }
    }

    fun getActualModel(): LiveData<ActualModel> = actualModel

    fun getIsReading(): LiveData<Boolean> = isReading
    fun getRefresh(): LiveData<Unit> = refresh
}