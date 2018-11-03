package sk.kotlin.sensebox.models

import android.arch.paging.PagedList
import sk.kotlin.sensebox.bl.db.entities.File

/**
 * Created by Patrik Švrlo on 14.10.2018.
 */
sealed class HistoryModel {

    data class List(val data: PagedList<File>) : HistoryModel()
    data class Item(val name: String, val size: Int) : HistoryModel()
    data class MeasurementsDownloaded(val file: File) : HistoryModel()
    object ListDownloaded : HistoryModel()
}