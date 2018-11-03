package sk.kotlin.sensebox.bl.repos

import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.functions.Function
import sk.kotlin.sensebox.Constants
import sk.kotlin.sensebox.bl.PreferencesManager
import sk.kotlin.sensebox.bl.PreferencesManager.PreferenceKey
import sk.kotlin.sensebox.bl.bt.BleClient
import sk.kotlin.sensebox.bl.bt.BleResult
import sk.kotlin.sensebox.models.ActualModel
import sk.kotlin.sensebox.utils.BufferUntil
import sk.kotlin.sensebox.utils.ValueInterpreter

class ActualRepository(
        bleClient: BleClient,
        private val preferencesManager: PreferencesManager
) : BaseRepository(bleClient) {

    fun refreshActualData(): Single<BleResult> {
        return bleClient.sendCommand(Constants.BLE_DEVICE_MAC,
                Constants.BLE_UUID_SERVICE,
                Constants.BLE_UUID_CHARACTERISTIC,
                Constants.BLE_UUID_DESCRIPTOR,
                Constants.REQUEST_CODE_ACTUAL
        )
    }

    fun getActualData(): Flowable<ActualModel> {
        return Flowable.concat(loadActualData(),
                bleClient.onDataReceived()
                        .filter { it.requestCode == Constants.REQUEST_CODE_ACTUAL }
                        .flatMap { Flowable.fromCallable { it.data } }
                        .lift(BufferUntil<ByteArray, ActualModel>(3,
                                Function { it.contentEquals(Constants.RESPONSE_FLAG_END) || it.contentEquals(Constants.RESPONSE_FLAG_UDEF) },
                                Function { parseActualData(it) }
                        ))
                        .flatMap {
                            storeActualData(it.first())
                            Flowable.fromCallable { it.first() }
                        }
        )
    }

    private fun parseActualData(data: List<ByteArray>): ActualModel {
        val actualModel = ActualModel()
        for (bytes in data) {
            when (bytes[0]) {
                Constants.RESPONSE_FLAG_TIMESTAMP -> actualModel.timestamp = ValueInterpreter.unixTimestampToMillis(ValueInterpreter.byteArrayToInt(bytes.copyOfRange(1, 5)))
                Constants.RESPONSE_FLAG_TEMPERATURE -> actualModel.temperature = ValueInterpreter.byteArrayToFloat(bytes.copyOfRange(1, 5))
                Constants.RESPONSE_FLAG_HUMIDITY -> actualModel.humidity = ValueInterpreter.byteArrayToFloat(bytes.copyOfRange(1, 5))
            }
        }

        return actualModel
    }

    private fun storeActualData(model: ActualModel) {
        preferencesManager.Builder()
                .setLong(PreferenceKey.LAST_ACTUAL_TIMESTAMP, model.timestamp!!)
                .setFloat(PreferenceKey.LAST_ACTUAL_TEMPERATURE, model.temperature!!)
                .setFloat(PreferenceKey.LAST_ACTUAL_HUMIDITY, model.humidity!!)
                .store()
    }

    private fun loadActualData(): Flowable<ActualModel> {
        return Flowable.create({ emitter ->
            if (preferencesManager.exists(PreferenceKey.LAST_ACTUAL_TIMESTAMP) &&
                    preferencesManager.exists(PreferenceKey.LAST_ACTUAL_TEMPERATURE) &&
                    preferencesManager.exists(PreferenceKey.LAST_ACTUAL_HUMIDITY)) {

                emitter.onNext(ActualModel().apply {
                    timestamp = PreferencesManager.getLongValue(PreferenceKey.LAST_ACTUAL_TIMESTAMP)
                    temperature = PreferencesManager.getFloatValue(PreferenceKey.LAST_ACTUAL_TEMPERATURE)
                    humidity = PreferencesManager.getFloatValue(PreferenceKey.LAST_ACTUAL_HUMIDITY)
                })
            }

            emitter.onComplete()
        }, BackpressureStrategy.BUFFER)
    }
}