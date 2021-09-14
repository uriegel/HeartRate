package de.uriegel.heartrate

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import de.uriegel.heartrate.BluetoothLeService.Companion.makeGattUpdateIntentFilter
import de.uriegel.heartrate.databinding.ActivityHeartRateBinding

class HeartRateActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHeartRateBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (deviceAddress != null) {
            val gattServiceIntent = Intent(this, HeartRateService::class.java)
            bindService(gattServiceIntent, heartRateServiceConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
        if (bluetoothService != null) {
            val result = bluetoothService!!.connect(deviceAddress!!)
            Log.d(TAG, "Connect request result=$result")
        }
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    // connected = true
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    // connected = false
                }
                HeartRateService.ACTION_GATT_HEART_RATE -> {
                    binding.textViewHeartRate.text = intent.getIntExtra(HeartRateService.HEART_RATE, 0).toString()
                }
            }
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
        preferences.getString(MainActivity.HEARTRATE_ADDRESS, null)
    }
    private lateinit var binding: ActivityHeartRateBinding
}

private const val TAG = "HR"