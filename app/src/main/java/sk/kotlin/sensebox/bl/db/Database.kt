package sk.kotlin.sensebox.bl.db

import android.arch.persistence.room.Database
import android.arch.persistence.room.RoomDatabase
import sk.kotlin.Constants
import sk.kotlin.sensebox.bl.db.daos.FileDao
import sk.kotlin.sensebox.bl.db.daos.RecordDao
import sk.kotlin.sensebox.bl.db.entities.File
import sk.kotlin.sensebox.bl.db.entities.Record

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
@Database(entities = [File::class, Record::class], version = Constants.DATABASE_VERSION, exportSchema = false)
abstract class Database : RoomDatabase() {

    abstract fun getFileDao(): FileDao
    abstract fun getRecordDao(): RecordDao
}