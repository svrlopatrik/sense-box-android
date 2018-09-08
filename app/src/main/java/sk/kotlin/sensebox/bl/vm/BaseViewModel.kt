package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.ViewModel
import android.os.Bundle
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
abstract class BaseViewModel : ViewModel() {

    protected val disposables: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    protected fun removeDisposable(disposable: Disposable) {
        disposables.remove(disposable)
    }

    protected fun addDisposable(disposable: Disposable) {
        disposables.add(disposable)
    }

    protected fun clearDisposables() {
        disposables.clear()
    }

    override fun onCleared() {
        super.onCleared()
        clearDisposables()
    }

    abstract fun onViewCreated(savedInstanceState: Bundle?)

}