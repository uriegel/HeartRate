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
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import de.uriegel.activityextensions.ActivityRequest
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
                val res = activityRequest.launch(enableBtIntent)
            }
        }
    }

    fun onScan(view: View) {
        startActivity(Intent(this@MainActivity, DevicesActivity::class.java))
    }

    override val coroutineContext = Dispatchers.Main
    private val activityRequest = ActivityRequest(this)
}