package sk.kotlin.sensebox.bl.db.utils

import android.arch.persistence.room.TypeConverter
import java.util.*


class RoomConverters {

    @TypeConverter
    fun longToDate(value: Long?): Date? {
        return value?.let {
            Date(it)
        }
    }

    @TypeConverter
    fun dateToLong(date: Date?): Long? {
        return date?.let {
            it.time
        }
    }
}