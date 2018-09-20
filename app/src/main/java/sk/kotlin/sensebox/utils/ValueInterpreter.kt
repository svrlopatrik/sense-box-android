package sk.kotlin.sensebox.utils

import android.annotation.SuppressLint
import sk.kotlin.sensebox.Constants
import sk.kotlin.sensebox.bl.PreferencesManager
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

    fun intToByteArray(value: Int): ByteArray {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(value).array()
    }

    @JvmStatic
    fun unixTimestampToMillis(timestamp: Int): Long {
        return timestamp * 1000L
    }

    @JvmStatic
    fun celsiusToFahrenheit(value: Float): Float {
        return (value * 9 / 5) + 32
    }

    @JvmStatic
    fun millisToUtcDatetime(millis: Long): String {
        return millisToFormattedForm(millis, Constants.TIMEZONE_RTC_MODULE, PreferencesManager.getStringValue(PreferencesManager.PreferenceKey.DATETIME_FORMAT))
    }

    @JvmStatic
    fun millisToDate(millis: Long, timezone: String = TimeZone.getDefault().id): String {
        return millisToFormattedForm(millis, TimeZone.getDefault().id, PreferencesManager.getStringValue(PreferencesManager.PreferenceKey.DATE_FORMAT))
    }

    @JvmStatic
    fun millisToUtcTime(millis: Long, timezone: String = TimeZone.getDefault().id): String {
        return millisToFormattedForm(millis, Constants.TIMEZONE_RTC_MODULE, PreferencesManager.getStringValue(PreferencesManager.PreferenceKey.TIME_FORMAT))
    }

    @JvmStatic
    fun rawDateToFormattedDate(rawDate: Int): String {
        return millisToDate(rawDateToCalendar(rawDate).timeInMillis)
    }

    private fun millisToFormattedForm(millis: Long, timezone: String, format: String): String {
        return if (millis != 0L) {
            SimpleDateFormat(format).run {
                timeZone = TimeZone.getTimeZone(timezone)
                this.format(Date(millis))
            }
        } else {
            "---"
        }
    }

    @JvmStatic
    fun rawDateToCalendar(rawDate: Int): Calendar {
        return Calendar.getInstance().apply {
            time = rawDateFormatter.parse(rawDate.toString())
        }
    }

    @JvmStatic
    fun floatToFormattedTemperature(value: Float): String {
        return floatToFormattedValue(value, 2, PreferencesManager.getStringValue(PreferencesManager.PreferenceKey.TEMPERATURE_SYMBOL))
    }

    @JvmStatic
    fun floatToFormattedHumidity(value: Float): String {
        return floatToFormattedValue(value, 2, "%")
    }

    private fun floatToFormattedValue(value: Float, decimals: Int, unit: String): String {
        return String.format("%.${decimals}f %s", value, unit)
    }
}