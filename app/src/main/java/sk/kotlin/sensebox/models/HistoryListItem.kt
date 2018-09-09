package sk.kotlin.sensebox.models

/**
 * Created by Patrik Švrlo on 9.9.2018.
 */
data class HistoryListItem(
        val id: Long,
        val fileName: String,
        val fileSize: Long,
        val measurementsCount: Int,
        val isDownloaded: Boolean,
        val isUpdated: Boolean
)