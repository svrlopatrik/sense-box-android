package sk.kotlin.sensebox.ui.fragments.history

import android.os.Bundle
import android.support.v7.util.DiffUtil
import sk.kotlin.sensebox.bl.db.entities.File

class HistoryListDiffCallback(
        private val oldList: List<File>,
        private val newList: List<File>
) : DiffUtil.Callback() {

    override fun getOldListSize() = oldList.size

    override fun getNewListSize() = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition].id == newList[newItemPosition].id

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int) = oldList[oldItemPosition] == newList[newItemPosition]

    override fun getChangePayload(oldItemPosition: Int, newItemPosition: Int): Bundle {
        val bundle = Bundle()
        val oldItem = oldList[oldItemPosition]
        val newItem = newList[newItemPosition]

        if (oldItem.size != newItem.size) {
            bundle.putInt(File.COLUMN_SIZE, newItem.size)
        }
        if (oldItem.count != newItem.count) {
            bundle.putInt(File.COLUMN_COUNT, newItem.count)
        }
        if (oldItem.averageTemperature != newItem.averageTemperature) {
            bundle.putFloat(File.COLUMN_AVERAGE_TEMP, newItem.averageTemperature!!)
        }
        if (oldItem.averageHumidity != newItem.averageHumidity) {
            bundle.putFloat(File.COLUMN_AVERAGE_HUMI, newItem.averageHumidity!!)
        }
        if (oldItem.isDownloaded != newItem.isDownloaded) {
            bundle.putBoolean(File.COLUMN_IS_DOWNLOADED, newItem.isDownloaded)
        }
        if (oldItem.isUpdated != newItem.isUpdated) {
            bundle.putBoolean(File.COLUMN_IS_UPDATED, newItem.isUpdated)
        }

        return bundle
    }
}