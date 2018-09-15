package sk.kotlin.sensebox.bl.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single
import sk.kotlin.sensebox.bl.db.entities.BaseEntity
import sk.kotlin.sensebox.bl.db.entities.Record

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
@Dao
abstract class RecordDao : BaseDao<Record> {

    @Query("SELECT * FROM `${Record.TABLE_NAME}` WHERE `${BaseEntity.COLUMN_ID}` = :id")
    abstract fun getById(id: String): Single<Record>

    @Query("SELECT * FROM `${Record.TABLE_NAME}`")
    abstract fun getAll(): Flowable<List<Record>>

    @Query("SELECT * FROM `${Record.TABLE_NAME}` WHERE `${Record.COLUMN_FK_FILE}` = :fileId")
    abstract fun getAllByFile(fileId: String): Flowable<List<Record>>

}