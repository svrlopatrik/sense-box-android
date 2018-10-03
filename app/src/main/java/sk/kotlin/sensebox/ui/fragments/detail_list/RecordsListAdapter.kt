package sk.kotlin.sensebox.ui.fragments.detail_list

import android.support.v4.content.ContextCompat
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import sk.kotlin.sensebox.R
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
        holder.bind(data[position], if (position != 0) data[position - 1] else null, position)
    }

    inner class ViewHolder(private val viewBinding: ItemRecordListBinding) : RecyclerView.ViewHolder(viewBinding.root) {
        fun bind(actual: Record, previous: Record?, position: Int) {
            viewBinding.item = actual

            if (previous != null) {
                viewBinding.imageTempDiff.apply {
                    when {
                        actual.temperature > previous.temperature -> {
                            setImageResource(R.drawable.ic_arrow_upward)
                            setColorFilter(ContextCompat.getColor(context, R.color.cc_rd_800), android.graphics.PorterDuff.Mode.SRC_IN)
                        }
                        actual.temperature < previous.temperature -> {
                            setImageResource(R.drawable.ic_arrow_downward)
                            setColorFilter(ContextCompat.getColor(context, R.color.cc_cy_600), android.graphics.PorterDuff.Mode.SRC_IN)
                        }
                    }
                }

                viewBinding.imageHumiDiff.apply {
                    when {
                        actual.humidity > previous.humidity -> {
                            setImageResource(R.drawable.ic_arrow_upward)
                            setColorFilter(ContextCompat.getColor(context, R.color.cc_bg_800), android.graphics.PorterDuff.Mode.SRC_IN)
                        }
                        actual.humidity < previous.humidity -> {
                            setImageResource(R.drawable.ic_arrow_downward)
                            setColorFilter(ContextCompat.getColor(context, R.color.cc_bg_500), android.graphics.PorterDuff.Mode.SRC_IN)
                        }
                    }
                }
            } else {
                viewBinding.imageTempDiff.setImageDrawable(null)
                viewBinding.imageHumiDiff.setImageDrawable(null)
            }

            viewBinding.textTime.text = position.toString()
            viewBinding.executePendingBindings()
        }
    }
}