package sk.kotlin.sensebox.bl.db.models

import android.arch.persistence.room.ColumnInfo

/**
 * Created by Patrik Švrlo on 30.9.2018.
 */
data class FileProperties(
        @ColumnInfo(name = PROPERTY_COUNT) val count: Int,
        @ColumnInfo(name = PROPERTY_AVERAGE_TEMPERATURE) val averageTemperature: Float,
        @ColumnInfo(name = PROPERTY_AVERAGE_HUMIDITY) val averageHumidity: Float
) {
    companion object {
        const val PROPERTY_COUNT = "count"
        const val PROPERTY_AVERAGE_TEMPERATURE = "average_temperature"
        const val PROPERTY_AVERAGE_HUMIDITY = "average_humidity"
    }
}