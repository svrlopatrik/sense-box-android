package sk.kotlin.sensebox.di.modules

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import sk.kotlin.sensebox.bl.vm.*
import sk.kotlin.sensebox.di.ViewModelMapKey

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
@Module
abstract class ViewModelModule {

    @Binds
    abstract fun bindsViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelMapKey(MainActivityViewModel::class)
    abstract fun bindMainActivityViewModel(mainActivityViewModel: MainActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(DetailActivityViewModel::class)
    abstract fun bindDetailActivityViewModel(detailActivityViewModel: DetailActivityViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(ActualFragmentViewModel::class)
    abstract fun bindActualFragmentViewModel(actualFragmentViewModel: ActualFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(HistoryFragmentViewModel::class)
    abstract fun bindHistoryFragmentViewModel(historyFragmentViewModel: HistoryFragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelMapKey(SettingsFragmentViewModel::class)
    abstract fun bindSettingsFragmentViewModel(settingsFragmentViewModel: SettingsFragmentViewModel): ViewModel

}