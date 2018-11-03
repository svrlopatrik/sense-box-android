package sk.kotlin.sensebox.ui.fragments.history

import android.arch.paging.PagedListAdapter
import android.support.v4.content.ContextCompat
import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.db.entities.File
import sk.kotlin.sensebox.databinding.ItemHistoryListBinding


/**
 * Created by Patrik Švrlo on 9.9.2018.
 */
class HistoryListAdapter(
        private val onItemClickCallback: (File) -> Unit
) : PagedListAdapter<File, HistoryListAdapter.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<File>() {
            override fun areItemsTheSame(oldItem: File?, newItem: File?) = oldItem?.id == newItem?.id

            override fun areContentsTheSame(oldItem: File?, newItem: File?): Boolean {
                return oldItem == newItem
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHistoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = getItem(position)
        if (file != null) {
            holder.bind(file)
        } else {
            holder.clear()
        }
    }

    inner class ViewHolder(private val viewBinding: ItemHistoryListBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(item: File) {
            viewBinding.item = item
            viewBinding.imageState.apply {
                if (!item.isDownloaded) {
                    setImageResource(R.drawable.ic_file_download)
                    setColorFilter(ContextCompat.getColor(context, R.color.cc_gn_800), android.graphics.PorterDuff.Mode.SRC_IN)
                } else if (!item.isUpdated) {
                    setImageResource(R.drawable.ic_loop)
                    setColorFilter(ContextCompat.getColor(context, R.color.cc_yw_800), android.graphics.PorterDuff.Mode.SRC_IN)
                }
            }
            viewBinding.executePendingBindings()

            viewBinding.root.setOnClickListener { onItemClickCallback(item) }
        }

        fun clear() {
            viewBinding.textAverageHumidity.text = null
            viewBinding.textAverageTemperature.text = null
            viewBinding.textDate.text = null
            viewBinding.textMeasurementsCount.text = null
            viewBinding.imageState.setImageDrawable(null)
        }
    }
}