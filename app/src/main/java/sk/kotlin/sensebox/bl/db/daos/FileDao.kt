package sk.kotlin.sensebox.bl.db.daos

import android.arch.paging.DataSource
import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import io.reactivex.Maybe
import io.reactivex.Single
import sk.kotlin.sensebox.bl.db.entities.BaseEntity
import sk.kotlin.sensebox.bl.db.entities.File

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
@Dao
abstract class FileDao : BaseDao<File>() {

    @Query("SELECT * FROM `${File.TABLE_NAME}` WHERE `${BaseEntity.COLUMN_ID}` = :id")
    abstract fun getById(id: String): Maybe<File>

    @Query("SELECT * FROM `${File.TABLE_NAME}` ORDER BY `${BaseEntity.COLUMN_ID}` DESC")
    abstract fun getAll(): Single<List<File>>

    @Query("SELECT * FROM `${File.TABLE_NAME}` ORDER BY `${BaseEntity.COLUMN_ID}` DESC LIMIT :offset, :count")
    abstract fun getFromRange(offset: Int, count: Int): Single<List<File>>

    @Query("SELECT * FROM `${File.TABLE_NAME}` ORDER BY `${BaseEntity.COLUMN_ID}` DESC")
    abstract fun getAllPaged(): DataSource.Factory<Int, File>

}