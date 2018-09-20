package sk.kotlin.sensebox.bl.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Maybe
import sk.kotlin.sensebox.bl.db.entities.BaseEntity
import sk.kotlin.sensebox.bl.db.entities.File

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
@Dao
abstract class FileDao : BaseDao<File> {

    @Query("SELECT * FROM `${File.TABLE_NAME}` WHERE `${BaseEntity.COLUMN_ID}` = :id")
    abstract fun getById(id: String): Maybe<File>

    @Query("SELECT * FROM `${File.TABLE_NAME}` ORDER BY `${BaseEntity.COLUMN_ID}` DESC")
    abstract fun getAllLive(): Flowable<List<File>>

    @Query("SELECT * FROM `${File.TABLE_NAME}` ORDER BY `${BaseEntity.COLUMN_ID}` DESC LIMIT :offset, :count")
    abstract fun getFromRangeLive(offset: Int, count: Int): Flowable<List<File>>

    fun getAll(): Flowable<List<File>> = getAllLive().take(1)
    fun getFromRange(offset: Int, count: Int): Flowable<List<File>> = getFromRangeLive(offset, count)

}