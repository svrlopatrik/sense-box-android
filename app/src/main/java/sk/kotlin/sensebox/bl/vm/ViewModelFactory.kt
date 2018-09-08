package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider
import sk.kotlin.sensebox.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Provider

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
@ApplicationScope
class ViewModelFactory @Inject constructor(private val creators: Map<Class<out ViewModel>,
        @JvmSuppressWildcards Provider<ViewModel>>) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        var creator: Provider<out ViewModel>? = creators[modelClass]
        if (creator == null) {
            for ((key, value) in creators) {
                if (modelClass.isAssignableFrom(key)) {
                    creator = value
                    break
                }
            }
        }
        if (creator == null) {
            throw IllegalArgumentException("unknown model class " + modelClass)
        }
        try {
            return creator.get() as T
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}