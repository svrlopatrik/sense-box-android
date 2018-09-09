package sk.kotlin.sensebox.ui.fragments.history

import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_history.*
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.vm.HistoryFragmentViewModel
import sk.kotlin.sensebox.models.HistoryListItem
import sk.kotlin.sensebox.ui.fragments.BaseFragment

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class HistoryFragment : BaseFragment<HistoryFragmentViewModel>() {

    companion object {
        fun getFragment(): HistoryFragment {
            return HistoryFragment()
        }
    }

    private lateinit var historyListAdapter: HistoryListAdapter

    override fun setLayout() = R.layout.fragment_history

    override fun setViewModel() = ViewModelProviders.of(this, viewModelFactory).get(HistoryFragmentViewModel::class.java)

    override fun initViews(savedInstanceState: Bundle?) {
        initList()
    }

    private fun initList() {
        context?.let {
            historyListAdapter = HistoryListAdapter(it) { item -> onHistoryItemClick(item) }

            list_history.apply {
                setHasFixedSize(true)
                layoutManager = LinearLayoutManager(it)
                adapter = historyListAdapter
            }
        }
    }

    private fun onHistoryItemClick(item: HistoryListItem) {

    }
}