package sk.kotlin.sensebox.ui.fragments

import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import sk.kotlin.sensebox.bl.vm.BaseViewModel
import sk.kotlin.sensebox.bl.vm.ViewModelFactory
import sk.kotlin.sensebox.di.Injectable
import sk.kotlin.sensebox.utils.inflate
import javax.inject.Inject

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
abstract class BaseFragment<V : BaseViewModel> : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    protected var viewModel: V? = null
    protected var viewBinding: ViewDataBinding? = null

    protected val disposables: CompositeDisposable by lazy {
        CompositeDisposable()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        viewBinding = DataBindingUtil.inflate(inflater, setLayout(), container, false)
        return viewBinding?.root ?: container?.inflate(setLayout())
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = setViewModel()?.also {
            it.onViewCreated(savedInstanceState)
        }
        viewBinding?.setLifecycleOwner(activity)
        initViews(savedInstanceState)
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

    override fun onDetach() {
        super.onDetach()
        clearDisposables()
    }

    protected fun refresh() {
        fragmentManager?.beginTransaction()?.detach(this)?.attach(this)?.commit()
    }

    protected abstract fun setLayout(): Int
    protected abstract fun setViewModel(): V?
    protected abstract fun initViews(savedInstanceState: Bundle?)

}