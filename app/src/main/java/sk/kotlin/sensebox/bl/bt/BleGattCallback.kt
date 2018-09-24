package sk.kotlin.sensebox.bl.bt

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import java.util.concurrent.CopyOnWriteArrayList

object BleGattCallback : BluetoothGattCallback() {

    private val connectionChangeListeners = CopyOnWriteArrayList<ConnectionChangeListener>()
    private val characteristicReadListeners = CopyOnWriteArrayList<CharacteristicReadListener>()
    private val characteristicChangedListeners = CopyOnWriteArrayList<CharacteristicChangedListener>()
    private val characteristicWriteListeners = CopyOnWriteArrayList<CharacteristicWriteListener>()
    private val servicesDiscoveredListeners = CopyOnWriteArrayList<ServicesDiscoveredListener>()
    private val descriptorWriteListeners = CopyOnWriteArrayList<DescriptorWriteListener>()

    override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
        synchronized(connectionChangeListeners) {
            for (listener in connectionChangeListeners) {
                listener.onConnectionStateChange(gatt, status, newState)
            }
        }
    }

    override fun onCharacteristicRead(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        synchronized(characteristicReadListeners) {
            for (listener in characteristicReadListeners) {
                listener.onCharacteristicRead(gatt, characteristic, status)
            }
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
        synchronized(characteristicChangedListeners) {
            for (listener in characteristicChangedListeners) {
                listener.onCharacteristicChanged(gatt, characteristic)
            }
        }
    }

    override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
        synchronized(characteristicWriteListeners) {
            for (listener in characteristicWriteListeners) {
                listener.onCharacteristicWrite(gatt, characteristic, status)
            }
        }
    }

    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
        synchronized(servicesDiscoveredListeners) {
            for (listener in servicesDiscoveredListeners) {
                listener.onServicesDiscovered(gatt, status)
            }
        }
    }

    override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
        synchronized(descriptorWriteListeners) {
            for (listener in descriptorWriteListeners) {
                listener.onDescriptorWrite(gatt, descriptor, status)
            }
        }

    }

    fun addConnectionChangeListener(connectionChangeListener: ConnectionChangeListener) {
        synchronized(connectionChangeListeners) {
            connectionChangeListeners.add(connectionChangeListener)
        }
    }

    fun addCharacteristicReadListener(characteristicReadListener: CharacteristicReadListener) {
        synchronized(characteristicReadListeners) {
            characteristicReadListeners.add(characteristicReadListener)
        }
    }

    fun addCharacteristicChangedListener(characteristicChangedListener: CharacteristicChangedListener) {
        synchronized(characteristicChangedListeners) {
            characteristicChangedListeners.add(characteristicChangedListener)
        }
    }

    fun addCharacteristicWriteListener(characteristicWriteListener: CharacteristicWriteListener) {
        synchronized(characteristicWriteListeners) {
            characteristicWriteListeners.add(characteristicWriteListener)
        }
    }

    fun addServicesDiscoveredListener(servicesDiscoveredListener: ServicesDiscoveredListener) {
        synchronized(servicesDiscoveredListeners) {
            servicesDiscoveredListeners.add(servicesDiscoveredListener)
        }
    }

    fun addDescriptorWriteListener(descriptorWriteListener: DescriptorWriteListener) {
        synchronized(descriptorWriteListeners) {
            descriptorWriteListeners.add(descriptorWriteListener)
        }
    }

    fun removeConnectionChangeListener(connectionChangeListener: ConnectionChangeListener) {
        synchronized(connectionChangeListeners) {
            connectionChangeListeners.remove(connectionChangeListener)
        }
    }

    fun removeCharacteristicReadListener(characteristicReadListener: CharacteristicReadListener) {
        synchronized(characteristicReadListeners) {
            characteristicReadListeners.remove(characteristicReadListener)
        }
    }

    fun removeCharacteristicChangedListener(characteristicChangedListener: CharacteristicChangedListener) {
        synchronized(characteristicChangedListeners) {
            characteristicChangedListeners.remove(characteristicChangedListener)
        }
    }

    fun removeCharacteristicWriteListener(characteristicWriteListener: CharacteristicWriteListener) {
        synchronized(characteristicWriteListeners) {
            characteristicWriteListeners.remove(characteristicWriteListener)
        }
    }

    fun removeServicesDiscoveredListener(servicesDiscoveredListener: ServicesDiscoveredListener) {
        synchronized(servicesDiscoveredListeners) {
            servicesDiscoveredListeners.remove(servicesDiscoveredListener)
        }
    }

    fun removeDescriptorWriteListener(descriptorWriteListener: DescriptorWriteListener) {
        synchronized(descriptorWriteListeners) {
            descriptorWriteListeners.remove(descriptorWriteListener)
        }
    }

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