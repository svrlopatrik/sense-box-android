package sk.kotlin.sensebox.ui.fragments.live

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import kotlinx.android.synthetic.main.fragment_live.*
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.LiveFragmentViewModel
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
        text_value_temperature.text = String.format("%s %s", getString(R.string.not_measured_symbol), getString(R.string.celsius_unit_symbol))   //todo load unit from settings
        text_value_humidity.text = String.format("%s %s", getString(R.string.not_measured_symbol), getString(R.string.humidity_unit_symbol))
    }
}