package sk.kotlin.sensebox.ui.fragments.detail_chart

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.DetailChartFragmentViewModel
import sk.kotlin.sensebox.ui.fragments.BaseFragment

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class DetailChartFragment : BaseFragment<DetailChartFragmentViewModel>() {

    companion object {
        fun getFragment(): DetailChartFragment {
            return DetailChartFragment()
        }
    }

    override fun setLayout() = R.layout.fragment_detail_chart

    override fun setViewModel() = ViewModelProviders.of(this, viewModelFactory).get(DetailChartFragmentViewModel::class.java)

    override fun initViews(savedInstanceState: Bundle?) {

    }
}