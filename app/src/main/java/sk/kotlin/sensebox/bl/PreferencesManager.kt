package sk.kotlin.sensebox.bl

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import sk.kotlin.sensebox.Constants

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
@SuppressLint("ApplySharedPref")
class PreferencesManager(context: Context, name: String) {

    companion object {
        private lateinit var preferencesMap: MutableMap<PreferenceKey, Any?>

        private fun loadPreferences(sharedPreferences: SharedPreferences) {
            val preferencesMap: MutableMap<PreferenceKey, Any?> = emptyMap<PreferenceKey, Any?>().toMutableMap()
            for (preferenceKey in PreferenceKey.values()) {
                preferencesMap[preferenceKey] = getValueByType(sharedPreferences, preferenceKey)
            }

            this.preferencesMap = preferencesMap
        }

        private fun updatePreference(sharedPreferences: SharedPreferences, key: String) {
            val preferenceKey = PreferenceKey.valueOf(key)
            preferencesMap[preferenceKey] = getValueByType(sharedPreferences, preferenceKey)
        }

        private fun getValueByType(sharedPreferences: SharedPreferences, enum: PreferenceKey): Any {
            return when (enum.getDefault()) {
                is Int -> {
                    sharedPreferences.getInt(enum.name, enum.getDefault() as Int)
                }
                is Float -> {
                    sharedPreferences.getFloat(enum.name, enum.getDefault() as Float)
                }
                is String -> {
                    sharedPreferences.getString(enum.name, enum.getDefault() as String)
                }
                is Byte -> {
                    sharedPreferences.getInt(enum.name, (enum.getDefault() as Byte).toInt()).toByte()
                }
                is Long -> {
                    sharedPreferences.getLong(enum.name, enum.getDefault() as Long)
                }
                else -> {
                    throw Exception("undefined type")
                }
            }
        }

        @JvmStatic
        fun getStringValue(key: PreferenceKey) = preferencesMap[key] as String

        @JvmStatic
        fun getIntValue(key: PreferenceKey) = preferencesMap[key] as Int

        @JvmStatic
        fun getFloatValue(key: PreferenceKey) = preferencesMap[key] as Float

        @JvmStatic
        fun getByteValue(key: PreferenceKey) = preferencesMap[key] as Byte

        @JvmStatic
        fun getLongValue(key: PreferenceKey) = preferencesMap[key] as Long
    }

    private var sharedPreferences: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    init {
        loadPreferences(sharedPreferences)

        sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPreferences, key ->
            updatePreference(sharedPreferences, key)
        }
    }

    fun exists(key: PreferenceKey) = sharedPreferences.contains(key.name)

    fun removeAll() {
        sharedPreferences.edit().clear().commit()
    }

    fun remove(key: PreferenceKey) {
        sharedPreferences.edit().remove(key.name).commit()
    }

    fun storeString(key: PreferenceKey, value: String) {
        sharedPreferences.edit().putString(key.name, value).commit()
    }

    fun storeInt(key: PreferenceKey, value: Int) {
        sharedPreferences.edit().putInt(key.name, value).commit()
    }

    fun storeFloat(key: PreferenceKey, value: Float) {
        sharedPreferences.edit().putFloat(key.name, value).commit()
    }

    fun storeByte(key: PreferenceKey, value: Byte) {
        sharedPreferences.edit().putInt(key.name, value.toInt()).commit()
    }

    fun storeLong(key: PreferenceKey, value: Long) {
        sharedPreferences.edit().putLong(key.name, value).commit()
    }

    inner class Builder {
        private var editor: SharedPreferences.Editor = sharedPreferences.edit()

        fun setString(key: PreferenceKey, value: String): Builder {
            editor.putString(key.name, value)
            return this
        }

        fun setInt(key: PreferenceKey, value: Int): Builder {
            editor.putInt(key.name, value)
            return this
        }

        fun setFloat(key: PreferenceKey, value: Float): Builder {
            editor.putFloat(key.name, value)
            return this
        }

        fun setByte(key: PreferenceKey, value: Byte): Builder {
            editor.putInt(key.name, value.toInt())
            return this
        }

        fun setLong(key: PreferenceKey, value: Long): Builder {
            editor.putLong(key.name, value)
            return this
        }

        fun store() {
            editor.commit()
        }
    }

    enum class PreferenceKey(private var default: Any) {

        LAST_ACTUAL_TIMESTAMP(0L),
        LAST_ACTUAL_TEMPERATURE(0f),
        LAST_ACTUAL_HUMIDITY(0f),
        TEMPERATURE_UNIT(Constants.UNIT_FLAG_TEMPERATURE_CELSIUS),
        TEMPERATURE_SYMBOL("°C"),
        HUMIDITY_SYMBOL("%"),
        DATE_FORMAT(Constants.DATE_FORMAT_DEFAULT),
        TIME_FORMAT(Constants.TIME_FORMAT_DEFAULT),
        DATETIME_FORMAT(Constants.DATETIME_FORMAT_DEFAULT);

        fun getDefault() = default
    }

}