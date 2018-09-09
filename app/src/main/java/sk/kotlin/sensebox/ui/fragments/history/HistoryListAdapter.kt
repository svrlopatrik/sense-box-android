package sk.kotlin.sensebox.ui.fragments.history

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import sk.kotlin.sensebox.databinding.ItemHistoryListBinding
import sk.kotlin.sensebox.models.HistoryListItem


/**
 * Created by Patrik Švrlo on 9.9.2018.
 */
class HistoryListAdapter(
        private val context: Context,
        private val onItemClickCallback: (HistoryListItem) -> Unit
) : RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    private var data: List<HistoryListItem> = emptyList()

    fun setData(data: List<HistoryListItem>) {

    }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHistoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
    }

    inner class ViewHolder(private val viewBinding: ItemHistoryListBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: HistoryListItem) {
            viewBinding.item = item
            viewBinding.executePendingBindings()

            viewBinding.root.setOnClickListener { onItemClickCallback(item) }
        }
    }
}