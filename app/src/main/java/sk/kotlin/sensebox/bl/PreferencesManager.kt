package sk.kotlin.sensebox.bl

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

/**
 * Created by Patrik Švrlo on 15.9.2018.
 */
@SuppressLint("ApplySharedPref")
class PreferencesManager(
        private val context: Context,
        private val name: String) {

    private var preferences: SharedPreferences = context.getSharedPreferences(name, Context.MODE_PRIVATE)

    fun exists(key: PreferenceKey): Boolean {
        return preferences.contains(key.name)
    }

    fun removeAll() {
        preferences.edit().clear().commit()
    }

    fun remove(key: PreferenceKey) {
        preferences.edit().remove(key.name).commit()
    }

    fun getString(key: PreferenceKey, default: String = ""): String {
        return preferences.getString(key.name, default)
    }

    fun getInt(key: PreferenceKey, default: Int = -1): Int {
        return preferences.getInt(key.name, default)
    }

    fun getFloat(key: PreferenceKey, default: Float = -1f): Float {
        return preferences.getFloat(key.name, default)
    }

    fun getByte(key: PreferenceKey, default: Byte = 0xFF.toByte()): Byte {
        return preferences.getInt(key.name, default.toInt()).toByte()
    }

    fun storeString(key: PreferenceKey, value: String) {
        preferences.edit().putString(key.name, value).commit()
    }

    fun storeInt(key: PreferenceKey, value: Int) {
        preferences.edit().putInt(key.name, value).commit()
    }

    fun storeFloat(key: PreferenceKey, value: Float) {
        preferences.edit().putFloat(key.name, value).commit()
    }

    fun storeByte(key: PreferenceKey, value: Byte) {
        preferences.edit().putInt(key.name, value.toInt()).commit()
    }

    inner class Builder {
        private var editor: SharedPreferences.Editor = preferences.edit()

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

        fun store() {
            editor.commit()
        }
    }

    enum class PreferenceKey {
        LAST_ACTUAL_TIMESTAMP,
        LAST_ACTUAL_TEMPERATURE,
        LAST_ACTUAL_HUMIDITY,
        UNIT_TEMPERATURE,
        TIME_FORMAT
    }

}