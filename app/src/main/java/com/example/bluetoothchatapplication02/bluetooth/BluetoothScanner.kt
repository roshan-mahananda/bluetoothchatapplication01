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

    // Stops scanning after 10 seconds.
    private val SCAN_PERIOD: Long = 10000
    private var scanning = false
    private val handler = Handler(Looper.getMainLooper())

    fun findPairedDevices(bluetoothAdapter: BluetoothAdapter): List<BluetoothDevice> {
        val pairedDevices = bluetoothAdapter.bondedDevices
        return pairedDevices.map { device ->
            BluetoothDevice(
                deviceName = device.name ?: "Unknown Device",
                deviceAddress = device.address
            )
        }
    }

    fun startDiscovery(bluetoothAdapter: BluetoothAdapter?) {
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter.cancelDiscovery()
        }
        bluetoothAdapter?.startDiscovery()
    }

    fun scanLeDevice(
        bluetoothAdapter: BluetoothAdapter?,
        onDeviceFound: (BluetoothDevice) -> Unit
    ) {

        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner ?: return

        val leScanCallback = object : ScanCallback() {
            override fun onScanResult(callbackType: Int, result: ScanResult) {
                super.onScanResult(callbackType, result)

                val androidDevice = result.device
                val customDevice = BluetoothDevice(
                    deviceName = androidDevice.name ?: "Unknown BLE Device",
                    deviceAddress = androidDevice.address
                )

                onDeviceFound(customDevice)
            }
        }

        if (!scanning) {
            handler.postDelayed({
                scanning = false
                bluetoothLeScanner.stopScan(leScanCallback)
            }, SCAN_PERIOD)

            scanning = true
            bluetoothLeScanner.startScan(leScanCallback)
        } else {
            scanning = false
            bluetoothLeScanner.stopScan(leScanCallback)
        }
    }
}