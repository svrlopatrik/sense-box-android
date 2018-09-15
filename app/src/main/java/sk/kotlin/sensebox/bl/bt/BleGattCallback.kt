package sk.kotlin.sensebox.bl.bt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor

object BleGattCallback : BluetoothGattCallback() {

    private val connectionChangeListeners = mutableListOf<ConnectionChangeListener>()
    private val characteristicReadListeners = mutableListOf<CharacteristicReadListener>()
    private val characteristicChangedListeners = mutableListOf<CharacteristicChangedListener>()
    private val characteristicWriteListeners = mutableListOf<CharacteristicWriteListener>()
    private val servicesDiscoveredListeners = mutableListOf<ServicesDiscoveredListener>()
    private val descriptorWriteListeners = mutableListOf<DescriptorWriteListener>()

    @Synchronized
    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        for (listener in connectionChangeListeners) {
            listener.onConnectionStateChange(gatt, status, newState)
        }
    }

    @Synchronized
    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        for (listener in characteristicReadListeners) {
            listener.onCharacteristicRead(gatt, characteristic, status)
        }
    }

    @Synchronized
    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        for (listener in characteristicChangedListeners) {
            listener.onCharacteristicChanged(gatt, characteristic)
        }
    }

    @Synchronized
    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        for (listener in characteristicWriteListeners) {
            listener.onCharacteristicWrite(gatt, characteristic, status)
        }
    }

    @Synchronized
    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        for (listener in servicesDiscoveredListeners) {
            listener.onServicesDiscovered(gatt, status)
        }
    }

    @Synchronized
    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        for (listener in descriptorWriteListeners) {
            listener.onDescriptorWrite(gatt, descriptor, status)
        }

    }

    @Synchronized
    fun addConnectionChangeListener(connectionChangeListener: ConnectionChangeListener) {
        connectionChangeListeners.add(connectionChangeListener)
    }

    @Synchronized
    fun addCharacteristicReadListener(characteristicReadListener: CharacteristicReadListener) {
        characteristicReadListeners.add(characteristicReadListener)
    }

    @Synchronized
    fun addCharacteristicChangedListener(characteristicChangedListener: CharacteristicChangedListener) {
        characteristicChangedListeners.add(characteristicChangedListener)
    }

    @Synchronized
    fun addCharacteristicWriteListener(characteristicWriteListener: CharacteristicWriteListener) {
        characteristicWriteListeners.add(characteristicWriteListener)
    }

    @Synchronized
    fun addServicesDiscoveredListener(servicesDiscoveredListener: ServicesDiscoveredListener) {
        servicesDiscoveredListeners.add(servicesDiscoveredListener)
    }

    @Synchronized
    fun addDescriptorWriteListener(descriptorWriteListener: DescriptorWriteListener) {
        descriptorWriteListeners.add(descriptorWriteListener)
    }

    @Synchronized
    fun removeConnectionChangeListener(connectionChangeListener: ConnectionChangeListener) {
        connectionChangeListeners.remove(connectionChangeListener)
    }

    @Synchronized
    fun removeCharacteristicReadListener(characteristicReadListener: CharacteristicReadListener) {
        characteristicReadListeners.remove(characteristicReadListener)
    }

    @Synchronized
    fun removeCharacteristicChangedListener(characteristicChangedListener: CharacteristicChangedListener) {
        characteristicChangedListeners.remove(characteristicChangedListener)
    }

    @Synchronized
    fun removeCharacteristicWriteListener(characteristicWriteListener: CharacteristicWriteListener) {
        characteristicWriteListeners.remove(characteristicWriteListener)
    }

    @Synchronized
    fun removeServicesDiscoveredListener(servicesDiscoveredListener: ServicesDiscoveredListener) {
        servicesDiscoveredListeners.remove(servicesDiscoveredListener)
    }

    @Synchronized
    fun removeDescriptorWriteListener(descriptorWriteListener: DescriptorWriteListener) {
        descriptorWriteListeners.remove(descriptorWriteListener)
    }

    @Synchronized
    fun release() {
        connectionChangeListeners.clear()
        characteristicReadListeners.clear()
        characteristicChangedListeners.clear()
        characteristicWriteListeners.clear()
        servicesDiscoveredListeners.clear()
    }

    interface ConnectionChangeListener {
        fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int)
    }

    interface CharacteristicReadListener {
        fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int)
    }

    interface CharacteristicChangedListener {
        fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?)
    }

    interface CharacteristicWriteListener {
        fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int)
    }

    interface ServicesDiscoveredListener {
        fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int)
    }

    interface DescriptorWriteListener {
        fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int)
    }
}