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
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single
import sk.kotlin.sensebox.Constants
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

class BleClient(val context: Context) {

    private val btManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    private var btAdapter: BluetoothAdapter? = null

    @Volatile
    private var btGatt: BluetoothGatt? = null
    @Volatile
    private var btDevice: BluetoothDevice? = null
    @Volatile
    private var requestCode: Byte = 0x00

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
            btManager.getConnectionState(it, BluetoothProfile.GATT) == BluetoothProfile.STATE_CONNECTED
        } ?: false
    }

    fun disconnect() {
        btGatt?.let {
            Timber.i("Disconnect from ble gatt server.")
            it.disconnect()
            it.close()
        }
    }

    fun isDiscovered() = btGatt?.services?.isNotEmpty() ?: false

    fun isGattReady() = isConnected() && isDiscovered()

    fun releaseConnection() {
        btGatt = null
        btDevice = null
    }

    private fun scanDevices(timeout: Long = 10, timeUnit: TimeUnit = TimeUnit.SECONDS): Observable<BleResult> {
        Timber.i("Scan ble device.")
        val scanObservable = if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            scanDevicesHigherApi(timeout, timeUnit)
        } else {
            scanDevicesLowApi(timeout, timeUnit)
        }

        return Observable.concat(scanObservable, Observable.fromCallable {
            Timber.e("Device not found.")
            BleResult.Failure(BleFailType.SCAN_FINISHED)
        })
    }

    private fun scanDevicesLowApi(timeout: Long, timeUnit: TimeUnit): Observable<BleResult> {
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
    private fun scanDevicesHigherApi(timeout: Long, timeUnit: TimeUnit): Observable<BleResult> {
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
                    emitter.onNext(BleResult.Failure(BleFailType.SCAN_ERROR))
                }
            }

            val scanFilters = emptyList<ScanFilter>()
            val scanSettings = ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_POWER).build()
            btAdapter?.bluetoothLeScanner?.startScan(scanFilters, scanSettings, scanCallback)
        }
                .doFinally { btAdapter?.bluetoothLeScanner?.stopScan(scanCallback) }
                .takeUntil(Observable.just(true).delay(timeout, timeUnit))
    }

    private fun connect(deviceMac: String, autoConnect: Boolean = false): Single<BleResult> {
        Timber.i("Connect device.")
        return when {
            !isEnabled() -> {
                Timber.e("Bluetooth not enabled.")
                Single.fromCallable { BleResult.Failure(BleFailType.BT_NOT_ENABLED) }
            }
            isGattReady() -> {
                Timber.i("Connected to device.")
                Single.fromCallable { BleResult.Connected }
            }
            btDevice != null -> reconnect(btDevice!!, autoConnect)
            else -> initialConnect(deviceMac, autoConnect)
        }
    }

    private fun initialConnect(deviceMac: String, autoConnect: Boolean): Single<BleResult> {
        return scanDevices(1, TimeUnit.MINUTES)
                .filter { it is BleResult.Failure || (it is BleResult.DeviceFound && it.bleDevice.address == deviceMac) }
                .firstOrError()
                .flatMap { scanResult ->
                    when (scanResult) {
                        is BleResult.DeviceFound -> {
                            Timber.i("Device found.")
                            reconnect(scanResult.bleDevice, autoConnect)
                        }
                        else -> Single.fromCallable { scanResult }
                    }
                }
    }

    private fun reconnect(device: BluetoothDevice, autoConnect: Boolean): Single<BleResult> {
        return bleGattCallback.connectionChangedSubject.firstOrError()
                .flatMap<BleResult> {
                    if (it.status == BluetoothGatt.GATT_SUCCESS && it.newState == BluetoothProfile.STATE_CONNECTED) {
                        Timber.i("Connected to device.")
                        btDevice = device
                        btGatt = it.gatt
                        discoverServices()
                    } else {
                        Timber.e("Cannot connect to device.")
                        Single.fromCallable { BleResult.Failure(BleFailType.CANNOT_CONNECT) }
                    }
                }.flatMap<BleResult> {
                    when (it) {
                        is BleResult.ServicesDiscovered -> Single.fromCallable { BleResult.Connected }
                        else -> Single.fromCallable { it }
                    }
                }
                .also {
                    //todo is it necessary
                    if (isConnected()) {
                        disconnect()
                    }
                    device.connectGatt(context, autoConnect, bleGattCallback)
                }
    }

    private fun discoverServices(): Single<BleResult> {
        Timber.i("Discover services.")
        return when {
            !isConnected() -> {
                Timber.e("Cannot discover services - not connected.")
                Single.fromCallable { BleResult.Failure(BleFailType.NOT_CONNECTED) }
            }
            btGatt?.discoverServices() == false -> {
                Timber.e("Cannot discover services.")
                Single.fromCallable { BleResult.Failure(BleFailType.CANNOT_DISCOVER_SERVICES) }
            }
            else -> bleGattCallback.serviceDiscoveredSubject.firstOrError()
                    .flatMap<BleResult> {
                        if (it.status == BluetoothGatt.GATT_SUCCESS && it.gatt != null) {
                            Timber.i("Services discovered.")
                            btGatt = it.gatt
                            Single.fromCallable { BleResult.ServicesDiscovered }
                        } else {
                            Timber.e("Services discovery failed!")
                            Single.fromCallable { BleResult.Failure(BleFailType.CANNOT_DISCOVER_SERVICES) }
                        }
                    }
        }
    }

    private fun writeCharacteristic(uuidService: UUID, uuidCharacteristic: UUID, vararg data: Byte): Single<BleResult> {
        Timber.i("Write characteristic.")
        val characteristic = btGatt?.getService(uuidService)?.getCharacteristic(uuidCharacteristic)
        characteristic?.value = data
        characteristic?.writeType = BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE

        return when {
            !isGattReady() -> {
                Timber.e("Cannot write characteristic - gatt not ready.")
                Single.fromCallable { BleResult.Failure(BleFailType.NOT_CONNECTED) }
            }
            btGatt?.writeCharacteristic(characteristic) == false -> {
                Timber.e("Cannot write characteristic!")
                Single.fromCallable { BleResult.Failure(BleFailType.CANNOT_WRITE_CHARACTERISTIC) }
            }
            else -> bleGattCallback.characteristicWrittenSubject.firstOrError()
                    .flatMap<BleResult> {
                        if (it.status == BluetoothGatt.GATT_SUCCESS) {
                            Timber.i("Characteristic written.")
                            Single.fromCallable { BleResult.CharacteristicsWritten }
                        } else {
                            Timber.e("Write characteristic fail!")
                            Single.fromCallable { BleResult.Failure(BleFailType.CANNOT_WRITE_CHARACTERISTIC) }
                        }
                    }
        }
    }

    private fun writeDescriptor(uuidService: UUID, uuidCharacteristic: UUID, uuidDescriptor: UUID, vararg flag: Byte): Single<BleResult> {
        Timber.i("Write descriptor.")
        val descriptor = btGatt?.getService(uuidService)?.getCharacteristic(uuidCharacteristic)?.getDescriptor(uuidDescriptor)
        descriptor?.value = flag

        return when {
            !isGattReady() -> {
                Timber.e("Cannot write descriptor - gatt not ready.")
                Single.fromCallable { BleResult.Failure(BleFailType.NOT_CONNECTED) }
            }
            btGatt?.writeDescriptor(descriptor) == false -> {
                Timber.e("Cannot write descriptor!")
                Single.fromCallable { BleResult.Failure(BleFailType.CANNOT_WRITE_DESCRIPTOR) }
            }
            else -> bleGattCallback.descriptorWrittenSubject.firstOrError()
                    .flatMap<BleResult> {
                        if (it.status == BluetoothGatt.GATT_SUCCESS) {
                            Timber.i("Descriptor written.")
                            Single.fromCallable { BleResult.DescriptorWritten }
                        } else {
                            Timber.e("Write descriptor failed!")
                            Single.fromCallable { BleResult.Failure(BleFailType.CANNOT_WRITE_DESCRIPTOR) }
                        }
                    }
        }
    }

    private fun notifyCharacteristics(uuidService: UUID, uuidCharacteristic: UUID, uuidDescriptor: UUID): Single<BleResult> {
        Timber.i("Notify characteristic.")
        return when {
            !isGattReady() -> {
                Timber.e("Cannot notify characteristic - gatt not ready.")
                Single.fromCallable { BleResult.Failure(BleFailType.NOT_CONNECTED) }
            }
            else -> writeDescriptor(uuidService, uuidCharacteristic, uuidDescriptor, *Constants.ENABLE_NOTIFICATIONS_INDICATIONS)
                    .flatMap {
                        when (it) {
                            is BleResult.DescriptorWritten -> {
                                val characteristic = btGatt?.getService(uuidService)?.getCharacteristic(uuidCharacteristic)
                                if (btGatt?.setCharacteristicNotification(characteristic, true) != false) {
                                    Timber.i("Characteristic notified.")
                                    Single.fromCallable { BleResult.CharacteristicNotified }
                                } else {
                                    Timber.i("Cannot notify characteristic.")
                                    Single.fromCallable { BleResult.Failure(BleFailType.CANNOT_NOTIFY_CHARACTERISTIC) }
                                }
                            }
                            else -> Single.fromCallable { it }
                        }
                    }
        }
    }


    fun onConnectionStateChanged(): Observable<Int> = bleGattCallback.connectionChangedSubject
            .flatMap {
                if (it.status == BluetoothGatt.GATT_SUCCESS) {
                    Observable.fromCallable { it.newState }
                } else {
                    Observable.fromCallable { BluetoothProfile.STATE_DISCONNECTED }
                }
            }


    fun sendCommand(macAddress: String, uuidService: UUID, uuidCharacteristic: UUID, uuidDescriptor: UUID, vararg data: Byte): Single<BleResult> {
        return connect(macAddress)
                .flatMap {
                    when (it) {
                        is BleResult.Connected -> notifyCharacteristics(uuidService, uuidCharacteristic, uuidDescriptor)
                        else -> Single.fromCallable { it }
                    }
                }
                .flatMap {
                    when (it) {
                        is BleResult.CharacteristicNotified -> writeCharacteristic(uuidService, uuidCharacteristic, *data)
                        else -> Single.fromCallable { it }
                    }
                }
                .flatMap {
                    if (it is BleResult.CharacteristicsWritten) {
                        requestCode = data[0]
                    }
                    Single.fromCallable { it }
                }
    }

    fun onDataReceived(): Flowable<BleResult.Success> {
        return bleGattCallback.characteristicChangedProcessor.onBackpressureBuffer()
                .flatMap { Flowable.fromCallable { BleResult.Success(requestCode, it.characteristic.value) } }
    }

}