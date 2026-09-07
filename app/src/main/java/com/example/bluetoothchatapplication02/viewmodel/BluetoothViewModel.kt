package com.example.bluetoothchatapplication02.viewmodel

import androidx.lifecycle.ViewModel
import com.example.bluetoothchatapplication02.model.BluetoothDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothViewModel: ViewModel() {
    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices.asStateFlow()

    private val _discoverableDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoverableDevices.asStateFlow()

    fun updatePairedDevices(devices: List<BluetoothDevice>){
        _pairedDevices.value = devices
    }

    fun addDiscoveredDevice(device: BluetoothDevice){
        if(!_discoverableDevices.value.any{it.deviceAddress == device.deviceAddress}){
            _discoverableDevices.value += device
        }
    }
}