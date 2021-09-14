package de.uriegel.heartrate

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Intent
import android.util.Log
import java.util.*

class HeartRateService : BluetoothLeService() {
    override fun getUuid() = HEART_RATE_UUID

    override fun discoverService(bluetoothGatt: BluetoothGatt, service: BluetoothGattService) {
        val heartCharacteristics = service.characteristics?.find { it.uuid == UUID.fromString(HEART_RATE_CHARACTERISTICS_ID) }
        heartCharacteristics?.let {
            bluetoothGatt.setCharacteristicNotification(it, true)
            val descriptor = it.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTICS_ID))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            bluetoothGatt.writeDescriptor(descriptor)
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val flag = characteristic.properties
        val format = when (flag and 0x01) {
            0x01 -> {
                Log.d(getTag(), "Heart rate format UINT16.")
                BluetoothGattCharacteristic.FORMAT_UINT16
            }
            else -> {
                Log.d(getTag(), "Heart rate format UINT8.")
                BluetoothGattCharacteristic.FORMAT_UINT8
            }
        }
        val heartRate = characteristic.getIntValue(format, 1)
        broadcastHeartRateUpdate(heartRate)
    }

    private fun broadcastHeartRateUpdate(rate: Int) {
        val intent = Intent(ACTION_GATT_HEART_RATE)
        intent.putExtra(HEART_RATE, rate)
        sendBroadcast(intent)
    }

    override fun getTag() = "HR"

    companion object {
        const val HEART_RATE_UUID = "0000180D-0000-1000-8000-00805f9b34fb"
        const val HEART_RATE_CHARACTERISTICS_ID = "00002a37-0000-1000-8000-00805f9b34fb"
        const val ACTION_GATT_HEART_RATE = "de.uriegel.heartrate.ACTION_DATA_AVAILABLE"
        const val HEART_RATE = "HEART_RATE"
    }
}

