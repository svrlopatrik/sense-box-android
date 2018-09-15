package sk.kotlin.sensebox.ui.activities

import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import sk.kotlin.sensebox.bl.vm.BaseViewModel
import sk.kotlin.sensebox.bl.vm.ViewModelFactory
import javax.inject.Inject

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
abstract class BaseActivity<V : BaseViewModel>(private val viewModelClass: Class<V>?) : AppCompatActivity(), HasSupportFragmentInjector {
    constructor() : this(null)

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    protected var viewModel: V? = null
    protected var viewBinding: ViewDataBinding? = null

    protected val disposables: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = DataBindingUtil.setContentView(this, setLayout())
        viewBinding?.setLifecycleOwner(this)

        viewModelClass?.let { vmClass ->
            viewModel = ViewModelProviders.of(this, viewModelFactory).get(vmClass).also {
                it.onViewCreated(savedInstanceState)
            }
        }
        initViews(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
        clearDisposables()
    }

    protected fun removeDisposable(disposable: Disposable) {
        disposables.remove(disposable)
    }

    protected fun addDisposable(disposable: Disposable?) {
        if (disposable != null) {
            disposables.add(disposable)
        }
    }

    protected fun clearDisposables() {
        disposables.clear()
    }

    override fun supportFragmentInjector(): DispatchingAndroidInjector<Fragment> {
        return dispatchingAndroidInjector
    }

    protected abstract fun setLayout(): Int
    protected abstract fun initViews(savedInstanceState: Bundle?)
}