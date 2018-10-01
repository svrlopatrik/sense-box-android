package sk.kotlin.sensebox.models.states

sealed class DetailActivityState {

    data class Error(val message: String?) : DetailActivityState()
    object Success : DetailActivityState()

}