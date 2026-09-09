package com.example.bluetoothchatapplication02

import android.annotation.SuppressLint
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
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.bluetoothchatapplication02.bluetooth.BluetoothLeService
import com.example.bluetoothchatapplication02.bluetooth.BluetoothScanner
import com.example.bluetoothchatapplication02.bluetooth.BluetoothSupport
import com.example.bluetoothchatapplication02.ui.components.HopLinkBottomNav
import com.example.bluetoothchatapplication02.ui.components.Screen
import com.example.bluetoothchatapplication02.ui.screens.ChatScreen
import com.example.bluetoothchatapplication02.ui.screens.DiscoverScreen
import com.example.bluetoothchatapplication02.ui.screens.HopLinkDashboardScreen
import com.example.bluetoothchatapplication02.ui.screens.SosScreen
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        bluetoothSupport = BluetoothSupport(this)
        bluetoothScanner = BluetoothScanner()

        val gattServiceIntent = Intent(this, BluetoothLeService::class.java)
        bindService(gattServiceIntent, serviceConnection, Context.BIND_AUTO_CREATE)

        enableEdgeToEdge()
        setContent {
            BluetoothChatApplication02Theme {
                val discoveredDevices by viewModel.discoveredDevices.collectAsState()
                val activeRelays by viewModel.activeRelays.collectAsState()
                val queuedMessages by viewModel.queuedMessages.collectAsState()

                var isHopLinkOn by remember {
                    mutableStateOf(bluetoothSupport.getBluetoothAdapter()?.isEnabled == true)
                }

                // Track current active screen for bottom navigation
                var currentRoute by remember { mutableStateOf(Screen.Home.route) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        HopLinkBottomNav(currentRoute = currentRoute) { selectedScreen ->
                            currentRoute = selectedScreen.route
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        when (currentRoute) {
                            Screen.Home.route -> {
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
                                        } else {
                                            adapter?.disable()
                                        }
                                    }
                                )
                            }
                            Screen.Discover.route -> {
                                DiscoverScreen(
                                    devices = discoveredDevices,
                                    onDeviceClick = { device ->
                                        bluetoothService?.connect(device.deviceAddress)
                                        currentRoute = Screen.Chats.route
                                    }
                                )
                            }
                            Screen.Chats.route -> {
                                ChatScreen(
                                    onSendMessage = { message ->
                                        viewModel.updateQueuedMessages(queuedMessages + 1)
                                    }
                                )
                            }
                            Screen.SOS.route -> {
                                SosScreen(
                                    onTriggerSos = {
                                        viewModel.updateQueuedMessages(queuedMessages + 1)
                                    }
                                )
                            }
                        }
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

    private fun startDiscovery() {
        val adapter = bluetoothSupport.getBluetoothAdapter()
        if (adapter != null) {
            bluetoothScanner.scanLeDevice(adapter) { discoveredDevice ->
                viewModel.addDiscoveredDevice(discoveredDevice)
            }
        }
    }

    private fun makeGattUpdateIntentFilter(): IntentFilter {
        return IntentFilter().apply {
            addAction(BluetoothLeService.ACTION_GATT_CONNECTED)
            addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED)
        }
    }
}