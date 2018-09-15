package sk.kotlin.sensebox.utils

import android.annotation.SuppressLint
import sk.kotlin.sensebox.Constants
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
@SuppressLint("SimpleDateFormat")
object ValueInterpreter {

    private val rawDateFormatter = SimpleDateFormat(Constants.DATE_FORMAT_RAW)

    fun byteArrayToInt(byteArray: ByteArray): Int {
        val byteBuffer = ByteBuffer.wrap(byteArray)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        return byteBuffer.int
    }

    fun byteArrayToFloat(byteArray: ByteArray): Float {
        val byteBuffer = ByteBuffer.wrap(byteArray)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        return byteBuffer.float
    }

    fun byteArrayToShort(byteArray: ByteArray): Short {
        val byteBuffer = ByteBuffer.wrap(byteArray)
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN)
        return byteBuffer.short
    }

    fun unixTimestampToMillis(timestamp: Int): Long {
        return timestamp * 1000L
    }

    fun celsiusToFahrenheit(value: Float): Float {
        return (value * 9 / 5) + 32
    }

    @JvmStatic
    fun millisToFormattedDate(millis: Long, format: String? = Constants.DATE_FORMAT_DEFAULT): String {
        return if (millis != 0L && format?.isNotBlank() == true) {
            SimpleDateFormat(format).run {
                timeZone = TimeZone.getTimeZone("UTC")
                this.format(Date(millis))
            }
        } else {
            ""
        }
    }

    @JvmStatic
    fun floatToString(value: Float, decimals: Int = 2): String {
        return String.format("%.${decimals}f", value)
    }

    fun rawDateToCalendar(rawDate: Int): Calendar {
        return Calendar.getInstance().apply {
            time = rawDateFormatter.parse(rawDate.toString())
        }
    }

}