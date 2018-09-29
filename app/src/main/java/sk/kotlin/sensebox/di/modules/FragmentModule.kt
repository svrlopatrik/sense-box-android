package sk.kotlin.sensebox.di.modules

import dagger.Module
import dagger.android.ContributesAndroidInjector
import sk.kotlin.sensebox.ui.fragments.detail_chart.DetailChartFragment
import sk.kotlin.sensebox.ui.fragments.detail_list.DetailListFragment
import sk.kotlin.sensebox.ui.fragments.history.HistoryFragment
import sk.kotlin.sensebox.ui.fragments.live.LiveFragment
import sk.kotlin.sensebox.ui.fragments.notification.NotificationFragment
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
    abstract fun contributeDetailChartFragment(): DetailChartFragment

    @ContributesAndroidInjector
    abstract fun contributeDetailListFragment(): DetailListFragment

    @ContributesAndroidInjector
    abstract fun contributeSettingsFragment(): SettingsFragment

    @ContributesAndroidInjector
    abstract fun contributeNotificationFragment(): NotificationFragment
}