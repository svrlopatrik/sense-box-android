package sk.kotlin.sensebox.di

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import dagger.android.AndroidInjection
import dagger.android.support.AndroidSupportInjection
import dagger.android.support.HasSupportFragmentInjector
import sk.kotlin.sensebox.SenseBoxApp
import sk.kotlin.sensebox.di.modules.AppModule


class AppInjector private constructor() {

    companion object {
        public fun init(senseBoxApp: SenseBoxApp) {
            DaggerSenseBoxComponent.builder()
                    .appModule(AppModule(senseBoxApp))
                    .build()
                    .inject(senseBoxApp)

            senseBoxApp.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {

                override fun onActivityCreated(activity: Activity?, p1: Bundle?) {
                    handleActivity(activity)
                }

                override fun onActivityPaused(p0: Activity?) {}

                override fun onActivityResumed(p0: Activity?) {}

                override fun onActivityStarted(p0: Activity?) {}

                override fun onActivityDestroyed(p0: Activity?) {}

                override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {}

                override fun onActivityStopped(p0: Activity?) {}

            })
        }

        private fun handleActivity(activity: Activity?) {
            activity?.let {
                if (it is HasSupportFragmentInjector) {
                    AndroidInjection.inject(activity)
                }
                if (it is FragmentActivity) {
                    (activity as FragmentActivity).supportFragmentManager.registerFragmentLifecycleCallbacks(object : FragmentManager.FragmentLifecycleCallbacks() {
                        override fun onFragmentCreated(fm: FragmentManager?, f: Fragment?, savedInstanceState: Bundle?) {
                            if (f is Injectable) {
                                AndroidSupportInjection.inject(f)
                            }
                        }
                    }, true)
                }
            }
        }
    }


}