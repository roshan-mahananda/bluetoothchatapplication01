package com.example.bluetoothchatapplication02

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluetoothchatapplication02.bluetooth.BluetoothScanner
import com.example.bluetoothchatapplication02.bluetooth.BluetoothSupport
import com.example.bluetoothchatapplication02.ui.components.BluetoothHeader
import com.example.bluetoothchatapplication02.ui.components.DiscoveredDeviceList
import com.example.bluetoothchatapplication02.ui.components.PairedDeviceList
import com.example.bluetoothchatapplication02.ui.theme.BluetoothChatApplication02Theme
import com.example.bluetoothchatapplication02.viewmodel.BluetoothViewModel

@SuppressLint("MissingPermission")
class MainActivity : ComponentActivity() {

    private lateinit var bluetoothSupport: BluetoothSupport
    private lateinit var bluetoothScanner: BluetoothScanner
    private val viewModel: BluetoothViewModel by viewModels()

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action: String? = intent.action

            if (android.bluetooth.BluetoothDevice.ACTION_FOUND == action) {

                val androidDevice: android.bluetooth.BluetoothDevice? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(
                        android.bluetooth.BluetoothDevice.EXTRA_DEVICE,
                        android.bluetooth.BluetoothDevice::class.java
                    )
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(android.bluetooth.BluetoothDevice.EXTRA_DEVICE)
                }

                androidDevice?.let {
                    val customDevice = com.example.bluetoothchatapplication02.model.BluetoothDevice(
                        deviceName = it.name ?: "Unknown Device",
                        deviceAddress = it.address
                    )
                    viewModel.addDiscoveredDevice(customDevice)
                }
            }
        }
    }

    private val enableBluetoothLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) fetchPairedDevices()
    }

    private val requestBtPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            initBluetoothFlow()
        } else {
            println("Bluetooth Permission Denied")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothSupport = BluetoothSupport(this)
        bluetoothScanner = BluetoothScanner()

        val filter = IntentFilter(android.bluetooth.BluetoothDevice.ACTION_FOUND)
        registerReceiver(receiver, filter)

        val permissionsNeeded = mutableListOf<String>()
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            permissionsNeeded += android.Manifest.permission.BLUETOOTH_CONNECT
            permissionsNeeded += android.Manifest.permission.BLUETOOTH_SCAN
        }

        if (permissionsNeeded.isNotEmpty()) {
            requestBtPermission.launch(permissionsNeeded.toTypedArray())
        } else {
            initBluetoothFlow()
        }

        enableEdgeToEdge()
        setContent {
            BluetoothChatApplication02Theme {
                val pairedDevices by viewModel.pairedDevices.collectAsState()
                val discoveredDevices by viewModel.discoveredDevices.collectAsState()

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(16.dp)
                    ) {
                        BluetoothHeader()
                        Spacer(modifier = Modifier.height(16.dp))

                        PairedDeviceList(devices = pairedDevices)
                        Spacer(modifier = Modifier.height(16.dp))

                        DiscoveredDeviceList(
                            devices = discoveredDevices,
                            onScanClick = {
                                startDiscovery()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private fun initBluetoothFlow() {
        bluetoothSupport.checkBluetoothSupport()
        val adapter = bluetoothSupport.getBluetoothAdapter()

        if (adapter?.isEnabled == false) {
            bluetoothSupport.enableBluetooth(adapter, enableBluetoothLauncher)
        } else {
            fetchPairedDevices()
            startDiscovery()
        }
    }

    private fun fetchPairedDevices() {
        val adapter = bluetoothSupport.getBluetoothAdapter()
        if (adapter != null) {
            val devices = bluetoothScanner.findPairedDevices(adapter)
            viewModel.updatePairedDevices(devices)
        }
    }

    private fun startDiscovery() {
        val adapter = bluetoothSupport.getBluetoothAdapter()
        if (adapter != null) {
            bluetoothScanner.startDiscovery(adapter)
        }
    }
}