package com.example.bluetoothchatapplication02.bluetooth

import android.annotation.SuppressLint
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log


private const val TAG = "BluetoothLeService"

@SuppressLint("MissingPermission")

class BluetoothLeService : Service(){
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothGatt: BluetoothGatt? = null

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): BluetoothLeService{
            return this@BluetoothLeService
        }
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    fun initialize(): Boolean {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter
        if (bluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.")
            return false
        }
        return true
    }

    fun connect(address: String): Boolean {
        bluetoothAdapter?.let { adapter ->
            try {
                val device = adapter.getRemoteDevice(address)
                bluetoothGatt = device.connectGatt(this, false, bluetoothGattCallback)
                return true
            } catch (exception: IllegalArgumentException) {
                Log.w(TAG, "Device not found with provided address.")
                return false
            }
        } ?: run {
            Log.w(TAG, "BluetoothAdapter not initialized")
            return false
        }
    }

    private val bluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onConnectionStateChange(gatt: BluetoothGatt?, status: Int, newState: Int) {
            if(newState == BluetoothProfile.STATE_CONNECTED){
                Log.i(TAG, "Successfully connected to GATT Server")
                bluetoothGatt?.discoverServices()
            }else if(newState == BluetoothProfile.STATE_DISCONNECTED){
                Log.i(TAG, "Disconnected from the GATT Server")
            }
        }

        override fun onServicesDiscovered(gatt: BluetoothGatt?, status: Int) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(TAG, "Services discovered successfully")
            } else {
                Log.w(TAG, "onServicesDiscovered received: $status")
            }
        }
    }

    override fun onUnbind(intent: Intent?): Boolean {
        bluetoothGatt?.close()
        bluetoothGatt = null
        return super.onUnbind(intent)
    }
}