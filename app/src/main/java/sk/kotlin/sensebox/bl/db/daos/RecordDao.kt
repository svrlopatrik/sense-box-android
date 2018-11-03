package sk.kotlin.sensebox.bl.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import io.reactivex.Single
import sk.kotlin.sensebox.bl.db.entities.BaseEntity
import sk.kotlin.sensebox.bl.db.entities.Record
import sk.kotlin.sensebox.bl.db.models.FileProperties

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
@Dao
abstract class RecordDao : BaseDao<Record>() {

    @Query("SELECT * FROM `${Record.TABLE_NAME}` WHERE `${BaseEntity.COLUMN_ID}` = :id")
    abstract fun getById(id: String): Single<Record>

    @Query("SELECT * FROM `${Record.TABLE_NAME}`")
    abstract fun getAll(): Single<List<Record>>

    @Query("SELECT * FROM `${Record.TABLE_NAME}` WHERE `${Record.COLUMN_FK_FILE}` = :fileId ORDER BY `${BaseEntity.COLUMN_ID}` ASC")
    abstract fun getAllByFile(fileId: String): Single<List<Record>>

    /*
    Raw query:
    SELECT COUNT(*) AS count, AVG(temperature) AS average_temperature, AVG(humidity) AS average_humidity FROM record
    WHERE fk_file = fileId
    */
    @Query("SELECT COUNT(*) AS `${FileProperties.PROPERTY_COUNT}`, AVG(`${Record.COLUMN_TEMPERATURE}`) AS `${FileProperties.PROPERTY_AVERAGE_TEMPERATURE}`, AVG(`${Record.COLUMN_HUMIDITY}`) AS `${FileProperties.PROPERTY_AVERAGE_HUMIDITY}` FROM `${Record.TABLE_NAME}` WHERE `${Record.COLUMN_FK_FILE}` = :fileId")
    abstract fun getFileProperties(fileId: String): Single<FileProperties>

}