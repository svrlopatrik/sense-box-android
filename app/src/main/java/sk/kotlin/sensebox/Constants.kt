package sk.kotlin.sensebox

import java.util.*

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
object Constants {

    const val DATABASE_NAME = "sk.kotlin.sensebox.db"
    const val DATABASE_VERSION = 1

    const val PREFERENCES_NAME = "sk.kotlin.sensebox.preferences"

    const val BLE_DEVICE_MAC = "60:64:05:BF:64:3C"
    val BLE_UUID_DESCRIPTOR: UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb")
    val BLE_UUID_CHARACTERISTIC: UUID = UUID.fromString("000007c8-0000-1000-8000-00805f9b34fb")
    val BLE_UUID_SERVICE: UUID = UUID.fromString("00000b55-0000-1000-8000-00805f9b34fb")

    const val MODULE_TEMPERATURE_MAX_VALUE = 80
    const val MODULE_TEMPERATURE_MIN_VALUE = -40
    const val MODULE_HUMIDITY_MAX_VALUE = 100
    const val MODULE_HUMIDITY_MIN_VALUE = 0

    const val UNIT_FLAG_TEMPERATURE_CELSIUS = 0x01.toByte()
    const val UNIT_FLAG_TEMPERATURE_FAHRENHEIT = 0x02.toByte()

    const val REQUEST_CODE_ACTUAL = 0x1D.toByte()
    const val REQUEST_CODE_LIST = 0x1E.toByte()
    const val REQUEST_CODE_HISTORY = 0x1F.toByte()

    const val RESPONSE_FLAG_TIMESTAMP = 0x00.toByte()
    const val RESPONSE_FLAG_TEMPERATURE = 0x01.toByte()
    const val RESPONSE_FLAG_HUMIDITY = 0x02.toByte()
    val RESPONSE_FLAG_END = "END".toByteArray()
    val RESPONSE_FLAG_UDEF = "UDEF".toByteArray()

    val ENABLE_NOTIFICATIONS_INDICATIONS = byteArrayOf(0x03, 0x00)

    const val TIMEZONE_RTC_MODULE = "UTC"

    const val DATE_FORMAT_RAW = "yyyyMMdd"
    const val DATE_FORMAT_DEFAULT = "dd. MMMM yyyy"
    const val TIME_FORMAT_DEFAULT = "HH:mm:ss"
    const val DATETIME_FORMAT_DEFAULT = "dd.MMMM yyyy HH:mm:ss"

}