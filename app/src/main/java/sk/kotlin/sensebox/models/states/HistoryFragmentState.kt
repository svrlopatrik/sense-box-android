package sk.kotlin.sensebox.models.states

import sk.kotlin.sensebox.bl.db.entities.File

/**
 * Created by Patrik Švrlo on 16.9.2018.
 */
sealed class HistoryFragmentState {
    data class LocalData(val data: List<File>) : HistoryFragmentState()
    data class NewData(val data: List<File>) : HistoryFragmentState()
    data class Error(val message: String) : HistoryFragmentState()
    object Refresh : HistoryFragmentState()
}