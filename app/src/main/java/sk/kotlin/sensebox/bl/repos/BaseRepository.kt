package sk.kotlin.sensebox.bl.repos

import android.bluetooth.BluetoothProfile
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import sk.kotlin.sensebox.bl.bt.BleClient

abstract class BaseRepository(protected val bleClient: BleClient) {

    fun onBleDisconnected(): Observable<Int> {
        return bleClient.onConnectionStateChanged()
                .filter { it == BluetoothProfile.STATE_DISCONNECTED }
                .subscribeOn(Schedulers.newThread())
    }


}