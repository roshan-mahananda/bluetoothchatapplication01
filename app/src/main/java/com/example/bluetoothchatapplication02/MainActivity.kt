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
import androidx.core.content.ContextCompat
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.bluetoothchatapplication02.bluetooth.BluetoothLeService
import com.example.bluetoothchatapplication02.bluetooth.BluetoothScanner
import com.example.bluetoothchatapplication02.bluetooth.BluetoothSupport
import com.example.bluetoothchatapplication02.ui.components.HopLinkDashboardScreen
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
        if (result.resultCode == Activity.RESULT_OK) {
            fetchPairedDevices()
            startDiscovery()
        }
    }

    private val requestBtPermission = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        if (grants.values.all { it }) {
            initBluetoothFlow()
        } else {
            Log.e("MainActivity", "Bluetooth Permission Denied")
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
                val discoveredDevices by viewModel.discoveredDevices.collectAsState()
                val activeRelays by viewModel.activeRelays.collectAsState()
                val queuedMessages by viewModel.queuedMessages.collectAsState()

                var isHopLinkOn by remember {
                    mutableStateOf(bluetoothSupport.getBluetoothAdapter()?.isEnabled == true)
                }

                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .padding(innerPadding)
                            .padding(horizontal = 16.dp)
                    ) {
                        HopLinkDashboardScreen(
                            discoveredCount = discoveredDevices.size,
                            isBluetoothOn = isHopLinkOn,
                            activeRelaysCount = activeRelays,
                            queuedMessagesCount = queuedMessages,
                            onToggleBluetooth = { isOn ->
                                isHopLinkOn = isOn
                                val adapter = bluetoothSupport.getBluetoothAdapter()
                                if (isOn) {
                                    if (adapter?.isEnabled == false) {
                                        bluetoothSupport.enableBluetoothDirect(adapter)
                                    }
                                    viewModel.clearDiscoveredDevices()
                                    startDiscovery()
                                }else{
                                    if(adapter?.isEnabled == true){
                                        adapter.disable()
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        ContextCompat.registerReceiver(
            this,
            gattUpdateReceiver,
            makeGattUpdateIntentFilter(),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
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

        if (adapter?.isEnabled == true) {
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