package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.LiveData
import android.bluetooth.BluetoothProfile
import android.os.Bundle
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import sk.kotlin.sensebox.bl.bt.BleClient
import sk.kotlin.sensebox.events.BleConnectionEvent
import sk.kotlin.sensebox.events.BleFailEvent
import sk.kotlin.sensebox.events.RxBus
import sk.kotlin.sensebox.models.ui_states.MainActivityState
import sk.kotlin.sensebox.utils.SingleLiveEvent
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class MainActivityViewModel @Inject constructor(
        private val bleClient: BleClient,
        private val rxBus: RxBus
) : BaseViewModel() {

    private val mainActivityState = SingleLiveEvent<MainActivityState>()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        if (!isInitialized) {
            addDisposable(bleClient.onConnectionStateChanged()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                            {
                                mainActivityState.value = when (it) {
                                    BluetoothProfile.STATE_CONNECTING -> {
                                        Timber.i("Connection state: connecting.")
                                        MainActivityState.BleConnecting
                                    }
                                    BluetoothProfile.STATE_CONNECTED -> {
                                        Timber.i("Connection state: connected.")
                                        MainActivityState.BleConnected
                                    }
                                    BluetoothProfile.STATE_DISCONNECTING -> {
                                        Timber.i("Connection state: disconnecting.")
                                        MainActivityState.BleDisconnecting
                                    }
                                    BluetoothProfile.STATE_DISCONNECTED -> {
                                        Timber.i("Connection state: disconnected.")
                                        MainActivityState.BleDisconnected
                                    }
                                    else -> throw Exception("unknown state")
                                }
                            },
                            { mainActivityState.value = MainActivityState.Error(it.message) }
                    )
            )

            addDisposable(rxBus.ofType(BleConnectionEvent::class.java)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Timber.i("Received connection event, connecting: ${it.isConnecting}")
                        when (it.isConnecting) {
                            true -> mainActivityState.value = MainActivityState.BleConnecting
                            false -> mainActivityState.value = MainActivityState.BleDisconnected
                        }
                    }
            )

            addDisposable(rxBus.ofType(BleFailEvent::class.java)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        Timber.i("Received fail event, message: ${it.message}")
                        mainActivityState.value = MainActivityState.Error(it.message)
                    }
            )

            isInitialized = true
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleClient.disconnect()
        bleClient.releaseConnection()
    }

    fun getMainActivityState(): LiveData<MainActivityState> = mainActivityState
}