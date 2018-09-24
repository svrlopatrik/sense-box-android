package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.LiveData
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import sk.kotlin.sensebox.Constants
import sk.kotlin.sensebox.bl.PreferencesManager
import sk.kotlin.sensebox.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class SettingsFragmentViewModel @Inject constructor(
        private val preferences: PreferencesManager
) : BaseViewModel() {

    private val temperatureUnit = SingleLiveEvent<String>()

    override fun onViewCreated(savedInstanceState: Bundle?) {
        temperatureUnit.value = PreferencesManager.getStringValue(PreferencesManager.PreferenceKey.TEMPERATURE_SYMBOL)
    }

    fun onTemperatureUnitChanged(radioGroup: RadioGroup, id: Int) {
        val text = radioGroup.findViewById<RadioButton>(id).text.toString()
        if (text != temperatureUnit.value) {
            temperatureUnit.value = text
            preferences.Builder()
                    .setString(PreferencesManager.PreferenceKey.TEMPERATURE_SYMBOL, text)
                    .setByte(PreferencesManager.PreferenceKey.TEMPERATURE_UNIT, getTemperatureUnitFromSymbol(text))
                    .store()
        }
    }

    private fun getTemperatureUnitFromSymbol(symbol: String) = when (symbol) {
        "°C" -> Constants.UNIT_FLAG_TEMPERATURE_CELSIUS
        "°F" -> Constants.UNIT_FLAG_TEMPERATURE_FAHRENHEIT
        else -> throw Exception("undefined temperature symbol")
    }

    fun getTemperatureUnit(): LiveData<String> = temperatureUnit
}