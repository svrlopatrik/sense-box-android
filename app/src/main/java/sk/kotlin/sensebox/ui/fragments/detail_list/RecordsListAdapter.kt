package sk.kotlin.sensebox.ui.fragments.detail_list

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import sk.kotlin.sensebox.bl.db.entities.Record
import sk.kotlin.sensebox.databinding.ItemRecordListBinding

class RecordsListAdapter : RecyclerView.Adapter<RecordsListAdapter.ViewHolder>() {

    private var data: MutableList<Record> = ArrayList()

    fun setData(data: List<Record>) {
        this.data.addAll(data)
        notifyDataSetChanged()
    }

    override fun getItemCount() = data.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(ItemRecordListBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(data[position], if (position != 0) data[position - 1] else null)
    }

    inner class ViewHolder(private val viewBinding: ItemRecordListBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(actual: Record, previous: Record?) {
            viewBinding.item = actual

            viewBinding.executePendingBindings()
        }
    }
}