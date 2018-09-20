package sk.kotlin.sensebox.bl.db.entities

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.Ignore
import android.arch.persistence.room.Index
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
@Parcelize
@Entity(tableName = File.TABLE_NAME,
        indices = [Index(BaseEntity.COLUMN_ID)])
data class File(
        @ColumnInfo(name = File.COLUMN_SIZE)
        var size: Int = 0,
        @ColumnInfo(name = File.COLUMN_COUNT)
        var count: Int = 0,
        @ColumnInfo(name = File.COLUMN_DAY)
        var day: Int = 0,
        @ColumnInfo(name = File.COLUMN_MONTH)
        var month: Int = 0,
        @ColumnInfo(name = File.COLUMN_YEAR)
        var year: Int = 0,
        @ColumnInfo(name = File.COLUMN_AVERAGE_TEMP)
        var averageTemperature: Float? = null,
        @ColumnInfo(name = File.COLUMN_AVERAGE_HUMI)
        var averageHumidity: Float? = null,
        @ColumnInfo(name = File.COLUMN_IS_DOWNLOADED)
        var isDownloaded: Boolean = false,
        @ColumnInfo(name = File.COLUMN_IS_UPDATED)
        var isUpdated: Boolean = false
) : BaseEntity(), Parcelable {

    companion object {
        @Ignore
        const val TABLE_NAME = "file"

        @Ignore
        const val COLUMN_SIZE = "size"
        @Ignore
        const val COLUMN_COUNT = "count"
        @Ignore
        const val COLUMN_DAY = "day"
        @Ignore
        const val COLUMN_MONTH = "month"
        @Ignore
        const val COLUMN_YEAR = "year"
        @Ignore
        const val COLUMN_AVERAGE_TEMP = "average_temp"
        @Ignore
        const val COLUMN_AVERAGE_HUMI = "average_humi"
        @Ignore
        const val COLUMN_IS_DOWNLOADED = "is_downloaded"
        @Ignore
        const val COLUMN_IS_UPDATED = "is_updated"
    }
}