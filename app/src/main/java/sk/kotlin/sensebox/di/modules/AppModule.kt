package sk.kotlin.sensebox.di.modules

import android.content.Context
import dagger.Module
import dagger.Provides
import sk.kotlin.sensebox.SenseBoxApp
import sk.kotlin.sensebox.di.ApplicationScope

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
@Module(includes = [(ActivityModule::class), (ViewModelModule::class)])
class AppModule(private val senseBoxApp: SenseBoxApp) {

    @Provides
    @ApplicationScope
    fun providesContext(): Context = senseBoxApp.applicationContext

    @Provides
    @ApplicationScope
    fun providesApplication() = senseBoxApp
}