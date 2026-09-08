package com.example.bluetoothchatapplication02

import android.annotation.SuppressLint
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
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
import com.example.bluetoothchatapplication02.bluetooth.BluetoothLeService
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

    private var bluetoothService: BluetoothLeService? = null
    private val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(componentName: ComponentName, service: IBinder) {
            bluetoothService = (service as BluetoothLeService.LocalBinder).getService()
            bluetoothService?.let { bluetooth ->
                if (!bluetooth.initialize()) {
                    Log.e("MainActivity", "Unable to initialize Bluetooth")
                    finish()
                }
            }
        }

        override fun onServiceDisconnected(componentName: ComponentName) {
            bluetoothService = null
        }
    }

    private val gattUpdateReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothLeService.ACTION_GATT_CONNECTED -> {
                    viewModel.updateConnectionStatus("Connected")
                }
                BluetoothLeService.ACTION_GATT_DISCONNECTED -> {
                    viewModel.updateConnectionStatus("Disconnected")
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

        // Bind the BluetoothLeService
        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        val permissionsNeeded = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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

                // You can pass this to your header or lists to show active status
                val connectionStatus by viewModel.connectionStatus.collectAsState()

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

    override fun onResume() {
        super.onResume()
        registerReceiver(gattUpdateReceiver, makeGattUpdateIntentFilter())
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(gattUpdateReceiver)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(serviceConnection)
    }

    private fun initBluetoothFlow() {
        if (!bluetoothSupport.checkBluetoothSupport()) return

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
            bluetoothScanner.scanLeDevice(adapter) { discoveredDevice ->
                viewModel.addDiscoveredDevice(discoveredDevice)
            }
        }
    }

    private fun connectToDevice(address: String) {
        bluetoothService?.connect(address)
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        }
    }
}