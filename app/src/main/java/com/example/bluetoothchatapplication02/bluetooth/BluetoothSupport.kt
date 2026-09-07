package com.example.bluetoothchatapplication02.bluetooth

import android.annotation.SuppressLint
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher

@SuppressLint("MissingPermission")
class BluetoothSupport(private val activity: Activity) {

    fun getBluetoothAdapter(): BluetoothAdapter? {
        val bluetoothManager: BluetoothManager = activity.getSystemService(BluetoothManager::class.java)
        return bluetoothManager.adapter
    }

    fun checkBluetoothSupport() {
        val bluetoothAdapter = getBluetoothAdapter()

        if(bluetoothAdapter == null){
            println("Device doesn't support Bluetooth")
        } else {
            println("Bluetooth is supported")
        }
    }

    fun enableBluetooth(bluetoothAdapter: BluetoothAdapter?, launcher: ActivityResultLauncher<Intent>) {
        if(bluetoothAdapter?.isEnabled == false){
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            launcher.launch(enableBtIntent)
        }
    }
}