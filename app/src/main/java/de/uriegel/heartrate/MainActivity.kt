package de.uriegel.heartrate

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import de.uriegel.activityextensions.ActivityRequest
import de.uriegel.heartrate.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CoroutineScope {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val preferences = getSharedPreferences("default", MODE_PRIVATE)
        preferences?.getString(HEARTRATE_ADDRESS, null)?.let {
            heartRateAddress = it
            binding.btnHeartRate.isEnabled = true
        }
        preferences?.getString(BIKE_ADDRESS, null)?.let {
            bikeAddress = it
            binding.btnBike.isEnabled = true
        }

        launch {
            val result = activityRequest.checkAndAccessPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            if (result.any { !it.value }) {
                Toast.makeText(this@MainActivity, "Kein Zugriff auf den Standort", Toast.LENGTH_LONG).show()
                finish()
                return@launch
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val backgroundResult = activityRequest.checkAndAccessPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                if (backgroundResult.any { !it.value }) {
                    Toast.makeText(this@MainActivity, "Kein ständiger Zugriff auf den Standort", Toast.LENGTH_LONG).show()
                    finish()
                    return@launch
                }
            }

            val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                activityRequest.launch(enableBtIntent)
            }
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onScanHeartRate(view: View) {
        launch {
            heartRateAddress = scan(HeartRateService.HEART_RATE_UUID)
            binding.btnHeartRate.isEnabled = heartRateAddress != null
            val preferences = getSharedPreferences("default", MODE_PRIVATE)
            preferences?.edit()?.putString(HEARTRATE_ADDRESS, heartRateAddress)?.apply()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun onScanBike(view: View) {
        launch {
            bikeAddress = scan(BikeService.BIKE_UUID)
            binding.btnBike.isEnabled = bikeAddress != null
            val preferences = getSharedPreferences("default", MODE_PRIVATE)
            preferences?.edit()?.putString(BIKE_ADDRESS, bikeAddress)?.apply()
        }
    }

    @Suppress("UNUSED_PARAMETER")
    fun startHeartRate(view: View) {
        val intent = Intent(this, HeartRateActivity::class.java)
        startActivity(intent)
    }

    @Suppress("UNUSED_PARAMETER")
    fun startBike(view: View) {
        val intent = Intent(this, BikeActivity::class.java)
        startActivity(intent)
    }

    private suspend fun scan(uuid: String): String? {
        val intent = Intent(this@MainActivity, DevicesActivity::class.java)
        intent.putExtra("UUID", uuid)
        val result = activityRequest.launch(intent)
        return result.data?.getStringExtra(DevicesActivity.RESULT_DEVICE)
    }

    companion object {
        val HEARTRATE_ADDRESS = "HEARTRATE_ADDRESS"
        val BIKE_ADDRESS = "BIKE_ADDRESS"
    }

    override val coroutineContext = Dispatchers.Main

    private var heartRateAddress: String? = null
    private var bikeAddress: String? = null
    private val activityRequest = ActivityRequest(this)
    private lateinit var binding: ActivityMainBinding
}

