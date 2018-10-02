package sk.kotlin.sensebox.bl.vm

import android.arch.lifecycle.LiveData
import android.bluetooth.BluetoothProfile
import android.os.Bundle
import io.reactivex.Flowable
import io.reactivex.android.schedulers.AndroidSchedulers
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
import sk.kotlin.sensebox.utils.runOnUiThread
import timber.log.Timber
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
        if (!isInitialized) {
            addDisposable(bleClient.connectionState()
                    .filter { it == BluetoothProfile.STATE_DISCONNECTED }
                    .subscribeOn(Schedulers.newThread())
                    .subscribe { disposeActualDataRefreshing() }
            )

            addDisposable(rxBus.ofType(SettingsChangedEvent::class.java)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe { liveFragmentState.value = LiveFragmentState.Refresh }
            )

            isInitialized = true
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
        Timber.i("Refresh actual data.")
        disposeActualDataRefreshing()

        refreshActualDisposable = bleClient.connect(Constants.BLE_DEVICE_MAC).toFlowable()
                .flatMap {
                    when (it) {
                        is BleResult.Connected -> bleClient.notifyCharacteristics(Constants.BLE_UUID_SERVICE, Constants.BLE_UUID_CHARACTERISTIC, Constants.BLE_UUID_DESCRIPTOR)
                        else -> Flowable.fromCallable { it }
                    }
                }
                .flatMap {
                    when (it) {
                        is BleResult.CharacteristicNotified -> bleClient.writeCharacteristic(Constants.BLE_UUID_SERVICE, Constants.BLE_UUID_CHARACTERISTIC, Constants.REQUEST_CODE_ACTUAL).toFlowable()
                        else -> Flowable.fromCallable { it }
                    }
                }
                .doOnSubscribe {
                    if (!bleClient.isConnected()) {
                        rxBus.post(BleConnectionEvent(true))
                    }
                    runOnUiThread { isReading.value = true }
                }
                .doFinally { runOnUiThread { isReading.value = false } }
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        {
                            when (it) {
                                is BleResult.Success -> {
                                    when (it.data[0]) {
                                        Constants.RESPONSE_FLAG_TIMESTAMP -> {
                                            Timber.i("Timestamp received.")
                                            val timestamp = ValueInterpreter.byteArrayToInt(it.data.copyOfRange(1, 5))
                                            prefs.storeInt(PreferenceKey.LAST_ACTUAL_TIMESTAMP, timestamp)
                                            val timestampState = createTimestampState(timestamp)
                                            actualTimestamp.value = timestampState
                                            liveFragmentState.value = timestampState
                                        }
                                        Constants.RESPONSE_FLAG_TEMPERATURE -> {
                                            Timber.i("Temperature received.")
                                            val temperature = ValueInterpreter.byteArrayToFloat(it.data.copyOfRange(1, 5))
                                            prefs.storeFloat(PreferenceKey.LAST_ACTUAL_TEMPERATURE, temperature)
                                            val temperatureState = createTemperatureState(temperature)
                                            actualTemperature.value = temperatureState
                                            liveFragmentState.value = temperatureState
                                        }
                                        Constants.RESPONSE_FLAG_HUMIDITY -> {
                                            Timber.i("Humidity received.")
                                            val humidity = ValueInterpreter.byteArrayToFloat(it.data.copyOfRange(1, 5))
                                            prefs.storeFloat(PreferenceKey.LAST_ACTUAL_HUMIDITY, humidity)
                                            val humidityState = createHumidityState(humidity)
                                            actualHumidity.value = humidityState
                                            liveFragmentState.value = humidityState
                                        }
                                    }
                                }
                                is BleResult.Failure -> {
                                    Timber.e("Failure - ${it.bleFailState.name}")
                                    liveFragmentState.value = LiveFragmentState.Error(it.bleFailState.name)
                                    rxBus.post(BleFailEvent(it.bleFailState.name))
                                    if (!bleClient.isConnected()) {
                                        rxBus.post(BleConnectionEvent(false))
                                    }
                                }
                            }
                        },
                        { Timber.e(it, "Error refresh actual data.") },
                        { Timber.i("Refresh actual data completed.") }
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
        return LiveFragmentState.Temperature(temperature)
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