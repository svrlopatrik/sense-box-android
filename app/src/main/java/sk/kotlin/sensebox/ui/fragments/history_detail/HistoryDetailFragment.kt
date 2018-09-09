package sk.kotlin.sensebox.ui.fragments.history_detail

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.HistoryDetailFragmentViewModel
import sk.kotlin.sensebox.ui.fragments.BaseFragment

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class HistoryDetailFragment : BaseFragment<HistoryDetailFragmentViewModel>() {

    companion object {
        fun getFragment(): HistoryDetailFragment {
            return HistoryDetailFragment()
        }
    }

    override fun setLayout() = R.layout.fragment_history_detail

    override fun setViewModel() = ViewModelProviders.of(this, viewModelFactory).get(HistoryDetailFragmentViewModel::class.java)

    override fun initViews(savedInstanceState: Bundle?) {

    }
}