package com.example.bluetoothchatapplication02.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.result.ActivityResultLauncher

@SuppressLint("MissingPermission")
class BluetoothSupport(private val activity: Activity) {

    fun getBluetoothAdapter(): BluetoothAdapter? {
        val bluetoothManager: BluetoothManager = activity.getSystemService(BluetoothManager::class.java)
        return bluetoothManager.adapter
    }

    fun checkBluetoothSupport(): Boolean {
        val bluetoothAdapter = getBluetoothAdapter()

        if(bluetoothAdapter == null){
            println("Device doesn't support Bluetooth")
            return false
        }
        if (!activity.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)){
            println("Device doesn't support Bluetooth Low Energy (BLE)")
            return false
        }

        println("BLE is supported on this device")
        return true
    }

    @SuppressLint("MissingPermission")
    fun requestEnableBluetooth(launcher: androidx.activity.result.ActivityResultLauncher<Intent>) {
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        launcher.launch(enableBtIntent)
    }

    fun enableBluetoothDirect(bluetoothAdapter: BluetoothAdapter?): Boolean{
        return bluetoothAdapter?.enable() ?: false
    }
}