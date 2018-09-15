package sk.kotlin.sensebox.bl.bt

import android.bluetooth.BluetoothDevice

sealed class BleResult {

    data class Success(val data: ByteArray) : BleResult()
    data class Failure(val bleFailState: BleFailState) : BleResult()
    data class DeviceFound(val bleDevice: BluetoothDevice) : BleResult()

    object Connected : BleResult()
    object CharacteristicsWritten : BleResult()
    object ServicesDiscovered : BleResult()
    object DescriptorWritten : BleResult()
}