package com.example.bluetoothchatapplication02.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.os.Handler
import android.os.Looper
import com.example.bluetoothchatapplication02.model.BluetoothDevice

@SuppressLint("MissingPermission")
class BluetoothScanner {

    private val SCAN_PERIOD: Long = 10000 // 10 seconds
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())
    private var leScanCallback: ScanCallback? = null

    fun findPairedDevices(bluetoothAdapter: BluetoothAdapter): List<BluetoothDevice> {
        val pairedDevices = bluetoothAdapter.bondedDevices
        return pairedDevices.map { device ->
            BluetoothDevice(
                deviceName = device.name ?: "Unknown Device",
                deviceAddress = device.address
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun scanLeDevice(
        bluetoothAdapter: BluetoothAdapter?,
        onDeviceFound: (BluetoothDevice) -> Unit
    ) {
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner ?: return

        if (scanning) {
            leScanCallback?.let { bluetoothLeScanner.stopScan(it) }
            handler.removeCallbacksAndMessages(null)
            scanning = false
        }

        leScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)
                val androidDevice = result.device
                val customDevice = BluetoothDevice(
                    deviceName = androidDevice.name?.takeIf { it.isNotBlank() } ?: "Unknown BLE Device",
                    deviceAddress = androidDevice.address
                )
                onDeviceFound(customDevice)
            }
        }

        val settings = android.bluetooth.le.ScanSettings.Builder()
            .setScanMode(android.bluetooth.le.ScanSettings.SCAN_MODE_LOW_LATENCY)
            .build()

        handler.postDelayed({
            if (scanning) {
                scanning = false
                leScanCallback?.let { bluetoothLeScanner.stopScan(it) }
                leScanCallback = null
            }
        }, SCAN_PERIOD)

        scanning = true
        bluetoothLeScanner.startScan(null, settings, leScanCallback)
    }

    fun stopScan(bluetoothAdapter: BluetoothAdapter?) {
        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner ?: return
        if (scanning) {
            scanning = false
            leScanCallback?.let { bluetoothLeScanner.stopScan(it) }
            leScanCallback = null
            handler.removeCallbacksAndMessages(null)
        }
    }
}