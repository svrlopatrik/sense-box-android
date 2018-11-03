package sk.kotlin.sensebox.models.repo_states

import android.arch.paging.PagedList
import sk.kotlin.sensebox.bl.db.entities.File

/**
 * Created by Patrik Švrlo on 7.10.2018.
 */
sealed class FileRepositoryState {
    data class PagedFiles(val data: PagedList<File>) : FileRepositoryState()

    object NewFileInserted : FileRepositoryState()
    object NewRecordInserted : FileRepositoryState()
    object FileUpdated : FileRepositoryState()
    object EndOfResponse : FileRepositoryState()
    object UndefinedResponse : FileRepositoryState()
    object ErrorProcessHistoryData : FileRepositoryState()

    data class DownloadHistoryFinished(val file: File) : FileRepositoryState()

}