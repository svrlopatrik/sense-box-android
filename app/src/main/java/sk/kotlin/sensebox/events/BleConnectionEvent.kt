package sk.kotlin.sensebox.events

/**
 * Created by Patrik Švrlo on 16.9.2018.
 */
data class BleConnectionEvent(val isConnecting: Boolean) : BaseEvent()