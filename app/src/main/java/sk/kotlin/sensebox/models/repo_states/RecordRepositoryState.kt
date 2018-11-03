package sk.kotlin.sensebox.models.repo_states

sealed class RecordRepositoryState {

    data class Timestamp(val timestamp: Long) : RecordRepositoryState()
    data class Temperature(val temperature: Float) : RecordRepositoryState()
    data class Humidity(val humidity: Float) : RecordRepositoryState()

    object EndOfResponse : RecordRepositoryState()
    object UndefinedResponse : RecordRepositoryState()

}