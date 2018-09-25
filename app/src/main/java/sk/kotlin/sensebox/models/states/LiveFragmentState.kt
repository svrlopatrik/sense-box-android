package sk.kotlin.sensebox.models.states

/**
 * Created by Patrik Švrlo on 9.9.2018.
 */
sealed class LiveFragmentState {

    data class Timestamp(var millis: Long) : LiveFragmentState()
    data class Temperature(var value: Float) : LiveFragmentState()
    data class Humidity(var value: Float) : LiveFragmentState()

    data class Error(val message: String) : LiveFragmentState()
    object Refresh : LiveFragmentState()
}