package sk.kotlin.sensebox.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import sk.kotlin.sensebox.ui.activities.main.MainActivity


@Module
/**
 * Define all activities inside this module
 */
public abstract class ActivityModule {

    @ContributesAndroidInjector(modules = [FragmentModule::class])
    abstract fun contribueMainActivity(): MainActivity

}