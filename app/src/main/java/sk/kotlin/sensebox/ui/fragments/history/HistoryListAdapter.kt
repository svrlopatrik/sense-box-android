package sk.kotlin.sensebox.ui.fragments.history

import android.content.Context
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
        private val context: Context,
        private val onItemClickCallback: (File) -> Unit
) : RecyclerView.Adapter<HistoryListAdapter.ViewHolder>() {

    private var data: MutableList<File> = ArrayList()

    fun setData(data: List<File>) {
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    fun newData(data: List<File>) {
        if (this.data.isEmpty()) {
            setData(data)
            return  //diff calculation not needed
        }

        val diffCallback = HistoryListDiffCallback(this.data, data)
        val diffResult = DiffUtil.calculateDiff(diffCallback, true)

        this.data.clear()
        this.data.addAll(data)

        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemHistoryListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position])
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
    }
}