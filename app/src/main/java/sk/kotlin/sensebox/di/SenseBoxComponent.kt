package sk.kotlin.sensebox.di

import dagger.Component
import dagger.android.support.AndroidSupportInjectionModule
import sk.kotlin.sensebox.SenseBoxApp
import sk.kotlin.sensebox.di.modules.AppModule
import sk.kotlin.sensebox.di.modules.BluetoothModule
import sk.kotlin.sensebox.di.modules.DatabaseModule
import sk.kotlin.sensebox.di.modules.RepositoryModule

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
@ApplicationScope
@Component(modules = [AndroidSupportInjectionModule::class,
    AppModule::class,
    DatabaseModule::class,
    BluetoothModule::class,
    RepositoryModule::class
    /*another modules*/
])
interface SenseBoxComponent {
    fun inject(senseBoxApp: SenseBoxApp)
}