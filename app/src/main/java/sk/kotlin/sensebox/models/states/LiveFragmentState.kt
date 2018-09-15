package sk.kotlin.sensebox.models.states

/**
 * Created by Patrik Švrlo on 9.9.2018.
 */
sealed class LiveFragmentState {

    data class Timestamp(var millis: Long, var format: String) : LiveFragmentState()
    data class Temperature(var value: Float, var unit: String) : LiveFragmentState()
    data class Humidity(var value: Float, var unit: String) : LiveFragmentState()

    data class Error(val message: String) : LiveFragmentState()
}