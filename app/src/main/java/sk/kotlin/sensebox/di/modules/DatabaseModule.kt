package sk.kotlin.sensebox.di.modules

import android.arch.persistence.room.Room
import android.content.Context
import dagger.Module
import dagger.Provides
import sk.kotlin.sensebox.Constants
import sk.kotlin.sensebox.bl.db.Database
import sk.kotlin.sensebox.bl.db.daos.FileDao
import sk.kotlin.sensebox.bl.db.daos.RecordDao
import sk.kotlin.sensebox.di.ApplicationScope

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
@Module
class DatabaseModule {

    @Provides
    @ApplicationScope
    fun providesDatabase(context: Context): Database {
        return Room.databaseBuilder(context, Database::class.java, Constants.DATABASE_NAME).build()
    }

    @Provides
    @ApplicationScope
    fun providesFileDao(db: Database): FileDao = db.getFileDao()

    @Provides
    @ApplicationScope
    fun providesRecordDao(db: Database): RecordDao = db.getRecordDao()

}