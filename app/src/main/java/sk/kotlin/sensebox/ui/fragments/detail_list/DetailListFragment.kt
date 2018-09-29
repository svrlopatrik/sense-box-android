package sk.kotlin.sensebox.ui.fragments.detail_list

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.DetailListFragmentViewModel
import sk.kotlin.sensebox.ui.fragments.BaseFragment

class DetailListFragment : BaseFragment<DetailListFragmentViewModel>() {

    companion object {
        fun getFragment(): DetailListFragment {
            return DetailListFragment()
        }
    }

    override fun setLayout() = R.layout.fragment_detail_list

    override fun setViewModel() = ViewModelProviders.of(this, viewModelFactory).get(DetailListFragmentViewModel::class.java)

    override fun initViews(savedInstanceState: Bundle?) {

    }
}