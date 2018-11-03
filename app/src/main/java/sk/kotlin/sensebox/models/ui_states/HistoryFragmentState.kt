package sk.kotlin.sensebox.models.ui_states

import android.arch.paging.PagedList
import sk.kotlin.sensebox.bl.db.entities.File

/**
 * Created by Patrik Švrlo on 16.9.2018.
 */
sealed class HistoryFragmentState {
    data class Data(val data: PagedList<File>) : HistoryFragmentState()
    data class Error(val message: String) : HistoryFragmentState()
    data class HistoryDownloaded(val file: File) : HistoryFragmentState()
    object Refresh : HistoryFragmentState()
}