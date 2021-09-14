package de.uriegel.heartrate

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import de.uriegel.heartrate.databinding.ActivityBikeBinding
import de.uriegel.heartrate.databinding.ActivityHeartRateBinding

class BikeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBikeBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (deviceAddress != null) {
            val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
            bindService(gattServiceIntent, heartRateServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    private val heartRateServiceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.let {
                if (!it.initialize()) {
                    Log.e(TAG, "Unable to initialize Bluetooth")
                    finish()
                }
                it.connect(deviceAddress!!)
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }

    private var bluetoothService : BluetoothLeService? = null
    private val deviceAddress by lazy {
        val preferences = getSharedPreferences("default", MODE_PRIVATE)
        preferences.getString(MainActivity.BIKE_ADDRESS, null)
    }
    private lateinit var binding: ActivityBikeBinding
}

private const val TAG = "BIKE"