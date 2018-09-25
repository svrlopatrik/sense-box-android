package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.LiveData
import android.bluetooth.BluetoothProfile
import android.os.Bundle
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import sk.kotlin.sensebox.Constants
import sk.kotlin.sensebox.bl.PreferencesManager
import sk.kotlin.sensebox.bl.PreferencesManager.PreferenceKey
import sk.kotlin.sensebox.bl.bt.BleClient
import sk.kotlin.sensebox.bl.bt.BleResult
import sk.kotlin.sensebox.events.BleConnectionEvent
import sk.kotlin.sensebox.events.BleFailEvent
import sk.kotlin.sensebox.events.RxBus
import sk.kotlin.sensebox.events.SettingsChangedEvent
import sk.kotlin.sensebox.models.states.LiveFragmentState
import sk.kotlin.sensebox.utils.SingleLiveEvent
import sk.kotlin.sensebox.utils.ValueInterpreter
import javax.inject.Inject

/**
 * Created by Patrik Švrlo on 8.9.2018.
 */
class LiveFragmentViewModel @Inject constructor(
        private val bleClient: BleClient,
        private val prefs: PreferencesManager,
        private val rxBus: RxBus
) : BaseViewModel() {
    private val liveFragmentState = SingleLiveEvent<LiveFragmentState>()

    private val actualTimestamp = SingleLiveEvent<LiveFragmentState.Timestamp>()
    private val actualTemperature = SingleLiveEvent<LiveFragmentState.Temperature>()
    private val actualHumidity = SingleLiveEvent<LiveFragmentState.Humidity>()

    private val isReading = SingleLiveEvent<Boolean>()

    private var refreshActualDisposable: Disposable? = null

    override fun onViewCreated(savedInstanceState: Bundle?) {
        if (savedInstanceState == null) {
            addDisposable(bleClient.connectionState()
                    .filter { it == BluetoothProfile.STATE_DISCONNECTED }
                    .subscribeOn(Schedulers.newThread())
                    .subscribe { disposeActualDataRefreshing() }
            )

            addDisposable(rxBus.ofType(SettingsChangedEvent::class.java).subscribe {
                liveFragmentState.postValue(LiveFragmentState.Refresh)
            })
        }

        if (prefs.exists(PreferenceKey.LAST_ACTUAL_TIMESTAMP)) {
            val timestamp = PreferencesManager.getIntValue(PreferenceKey.LAST_ACTUAL_TIMESTAMP)
            actualTimestamp.value = createTimestampState(timestamp)
        }

        if (prefs.exists(PreferenceKey.LAST_ACTUAL_TEMPERATURE)) {
            val temperature = PreferencesManager.getFloatValue(PreferenceKey.LAST_ACTUAL_TEMPERATURE)
            actualTemperature.value = createTemperatureState(temperature)
        }

        if (prefs.exists(PreferenceKey.LAST_ACTUAL_HUMIDITY)) {
            val humidity = PreferencesManager.getFloatValue(PreferenceKey.LAST_ACTUAL_HUMIDITY)
            actualHumidity.value = createHumidityState(humidity)
        }

    }

    fun refreshActualData() {
        disposeActualDataRefreshing()

        refreshActualDisposable = bleClient.connect(Constants.BLE_DEVICE_MAC)
                .flatMap {
                    when (it) {
                        is BleResult.Connected -> bleClient.writeCharacteristic(Constants.BLE_UUID_SERVICE, Constants.BLE_UUID_CHARACTERISTIC, Constants.REQUEST_CODE_ACTUAL)
                        else -> Single.fromCallable {
                            rxBus.post(BleConnectionEvent(false))
                            it
                        }
                    }
                }
                .toFlowable()
                .flatMap {
                    when (it) {
                        BleResult.CharacteristicsWritten -> bleClient.notifyCharacteristics(Constants.BLE_UUID_SERVICE, Constants.BLE_UUID_CHARACTERISTIC, Constants.BLE_UUID_DESCRIPTOR)
                        else -> Flowable.fromCallable { it }
                    }
                }
                .doOnSubscribe {
                    if (!bleClient.isConnected()) {
                        rxBus.post(BleConnectionEvent(true))
                    }
                    isReading.postValue(true)
                }
                .doFinally { isReading.postValue(false) }
                .subscribeOn(Schedulers.newThread())
                .subscribe(
                        {
                            when (it) {
                                is BleResult.Success -> {
                                    when (it.data[0]) {
                                        Constants.RESPONSE_FLAG_TIMESTAMP -> {
                                            val timestamp = ValueInterpreter.byteArrayToInt(it.data.copyOfRange(1, 5))
                                            prefs.storeInt(PreferenceKey.LAST_ACTUAL_TIMESTAMP, timestamp)
                                            val timestampState = createTimestampState(timestamp)
                                            actualTimestamp.postValue(timestampState)
                                            liveFragmentState.postValue(timestampState)
                                        }
                                        Constants.RESPONSE_FLAG_TEMPERATURE -> {
                                            val temperature = ValueInterpreter.byteArrayToFloat(it.data.copyOfRange(1, 5))
                                            prefs.storeFloat(PreferenceKey.LAST_ACTUAL_TEMPERATURE, temperature)
                                            val temperatureState = createTemperatureState(temperature)
                                            actualTemperature.postValue(temperatureState)
                                            liveFragmentState.postValue(temperatureState)
                                        }
                                        Constants.RESPONSE_FLAG_HUMIDITY -> {
                                            val humidity = ValueInterpreter.byteArrayToFloat(it.data.copyOfRange(1, 5))
                                            prefs.storeFloat(PreferenceKey.LAST_ACTUAL_HUMIDITY, humidity)
                                            val humidityState = createHumidityState(humidity)
                                            actualHumidity.postValue(humidityState)
                                            liveFragmentState.postValue(humidityState)
                                        }
                                    }
                                }
                                is BleResult.Failure -> {
                                    liveFragmentState.postValue(LiveFragmentState.Error(it.bleFailState.name))
                                    rxBus.post(BleFailEvent(it.bleFailState.name))
                                }
                            }
                        },
                        { println("onError: ${it.message}") },
                        { println("onComplete") }
                ).also {
                    addDisposable(it)
                }
    }

    private fun disposeActualDataRefreshing() {
        refreshActualDisposable?.let {
            removeDisposable(it)
            refreshActualDisposable = null
        }
    }

    private fun createTimestampState(timestamp: Int): LiveFragmentState.Timestamp {
        return LiveFragmentState.Timestamp(ValueInterpreter.unixTimestampToMillis(timestamp))
    }

    private fun createTemperatureState(temperature: Float): LiveFragmentState.Temperature {
        val temperatureUnit = PreferencesManager.getByteValue(PreferenceKey.TEMPERATURE_UNIT)
        var temp = temperature
        if (temperatureUnit == Constants.UNIT_FLAG_TEMPERATURE_FAHRENHEIT) {
            temp = ValueInterpreter.celsiusToFahrenheit(temperature)
        }

        return LiveFragmentState.Temperature(temp)
    }

    private fun createHumidityState(humidity: Float): LiveFragmentState.Humidity {
        return LiveFragmentState.Humidity(humidity)
    }

    fun getLiveFragmentState(): LiveData<LiveFragmentState> = liveFragmentState

    fun getActualTimestamp(): LiveData<LiveFragmentState.Timestamp> = actualTimestamp
    fun getActualTemperature(): LiveData<LiveFragmentState.Temperature> = actualTemperature
    fun getActualHumidity(): LiveData<LiveFragmentState.Humidity> = actualHumidity

    fun getIsReading(): LiveData<Boolean> = isReading
}