package sk.kotlin.sensebox.ui.fragments.history

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_history.*
import sk.kotlin.sensebox.BR
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.db.entities.File
import sk.kotlin.sensebox.bl.vm.HistoryFragmentViewModel
import sk.kotlin.sensebox.models.ui_states.HistoryFragmentState
import sk.kotlin.sensebox.ui.activities.detail.DetailActivity
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
        viewBinding?.apply {
            setVariable(BR.viewModel, viewModel)
        }

        initList()
        observeLiveData()
    }

    private fun observeLiveData() {
        viewModel?.getHistoryFragmentState()?.observe(this, Observer { state -> state?.let { render(it) } })
        viewModel?.getHistoryList()?.observe(this, Observer { data ->
            data?.let {
                if (data.size != 0) {
                    historyListAdapter.submitList(data)
                    list_history.refresh()

                    if (data.size == 1) {
                        historyListAdapter.notifyDataSetChanged()
                    }
                }
            }
        })
    }

    private fun render(state: HistoryFragmentState) {
        when (state) {
            is HistoryFragmentState.Refresh -> refresh()
            is HistoryFragmentState.HistoryDownloaded -> startDetailActivity(state.file)
        }
    }

    private fun initList() {
        context?.let {
            historyListAdapter = HistoryListAdapter { item -> onHistoryItemClick(item) }

            list_history.apply {
                layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
                setHasFixedSize(true)
                adapter = historyListAdapter
            }
        }
    }

    private fun onHistoryItemClick(item: File) {
        if (!item.isDownloaded) {
            //try to download
            viewModel?.downloadHistoryData(item)
        } else if (!item.isUpdated) {
            //display selection dialog - show or update

        } else {
            //all records up to date
            startDetailActivity(item)
        }
    }

    private fun startDetailActivity(file: File) {
        DetailActivity.startActivity(requireContext(), file)
        activity?.overridePendingTransition(R.anim.slide_in_top, 0)
    }
}