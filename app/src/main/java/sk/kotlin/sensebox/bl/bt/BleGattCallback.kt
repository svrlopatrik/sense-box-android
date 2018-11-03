package sk.kotlin.sensebox.bl.bt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import io.reactivex.processors.PublishProcessor
import io.reactivex.subjects.PublishSubject
import sk.kotlin.sensebox.utils.ValueInterpreter
import timber.log.Timber

object BleGattCallback : BluetoothGattCallback() {

    val connectionChangedSubject: PublishSubject<ConnectionStateChanged> = PublishSubject.create()
    val serviceDiscoveredSubject: PublishSubject<ServiceDiscovered> = PublishSubject.create()
    val characteristicWrittenSubject: PublishSubject<CharacteristicWritten> = PublishSubject.create()
    val characteristicReadSubject: PublishSubject<CharacteristicRead> = PublishSubject.create()
    val characteristicChangedProcessor: PublishProcessor<CharacteristicChanged> = PublishProcessor.create()
    val descriptorWrittenSubject: PublishSubject<DescriptorWritten> = PublishSubject.create()

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        Timber.d("Gatt callback - connection changed")
        connectionChangedSubject.onNext(ConnectionStateChanged(gatt, status, newState))
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        Timber.d("Gatt callback - service discovered")
        Thread.sleep(200)
        serviceDiscoveredSubject.onNext(ServiceDiscovered(gatt, status))
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        Timber.d("Gatt callback - characteristic written")
        Thread.sleep(200)
        characteristicWrittenSubject.onNext(CharacteristicWritten(gatt, characteristic, status))
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        Timber.d("Gatt callback - characteristic read")
        characteristicReadSubject.onNext(CharacteristicRead(gatt, characteristic, status))
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        Timber.d("Gatt callback - characteristic changed")
        if (gatt != null && characteristic != null && characteristic.value.isNotEmpty()) {
            Timber.i("Notification received: [${ValueInterpreter.printByteArray(*characteristic.value)}]")
            characteristicChangedProcessor.onNext(CharacteristicChanged(gatt, characteristic))
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        Timber.d("Gatt callback - descriptor written")
        Thread.sleep(200)
        descriptorWrittenSubject.onNext(DescriptorWritten(gatt, descriptor, status))
    }

    data class ConnectionStateChanged(val gatt: BluetoothGatt?, val status: Int, val newState: Int)
    data class CharacteristicChanged(val gatt: BluetoothGatt, val characteristic: BluetoothGattCharacteristic)
    data class ServiceDiscovered(val gatt: BluetoothGatt?, val status: Int)
    data class CharacteristicWritten(val gatt: BluetoothGatt?, val characteristic: BluetoothGattCharacteristic?, val status: Int)
    data class CharacteristicRead(val gatt: BluetoothGatt?, val characteristic: BluetoothGattCharacteristic?, val status: Int)
    data class DescriptorWritten(val gatt: BluetoothGatt?, val descriptor: BluetoothGattDescriptor?, val status: Int)
}