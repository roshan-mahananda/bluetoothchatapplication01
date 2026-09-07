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

    private val SCAN_PERIOD: Long = 10000
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

    fun scanLeDevice(
        bluetoothAdapter: BluetoothAdapter?,
        onDeviceFound: (BluetoothDevice) -> Unit
    ) {

        val bluetoothLeScanner = bluetoothAdapter?.bluetoothLeScanner ?: return

        if (!scanning) {
            leScanCallback = object : ScanCallback() {
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
            handler.postDelayed({
                scanning = false
                leScanCallback?.let {
                    bluetoothLeScanner.stopScan(it)
                }
                leScanCallback = null
            }, SCAN_PERIOD)

            scanning = true
            leScanCallback?.let {
                bluetoothLeScanner.startScan(it)
            }
        } else {
            scanning = false
            leScanCallback?.let {
                bluetoothLeScanner.stopScan(it)
                leScanCallback = null
            }
        }
    }
}