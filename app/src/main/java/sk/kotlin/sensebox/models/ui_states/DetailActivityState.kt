package sk.kotlin.sensebox.models.ui_states

sealed class DetailActivityState {

    data class Error(val message: String?) : DetailActivityState()
    object Success : DetailActivityState()

}