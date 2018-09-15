package sk.kotlin.sensebox.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import sk.kotlin.sensebox.bl.bt.BleClient
import sk.kotlin.sensebox.di.ApplicationScope

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
@Module
class BluetoothModule {

    @Provides
    @ApplicationScope
    fun providesBleClient(context: Context): BleClient {
        return BleClient(context)
    }

}