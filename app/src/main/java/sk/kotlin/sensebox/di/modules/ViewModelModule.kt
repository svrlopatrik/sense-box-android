package sk.kotlin.sensebox.di.modules

import android.arch.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.Module
import sk.kotlin.sensebox.bl.vm.ViewModelFactory


@Module
/**
 * Define all viewModels inside this module
 */
abstract class ViewModelModule {

    @Binds
    abstract fun bindsViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

}