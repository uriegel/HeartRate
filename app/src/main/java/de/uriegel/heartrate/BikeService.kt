package de.uriegel.heartrate

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Intent
import java.util.*

class BikeService : BluetoothLeService() {
    override fun getUuid() = BikeService.BIKE_UUID

    override fun discoverService(bluetoothGatt: BluetoothGatt, service: BluetoothGattService) {
        val bikeCharacteristics = service.characteristics?.find { it.uuid == UUID.fromString(BIKE_CHARACTERISTICS_ID) }
        bikeCharacteristics?.let {
            bluetoothGatt.setCharacteristicNotification(it, true)
            val descriptor = it.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTICS_ID))
            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
            bluetoothGatt.writeDescriptor(descriptor)
        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        val flag = characteristic.properties
        val eins = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 1) // wheel cycles
        val zwei = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 5) // timestamp wheel
        val drei = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 7) // pedal cycles
        val vier = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT16, 9) // timestamp pedal
        if (eins > 0 || zwei > 0  || drei > 0 || vier > 0) {
            val mist = eins > 0 || zwei > 0  || drei > 0 || vier > 0
            val letzt = 0
        } else {
            val watt = 0
        }
    }

    private fun broadcastBikeUpdate(rate: Int) {
        val intent = Intent(ACTION_GATT_DATA)
//        intent.putExtra(HeartRateService.HEART_RATE, rate)
        sendBroadcast(intent)
    }

    override fun getTag() = "BIKE"

    companion object {
        const val BIKE_UUID = "00001816-0000-1000-8000-00805f9b34fb"
        const val BIKE_CHARACTERISTICS_ID = "00002a5b-0000-1000-8000-00805f9b34fb"
        //const val HEART_RATE = "HEART_RATE"
    }
}