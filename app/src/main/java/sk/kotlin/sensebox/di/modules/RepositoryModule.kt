package sk.kotlin.sensebox.di.modules

import dagger.Module
import dagger.Provides
import sk.kotlin.sensebox.bl.PreferencesManager
import sk.kotlin.sensebox.bl.bt.BleClient
import sk.kotlin.sensebox.bl.db.daos.FileDao
import sk.kotlin.sensebox.bl.db.daos.RecordDao
import sk.kotlin.sensebox.bl.repos.ActualRepository
import sk.kotlin.sensebox.bl.repos.HistoryRepository
import sk.kotlin.sensebox.di.ApplicationScope

/**
 * Created by Patrik Švrlo on 5.10.2018.
 */
@Module
class RepositoryModule {

    @Provides
    @ApplicationScope
    fun providesHistoryRepository(bleClient: BleClient, fileDao: FileDao, recordDao: RecordDao): HistoryRepository {
        return HistoryRepository(bleClient, fileDao, recordDao)
    }

    @Provides
    @ApplicationScope
    fun providesActualRepository(bleClient: BleClient, preferencesManager: PreferencesManager): ActualRepository {
        return ActualRepository(bleClient, preferencesManager)
    }

}