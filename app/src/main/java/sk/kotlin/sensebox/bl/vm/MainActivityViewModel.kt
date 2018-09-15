package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.bluetooth.BluetoothProfile
import android.os.Bundle
import io.reactivex.schedulers.Schedulers
import sk.kotlin.sensebox.bl.bt.BleClient
import sk.kotlin.sensebox.models.states.MainActivityState
import javax.inject.Inject

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class MainActivityViewModel @Inject constructor(
        private val bleClient: BleClient
) : BaseViewModel() {

    private val mainActivityState = MutableLiveData<MainActivityState>()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            addDisposable(bleClient.connectionState()
                    .subscribeOn(Schedulers.newThread())
                    .subscribe(
                            {
                                mainActivityState.postValue(when (it) {
                                    BluetoothProfile.STATE_CONNECTING -> MainActivityState.BleConnecting
                                    BluetoothProfile.STATE_CONNECTED -> MainActivityState.BleConnected
                                    BluetoothProfile.STATE_DISCONNECTING -> MainActivityState.BleDisconnecting
                                    BluetoothProfile.STATE_DISCONNECTED -> MainActivityState.BleDisconnected
                                    else -> MainActivityState.Error(Throwable("Unknown ble state"))
                                })
                            },
                            { mainActivityState.postValue(MainActivityState.Error(it)) }
                    )
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        bleClient.disconnect()
        bleClient.releaseConnection()
    }

    fun getMainActivityState(): LiveData<MainActivityState> = mainActivityState
}