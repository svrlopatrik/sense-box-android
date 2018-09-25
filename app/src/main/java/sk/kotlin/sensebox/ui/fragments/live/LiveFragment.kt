package sk.kotlin.sensebox.ui.fragments.live

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import sk.kotlin.sensebox.BR
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.LiveFragmentViewModel
import sk.kotlin.sensebox.models.states.LiveFragmentState
import sk.kotlin.sensebox.ui.fragments.BaseFragment

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class LiveFragment : BaseFragment<LiveFragmentViewModel>() {

    companion object {
        fun getFragment(): LiveFragment {
            return LiveFragment()
        }
    }

    override fun setLayout() = R.layout.fragment_live

    override fun setViewModel() = ViewModelProviders.of(this, viewModelFactory).get(LiveFragmentViewModel::class.java)

    override fun initViews(savedInstanceState: Bundle?) {
        viewBinding?.apply {
            setVariable(BR.viewModel, viewModel)
        }

        observeState()
    }

    private fun observeState() {
        viewModel?.let { viewModel ->
            viewModel.getLiveFragmentState().observe(this, Observer { state -> state?.let { render(it) } })
        }
    }

    private fun render(state: LiveFragmentState) {
        when (state) {
            is LiveFragmentState.Refresh -> refresh()
        }
    }
}