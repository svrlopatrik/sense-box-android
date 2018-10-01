package sk.kotlin.sensebox.bl.bt

import android.app.Activity
import android.bluetooth.*
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.Fragment
import io.reactivex.*
import io.reactivex.Observable
import sk.kotlin.sensebox.Constants
import sk.kotlin.sensebox.utils.ValueInterpreter
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class BleClient(val context: Context) {

    private val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var btAdapter: BluetoothAdapter? = null

    private var btGatt: BluetoothGatt? = null
    private var btDevice: BluetoothDevice? = null

    private val bleGattCallback: BleGattCallback

    init {
        btAdapter = btManager.adapter

        bleGattCallback = BleGattCallback
    }

    fun isBleSupported() = context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)

    fun requestEnableBluetooth(activity: Activity, requestCode: Int = 0x01) {
        val btIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        activity.startActivityForResult(btIntent, requestCode)
    }

    fun requestEnableBluetooth(fragment: Fragment, requestCode: Int = 0x01) {
        val btIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        fragment.startActivityForResult(btIntent, requestCode)
    }

    fun isEnabled() = btAdapter?.isEnabled ?: false

    fun enableBluetooth(): Boolean {
        return btAdapter?.let {
            return if (!isEnabled()) {
                return it.enable()
            } else true
        } ?: false
    }

    fun disableBluetooth(): Boolean {
        return btAdapter?.let {
            return if (isEnabled()) {
                return it.disable()
            } else true
        } ?: false
    }

    fun isConnected(): Boolean {
        return btDevice?.let {
            btGatt?.services?.isNotEmpty() ?: false && btManager.getConnectionState(it, BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED
        } ?: false
    }


    fun disconnect() {
        btGatt?.let {
            Timber.i("Disconnect from ble gatt server.")
            it.disconnect()
            it.close()
        }
    }

    fun releaseConnection() {
        btGatt = null
        btDevice = null
    }

    fun scanDevices(timeout: Long = 10, timeUnit: TimeUnit = TimeUnit.SECONDS): Observable<BleResult> {
        Timber.i("Scan ble device.")
        return if (isEnabled()) {
            val scanObservable = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
                scanDevicesHigherApi(timeout, timeUnit)
            } else {
                scanDevicesLowApi(timeout, timeUnit)
            }

            Observable.concat(scanObservable, Observable.fromCallable {
                Timber.e("Device not found.")
                BleResult.Failure(BleFailState.SCAN_FINISHED)
            })
        } else {
            Observable.fromCallable {
                Timber.e("Bluetooth not enabled.")
                BleResult.Failure(BleFailState.BT_NOT_ENABLED)
            }
        }
    }

    private fun scanDevicesLowApi(timeout: Long = 10, timeUnit: TimeUnit = TimeUnit.SECONDS): Observable<BleResult> {
        var scanCallback: BluetoothAdapter.LeScanCallback? = null
        return Observable.create<BleResult> { emitter ->
            scanCallback = BluetoothAdapter.LeScanCallback { bluetoothDevice, _, _ ->
                emitter.onNext(BleResult.DeviceFound(bluetoothDevice))
            }

            btAdapter?.startLeScan(scanCallback)
        }
                .doFinally { btAdapter?.stopLeScan(scanCallback) }
                .takeUntil(Observable.just(true).delay(timeout, timeUnit))

    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun scanDevicesHigherApi(timeout: Long = 10, timeUnit: TimeUnit = TimeUnit.SECONDS): Observable<BleResult> {
        var scanCallback: ScanCallback? = null
        return Observable.create<BleResult> { emitter ->
            scanCallback = object : ScanCallback() {
                override fun onScanResult(callbackType: Int, result: ScanResult?) {
                    result?.let {
                        emitter.onNext(BleResult.DeviceFound(it.device))
                    }
                }

                override fun onBatchScanResults(results: MutableList<ScanResult>?) {
                    results?.let { r -> r.forEach { emitter.onNext(BleResult.DeviceFound(it.device)) } }
                }

                override fun onScanFailed(errorCode: Int) {
                    emitter.onNext(BleResult.Failure(BleFailState.SCAN_ERROR))
                }
            }

            val scanFilters = emptyList<ScanFilter>()
            val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
            btAdapter?.bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
        }.doFinally { btAdapter?.bluetoothLeScanner?.stopScan(scanCallback) }
                .takeUntil(Observable.just(true).delay(timeout, timeUnit))
    }

    fun connect(deviceMac: String, autoConnect: Boolean = false): Single<BleResult> {
        Timber.i("Connect device.")
        return when {
            isConnected() -> Single.fromCallable { BleResult.Connected }
            btDevice != null -> reconnect(btDevice!!, autoConnect)
            else -> initialConnect(deviceMac, autoConnect)
        }
    }

    private fun initialConnect(deviceMac: String, autoConnect: Boolean): Single<BleResult> {
        return scanDevices(1, TimeUnit.MINUTES)
                .filter { it is BleResult.Failure || (it is BleResult.DeviceFound && it.bleDevice.address == deviceMac) }
                .take(1)
                .flatMapSingle { scanResult ->
                    when (scanResult) {
                        is BleResult.DeviceFound -> {
                            Timber.i("Device found.")
                            reconnect(scanResult.bleDevice, autoConnect)
                        }
                        else -> Single.fromCallable { scanResult }
                    }
                }
                .singleOrError()
    }

    private fun reconnect(device: BluetoothDevice, autoConnect: Boolean): Single<BleResult> {
        Timber.i("Connecting device.")
        var connectionChangeListener: BleGattCallback.ConnectionChangeListener? = null
        return Single.create<BleResult> { emitter ->
            connectionChangeListener = object : BleGattCallback.ConnectionChangeListener {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
                            Timber.i("Connected to device.")
                            btDevice = device
                            btGatt = gatt
                            emitter.onSuccess(BleResult.Connected)
                        }
                    } else {
                        Timber.e("Cannot connect to device.")
                        emitter.onSuccess(BleResult.Failure(BleFailState.CANNOT_CONNECT))
                        disconnect()
                    }
                }
            }

            connectionChangeListener?.let { bleGattCallback.addConnectionChangeListener(it) }
            disconnect()
            device.connectGatt(context, autoConnect, bleGattCallback)
        }.flatMap { connectionResult ->
            when (connectionResult) {
                is BleResult.Connected -> discoverServices()
                else -> Single.fromCallable { connectionResult }
            }
        }.flatMap { servicesDiscoveryResult ->
            when (servicesDiscoveryResult) {
                is BleResult.ServicesDiscovered -> Single.fromCallable { BleResult.Connected }
                else -> Single.fromCallable { servicesDiscoveryResult }
            }
        }.doFinally { connectionChangeListener?.let { bleGattCallback.removeConnectionChangeListener(it) } }
    }

    private fun discoverServices(): Single<BleResult> {
        return btGatt?.let { gatt ->
            var servicesDiscoveredListener: BleGattCallback.ServicesDiscoveredListener? = null
            return Single.create<BleResult> { emitter ->
                servicesDiscoveredListener = object : BleGattCallback.ServicesDiscoveredListener {
                    override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
                        if (status == BluetoothGatt.GATT_SUCCESS && gatt != null) {
                            Timber.i("Services discovered.")
                            btGatt = gatt
                            emitter.onSuccess(BleResult.ServicesDiscovered)
                        } else {
                            Timber.e("Services discovery failed!")
                            emitter.onSuccess(BleResult.Failure(BleFailState.CANNOT_DISCOVER_SERVICES))
                        }
                    }
                }

                Timber.i("Discover services.")
                if (gatt.discoverServices()) {
                    servicesDiscoveredListener?.let { bleGattCallback.addServicesDiscoveredListener(it) }
                } else {
                    Timber.e("Cannot discover services.")
                    emitter.onSuccess(BleResult.Failure(BleFailState.CANNOT_DISCOVER_SERVICES))
                }
            }.doFinally { servicesDiscoveredListener?.let { bleGattCallback.removeServicesDiscoveredListener(it) } }
        } ?: return Single.fromCallable {
            Timber.e("Cannot discover services - not connected.")
            BleResult.Failure(BleFailState.NOT_CONNECTED)
        }
    }

    fun writeCharacteristic(uuidService: UUID, uuidCharacteristic: UUID, vararg data: Byte): Single<BleResult> {
        if (!isConnected()) {
            Timber.e("Cannot write characteristic - not connected.")
            return Single.fromCallable { BleResult.Failure(BleFailState.NOT_CONNECTED) }
        }

        var characteristicWriteListener: BleGattCallback.CharacteristicWriteListener? = null
        return Single.create<BleResult> { emitter ->
            btGatt?.let { btGatt ->
                Timber.i("Write characteristic [${ValueInterpreter.printByteArray(*data)}].")
                val characteristic = btGatt.getService(uuidService).getCharacteristic(uuidCharacteristic)
                characteristic.value = data
                characteristic.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE
                if (btGatt.writeCharacteristic(characteristic)) {

                    characteristicWriteListener = object : BleGattCallback.CharacteristicWriteListener {
                        override fun onCharacteristicWrite(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?, status: Int) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                Timber.i("Characteristic written.")
                                emitter.onSuccess(BleResult.CharacteristicsWritten)
                            } else {
                                Timber.e("Write characteristic fail!")
                                emitter.onSuccess(BleResult.Failure(BleFailState.CANNOT_WRITE_CHARACTERISTIC))
                            }
                        }
                    }

                    characteristicWriteListener?.let { bleGattCallback.addCharacteristicWriteListener(it) }

                } else {
                    Timber.e("Cannot write characteristic!")
                    emitter.onSuccess(BleResult.Failure(BleFailState.CANNOT_WRITE_CHARACTERISTIC))
                }
            }
        }.doFinally { characteristicWriteListener?.let { bleGattCallback.removeCharacteristicWriteListener(it) } }
    }

    private fun writeDescriptor(uuidService: UUID, uuidCharacteristic: UUID, uuidDescriptor: UUID, vararg flag: Byte): Single<BleResult> {
        if (!isConnected()) {
            Timber.e("Cannot write descriptor - not connected.")
            return Single.fromCallable { BleResult.Failure(BleFailState.NOT_CONNECTED) }
        }

        var descriptorWriteListener: BleGattCallback.DescriptorWriteListener? = null
        return Single.create<BleResult> { emitter ->
            btGatt?.let { btGatt ->
                Timber.i("Write descriptor [${ValueInterpreter.printByteArray(*flag)}].")
                val descriptor = btGatt.getService(uuidService).getCharacteristic(uuidCharacteristic).getDescriptor(uuidDescriptor)
                descriptor.value = flag
                if (btGatt.writeDescriptor(descriptor)) {
                    descriptorWriteListener = object : BleGattCallback.DescriptorWriteListener {
                        override fun onDescriptorWrite(gatt: BluetoothGatt?, descriptor: BluetoothGattDescriptor?, status: Int) {
                            if (status == BluetoothGatt.GATT_SUCCESS) {
                                Timber.i("Descriptor written.")
                                emitter.onSuccess(BleResult.DescriptorWritten)
                            } else {
                                Timber.e("Write descriptor failed!")
                                emitter.onSuccess(BleResult.Failure(BleFailState.CANNOT_WRITE_DESCRIPTOR))
                            }
                        }
                    }
                    descriptorWriteListener?.let { bleGattCallback.addDescriptorWriteListener(it) }
                } else {
                    Timber.e("Cannot write descriptor!")
                    emitter.onSuccess(BleResult.Failure(BleFailState.CANNOT_WRITE_DESCRIPTOR))
                }
            }

        }.doFinally { descriptorWriteListener?.let { bleGattCallback.removeDescriptorWriteListener(it) } }
    }

    fun notifyCharacteristics(uuidService: UUID, uuidCharacteristic: UUID, uuidDescriptor: UUID): Flowable<BleResult> {
        if (!isConnected()) {
            Timber.e("Cannot notify characteristic - not connected.")
            return Flowable.fromCallable { BleResult.Failure(BleFailState.NOT_CONNECTED) }
        }

        return writeDescriptor(uuidService, uuidCharacteristic, uuidDescriptor, *Constants.ENABLE_NOTIFICATIONS_INDICATIONS)
                .toFlowable()
                .flatMap { descWriteResult ->
                    when (descWriteResult) {
                        is BleResult.Failure -> Flowable.fromCallable { descWriteResult }
                        else -> {

                            if (!isConnected()) {
                                Timber.e("Cannot notify descriptor - not connected.")
                                Flowable.fromCallable { BleResult.Failure(BleFailState.NOT_CONNECTED) }
                            } else {
                                btGatt?.let { btGatt ->
                                    Timber.i("Notify characteristic.")
                                    var characteristicChangedListener: BleGattCallback.CharacteristicChangedListener? = null
                                    Flowable.create(FlowableOnSubscribe<BleResult> { emitter ->

                                        val characteristic = btGatt.getService(uuidService).getCharacteristic(uuidCharacteristic)
                                        if (btGatt.setCharacteristicNotification(characteristic, true)) {
                                            characteristicChangedListener = object : BleGattCallback.CharacteristicChangedListener {
                                                override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
                                                    characteristic?.let {
                                                        val value = it.value
                                                        value?.let {
                                                            Timber.i("Notification received: [${ValueInterpreter.printByteArray(*value)}]")

                                                            when {
                                                                it.contentEquals(Constants.RESPONSE_FLAG_END) -> emitter.onComplete()
                                                                it.contentEquals(Constants.RESPONSE_FLAG_UDEF) -> {
                                                                    emitter.onNext(BleResult.Failure(BleFailState.UNDEFINED_RESPONSE))
                                                                    emitter.onComplete()
                                                                }
                                                                else -> emitter.onNext(BleResult.Success(value))
                                                            }
                                                        } ?: emitter.onComplete()
                                                    }
                                                }
                                            }
                                            Timber.i("Characteristic notified.")
                                            characteristicChangedListener?.let { bleGattCallback.addCharacteristicChangedListener(it) }
                                            emitter.onNext(BleResult.CharacteristicNotified)
                                        } else {
                                            Timber.i("Cannot notify characteristic.")
                                            emitter.onNext(BleResult.Failure(BleFailState.CANNOT_NOTIFY_CHARACTERISTIC))
                                        }

                                    }, BackpressureStrategy.BUFFER).doFinally { characteristicChangedListener?.let { bleGattCallback.removeCharacteristicChangedListener(it) } }
                                }
                            }
                        }
                    }
                }
    }

    fun connectionState(): Observable<Int> {
        var connectionChangeListener: BleGattCallback.ConnectionChangeListener? = null
        return Observable.create<Int> { emitter ->
            connectionChangeListener = object : BleGattCallback.ConnectionChangeListener {
                override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
                    emitter.onNext(newState)
                }
            }
            connectionChangeListener?.let { bleGattCallback.addConnectionChangeListener(it) }
        }.doFinally { connectionChangeListener?.let { bleGattCallback.removeConnectionChangeListener(it) } }
    }

}