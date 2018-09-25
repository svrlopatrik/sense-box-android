package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.LiveData
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import sk.kotlin.sensebox.Constants
import sk.kotlin.sensebox.R
import sk.kotlin.sensebox.bl.PreferencesManager
import sk.kotlin.sensebox.events.RxBus
import sk.kotlin.sensebox.events.SettingsChangedEvent
import sk.kotlin.sensebox.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class SettingsFragmentViewModel @Inject constructor(
        private val preferences: PreferencesManager,
        private val rxBus: RxBus
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
                    .setByte(PreferencesManager.PreferenceKey.TEMPERATURE_UNIT, getTemperatureUnitFromSymbol(id))
                    .store()
            rxBus.post(SettingsChangedEvent())
        }
    }

    private fun getTemperatureUnitFromSymbol(id: Int) = when (id) {
        R.id.button_celsius -> Constants.UNIT_FLAG_TEMPERATURE_CELSIUS
        R.id.button_fahrenheit -> Constants.UNIT_FLAG_TEMPERATURE_FAHRENHEIT
        else -> throw Exception("undefined temperature symbol")
    }

    fun getTemperatureUnit(): LiveData<String> = temperatureUnit
}