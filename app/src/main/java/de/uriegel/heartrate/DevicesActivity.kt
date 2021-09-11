package de.uriegel.heartrate

import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_devices.*

class DevicesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_devices)

        val layoutManager = LinearLayoutManager(this)
        layoutManager.orientation = LinearLayoutManager.VERTICAL
        devices.layoutManager = layoutManager
        val itemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        devices.addItemDecoration(itemDecoration)
        devices.setHasFixedSize(true)
        devices.adapter = devicesAdapter

        bleScanner = bluetoothAdapter.bluetoothLeScanner
        handler.postDelayed({
            scanning = false
            bleScanner?.stopScan(scanCallback)
        }, SCAN_PERIOD)
        scanning = true
        bleScanner?.startScan(null, scanSettings, scanCallback)
    }

    private val bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    var bleScanner: BluetoothLeScanner? = null

    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            devicesAdapter.addDevice(result.device)
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("BLE", "onScanFailed: code $errorCode")
        }
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var scanning = false
    private val handler = Handler()
    private val SCAN_PERIOD = 10000L
    private var devicesAdapter = DevicesAdapter()
}
