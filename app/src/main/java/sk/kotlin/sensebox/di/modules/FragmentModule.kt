package sk.kotlin.sensebox.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import sk.kotlin.sensebox.ui.fragments.history.HistoryFragment
import sk.kotlin.sensebox.ui.fragments.history_detail.HistoryDetailFragment
import sk.kotlin.sensebox.ui.fragments.live.LiveFragment
import sk.kotlin.sensebox.ui.fragments.settings.SettingsFragment

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
@Module
abstract class FragmentModule {

    @ContributesAndroidInjector
    abstract fun contributeLiveFragment(): LiveFragment

    @ContributesAndroidInjector
    abstract fun contributeHistoryFragment(): HistoryFragment

    @ContributesAndroidInjector
    abstract fun contributeHistoryDetailFragment(): HistoryDetailFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment
}