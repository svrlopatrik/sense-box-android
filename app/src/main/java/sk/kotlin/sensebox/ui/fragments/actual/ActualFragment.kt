package sk.kotlin.sensebox.ui.fragments.actual

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import sk.kotlin.sensebox.BR
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.ActualFragmentViewModel
import sk.kotlin.sensebox.ui.fragments.BaseFragment

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class ActualFragment : BaseFragment<ActualFragmentViewModel>() {

    companion object {
        fun getFragment(): ActualFragment {
            return ActualFragment()
        }
    }

    override fun setLayout() = R.layout.fragment_actual

    override fun setViewModel() = ViewModelProviders.of(this, viewModelFactory).get(ActualFragmentViewModel::class.java)

    override fun initViews(savedInstanceState: Bundle?) {
        viewBinding?.apply {
            setVariable(BR.viewModel, viewModel)
        }

        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel?.getRefresh()?.observe(this, Observer { refresh() })
    }
}