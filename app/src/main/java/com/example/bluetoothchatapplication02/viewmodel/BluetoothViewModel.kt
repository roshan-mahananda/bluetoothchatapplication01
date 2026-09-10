package com.example.bluetoothchatapplication02.viewmodel

import android.bluetooth.BluetoothAdapter
import androidx.lifecycle.ViewModel
import com.example.bluetoothchatapplication02.bluetooth.BluetoothScanner
import com.example.bluetoothchatapplication02.model.BluetoothDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class BluetoothViewModel : ViewModel() {
    private val _pairedDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    private val _discoverableDevices = MutableStateFlow<List<BluetoothDevice>>(emptyList())
    private val _connectionStatus = MutableStateFlow("Disconnected")
    private val _activeRelays = MutableStateFlow(0)
    private val _queuedMessages = MutableStateFlow(0)

    val pairedDevices: StateFlow<List<BluetoothDevice>> = _pairedDevices.asStateFlow()
    val discoveredDevices: StateFlow<List<BluetoothDevice>> = _discoverableDevices.asStateFlow()
    val connectionStatus: StateFlow<String> = _connectionStatus.asStateFlow()
    val activeRelays: StateFlow<Int> = _activeRelays.asStateFlow()
    val queuedMessages: StateFlow<Int> = _queuedMessages.asStateFlow()

    fun addDiscoveredDevice(device: BluetoothDevice) {
        val currentList = _discoverableDevices.value
        if (!currentList.any { it.deviceAddress == device.deviceAddress }) {
            _discoverableDevices.value = currentList + device
        }
    }

    fun loadPairedDevices(bluetoothAdapter: BluetoothAdapter, scanner: BluetoothScanner) {
        val pairedList = scanner.findPairedDevices(bluetoothAdapter)
        _pairedDevices.value = pairedList

        val currentDiscovered = _discoverableDevices.value
        val combined = (pairedList + currentDiscovered).distinctBy { it.deviceAddress }
        _discoverableDevices.value = combined
    }

    fun clearDiscoveredDevices() {
        _discoverableDevices.value = emptyList()
        _pairedDevices.value = emptyList()
    }

    fun updateConnectionStatus(status: String) {
        _connectionStatus.value = status
    }

    fun updateActiveRelays(count: Int) {
        _activeRelays.value = count
    }

    fun updateQueuedMessages(count: Int) {
        _queuedMessages.value = count
    }
}