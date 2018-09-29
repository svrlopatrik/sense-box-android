package sk.kotlin.sensebox.models.states

import sk.kotlin.sensebox.bl.db.entities.File

/**
 * Created by Patrik Švrlo on 16.9.2018.
 */
sealed class HistoryFragmentState {
    data class LocalList(val data: List<File>) : HistoryFragmentState()
    data class NewList(val data: List<File>) : HistoryFragmentState()
    data class Error(val message: String) : HistoryFragmentState()
    data class HistoryDownloaded(val file: File) : HistoryFragmentState()
    object Refresh : HistoryFragmentState()
}