package de.uriegel.heartrate

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import de.uriegel.activityextensions.ActivityRequest
import de.uriegel.activityextensions.async.delay
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), CoroutineScope {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        launch {
            val result = activityRequest.checkAndAccessPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
            if (result.any { !it.value }) {
                Toast.makeText(this@MainActivity, "Kein Zugriff auf den Standort", Toast.LENGTH_LONG).show()
                return@launch
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val backgroundResult = activityRequest.checkAndAccessPermissions(arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION))
                if (backgroundResult.any { !it.value }) {
                    Toast.makeText(this@MainActivity, "Kein ständiger Zugriff auf den Standort", Toast.LENGTH_LONG).show()
                    return@launch
                }
            }

            if (!bluetoothAdapter.isEnabled) {
                val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                val res = activityRequest.launch(enableBtIntent)
            }

            bleScanner = bluetoothAdapter.bluetoothLeScanner
            bleScanner?.startScan(null, scanSettings, scanCallback)
        }
    }

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            with(result.device) {
                if (device == null) {
                    Log.i("BLE","Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                    device = address
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE", "onScanFailed: code $errorCode")
        }
    }

    override val coroutineContext = Dispatchers.Main

    private var device: String? = null
    var bleScanner: BluetoothLeScanner? = null
    private val activityRequest = ActivityRequest(this)
    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()
}