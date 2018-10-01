package sk.kotlin.sensebox.ui.fragments.detail_list

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_detail_list.*
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.DetailActivityViewModel
import sk.kotlin.sensebox.ui.fragments.BaseFragment

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class DetailListFragment : BaseFragment<DetailActivityViewModel>() {

    companion object {
        fun getFragment(): DetailListFragment {
            return DetailListFragment()
        }
    }

    private lateinit var recordsListAdapter: RecordsListAdapter

    override fun setLayout() = R.layout.fragment_detail_list

    override fun setViewModel(): DetailActivityViewModel? {
        return activity?.let {
            ViewModelProviders.of(it, viewModelFactory).get(DetailActivityViewModel::class.java)
        }
    }

    override fun initViews(savedInstanceState: Bundle?) {
        initList()
        observeRecords()
    }

    private fun initList() {
        context?.let {
            recordsListAdapter = RecordsListAdapter()

            list_records.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(it)
                adapter = recordsListAdapter
            }
        }
    }

    private fun observeRecords() {
        viewModel?.getLoadedRecords()?.observe(this, Observer { data -> data?.let { recordsListAdapter.setData(it) } })
    }
}