package sk.kotlin.sensebox.models.states

/**
 * Created by Patrik Švrlo on 9.9.2018.
 */
sealed class MainActivityState {
    object BleConnecting : MainActivityState()
    object BleConnected : MainActivityState()
    object BleDisconnecting : MainActivityState()
    object BleDisconnected : MainActivityState()
    data class Error(val message: String?) : MainActivityState()
}