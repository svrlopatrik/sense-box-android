package sk.kotlin.sensebox.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import sk.kotlin.sensebox.ui.activities.main.MainActivity

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
@Module

public abstract class ActivityModule {

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    abstract fun contribueMainActivity(): MainActivity

}