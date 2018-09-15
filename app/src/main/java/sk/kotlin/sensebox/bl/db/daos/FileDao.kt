package sk.kotlin.sensebox.bl.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single
import sk.kotlin.sensebox.bl.db.entities.BaseEntity
import sk.kotlin.sensebox.bl.db.entities.File

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
@Dao
abstract class FileDao : BaseDao<File> {

    @Query("SELECT * FROM `${File.TABLE_NAME}` WHERE `${BaseEntity.COLUMN_ID}` = :id")
    abstract fun getById(id: String): Single<File>

    @Query("SELECT * FROM `${File.TABLE_NAME}`")
    abstract fun getAll(): Flowable<List<File>>

}