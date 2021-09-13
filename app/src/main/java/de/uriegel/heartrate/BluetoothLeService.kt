package de.uriegel.heartrate

import android.app.Service
import android.bluetooth.*
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.ParcelUuid
import android.util.Log
import java.util.*

class BluetoothLeService : Service() {
    override fun onBind(intent: Intent): IBinder = binder

    override fun onUnbind(intent: Intent?): Boolean {
        close()
        return super.onUnbind(intent)
    }

    fun initialize(): Boolean {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    fun connect(deviceAddress: String) =
        bluetoothAdapter?.let {
            try {
                val device = it.getRemoteDevice(deviceAddress)
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                true
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.")
                false
            }
            // connect to the GATT server on the device
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
            false
        }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            when (newState) {
                BluetoothProfile.STATE_CONNECTED -> {
                    connectionState = STATE_CONNECTED
                    broadcastUpdate(ACTION_GATT_CONNECTED)
                    bluetoothGatt?.discoverServices()
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    connectionState = STATE_DISCONNECTED
                    broadcastUpdate(ACTION_GATT_DISCONNECTED)
                }
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                bluetoothGatt?.services?.let {
                    val service = it.find { it.uuid == UUID.fromString(BluetoothLeService.HEART_RATE_UUID) }
                    service?.let {
                        val heartCharacteristics = it.characteristics?.find { it.uuid == UUID.fromString(BluetoothLeService.HEART_RATE_CHARACTERISTICS_ID) }
                        heartCharacteristics?.let {
                            val flag = it.properties
                            val format = when (flag and 0x01) {
                                0x01 -> {
                                    Log.d(TAG, "Heart rate format UINT16.")
                                    BluetoothGattCharacteristic.FORMAT_UINT16
                                }
                                else -> {
                                    Log.d(TAG, "Heart rate format UINT8.")
                                    BluetoothGattCharacteristic.FORMAT_UINT8
                                }
                            }
                            bluetoothGatt!!.setCharacteristicNotification(it, true)
                            // TODO: only Heart Rate
                            val descriptor = it.getDescriptor(UUID.fromString(BluetoothLeService.CLIENT_CHARACTERISTICS_ID))
                            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                            bluetoothGatt!!.writeDescriptor(descriptor)
                        }
                    }
                }
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
            val flag = characteristic.properties
            val format = when (flag and 0x01) {
                0x01 -> {
                    Log.d(TAG, "Heart rate format UINT16.")
                    BluetoothGattCharacteristic.FORMAT_UINT16
                }
                else -> {
                    Log.d(TAG, "Heart rate format UINT8.")
                    BluetoothGattCharacteristic.FORMAT_UINT8
                }
            }
            val heartRate = characteristic.getIntValue(format, 1)
            val test = heartRate + 9
        }
    }

    private fun broadcastUpdate(action: String) {
        val intent = Intent(action)
        sendBroadcast(intent)
    }

    private fun close() {
        bluetoothGatt?.let { gatt ->
            gatt.close()
            bluetoothGatt = null
        }
    }

    inner class LocalBinder : Binder() {
        fun getService() : BluetoothLeService {
            return this@BluetoothLeService
        }
    }

    companion object {
        const val HEART_RATE_UUID = "0000180D-0000-1000-8000-00805f9b34fb"
        const val BIKE_UUID = "00001816-0000-1000-8000-00805f9b34fb"
        const val HEART_RATE_CHARACTERISTICS_ID = "00002a37-0000-1000-8000-00805f9b34fb"
        const val BATTERY_CHARACTERISTICS_ID = "00002a38-0000-1000-8000-00805f9b34fb"
        const val CLIENT_CHARACTERISTICS_ID = "00002902-0000-1000-8000-00805f9b34fb"
        const val DEVICE_ADDRESS = "DEVICE_ADDRESS"
        const val ACTION_GATT_CONNECTED = "de.uriegel.heartrate.ACTION_GATT_CONNECTED"
        const val ACTION_GATT_DISCONNECTED = "de.uriegel.heartrate.ACTION_GATT_DISCONNECTED"
        private const val STATE_DISCONNECTED = 0
        private const val STATE_CONNECTED = 2
    }

    private val binder = LocalBinder()
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null
    private var connectionState = STATE_DISCONNECTED
}

private const val TAG = "BluetoothLeService"