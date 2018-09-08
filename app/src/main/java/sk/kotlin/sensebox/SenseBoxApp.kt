package sk.kotlin.sensebox

import android.app.Activity
import android.app.Application
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import sk.kotlin.sensebox.di.AppInjector
import timber.log.Timber
import javax.inject.Inject

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class SenseBoxApp : Application(), HasActivityInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>

    override fun onCreate() {
        super.onCreate()

        initTimber()
        initDagger()
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())    //logs to console
        Timber.d("Timber initialized.")
    }

    private fun initDagger() {
        AppInjector.init(this)
    }

    override fun activityInjector() = dispatchingAndroidInjector
}