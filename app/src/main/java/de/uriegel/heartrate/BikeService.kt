package de.uriegel.heartrate

import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothGattDescriptor
import android.bluetooth.BluetoothGattService
import android.content.Intent
import java.util.*

class BikeService : BluetoothLeService() {
    override fun getUuid() = HeartRateService.HEART_RATE_UUID

    override fun discoverService(bluetoothGatt: BluetoothGatt, service: BluetoothGattService) {
//        val heartCharacteristics = service.characteristics?.find { it.uuid == UUID.fromString(HeartRateService.HEART_RATE_CHARACTERISTICS_ID) }
//        heartCharacteristics?.let {
//            bluetoothGatt.setCharacteristicNotification(it, true)
//            val descriptor = it.getDescriptor(UUID.fromString(CLIENT_CHARACTERISTICS_ID))
//            descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
//            bluetoothGatt.writeDescriptor(descriptor)
//        }
    }

    override fun onCharacteristicChanged(gatt: BluetoothGatt, characteristic: BluetoothGattCharacteristic) {
        //val flag = characteristic.properties
    }

    private fun broadcastBikeUpdate(rate: Int) {
        val intent = Intent(ACTION_GATT_DATA)
//        intent.putExtra(HeartRateService.HEART_RATE, rate)
        sendBroadcast(intent)
    }

    override fun getTag() = "BIKE"

    companion object {
        const val BIKE_UUID = "00001816-0000-1000-8000-00805f9b34fb"
        //const val HEART_RATE_CHARACTERISTICS_ID = "00002a37-0000-1000-8000-00805f9b34fb"
        //const val HEART_RATE = "HEART_RATE"
    }
}