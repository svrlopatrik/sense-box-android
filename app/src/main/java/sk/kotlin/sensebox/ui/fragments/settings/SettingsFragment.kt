package sk.kotlin.sensebox.ui.fragments.settings

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import sk.kotlin.sensebox.BR
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.SettingsFragmentViewModel
import sk.kotlin.sensebox.ui.fragments.BaseFragment

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class SettingsFragment : BaseFragment<SettingsFragmentViewModel>() {

    companion object {
        fun getFragment(): SettingsFragment {
            return SettingsFragment()
        }
    }

    override fun setLayout() = R.layout.fragment_settings

    override fun setViewModel() = ViewModelProviders.of(this, viewModelFactory).get(SettingsFragmentViewModel::class.java)

    override fun initViews(savedInstanceState: Bundle?) {
        viewBinding?.apply {
            setVariable(BR.viewModel, viewModel)
        }
    }

}