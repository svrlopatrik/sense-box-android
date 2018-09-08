package sk.kotlin.sensebox.ui.fragments.live

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.LiveFragmentViewModel
import sk.kotlin.sensebox.ui.fragments.BaseFragment

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class LiveFragment : BaseFragment<LiveFragmentViewModel>() {

    override fun setLayout() = R.layout.fragment_live

    override fun setViewModel() = ViewModelProviders.of(this, viewModelFactory).get(LiveFragmentViewModel::class.java)

    override fun initViews(savedInstanceState: Bundle?) {

    }
}