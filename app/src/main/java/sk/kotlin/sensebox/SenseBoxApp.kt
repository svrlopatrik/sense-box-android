package sk.kotlin.sensebox

import android.Manifest
import android.app.Activity
import android.app.Application
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasActivityInjector
import pub.devrel.easypermissions.EasyPermissions
import sk.kotlin.sensebox.di.AppInjector
import sk.kotlin.sensebox.utils.AppCrashHandler
import sk.kotlin.sensebox.utils.FileLoggingTree
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
        initCrashHandler()
        initDagger()
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())    //logs to console
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Timber.plant(FileLoggingTree(this))
        }
        Timber.d("Timber initialized.")
    }

    private fun initDagger() {
        AppInjector.init(this)
    }

    private fun initCrashHandler() {
        Thread.setDefaultUncaughtExceptionHandler(AppCrashHandler)
    }

    override fun activityInjector() = dispatchingAndroidInjector
}