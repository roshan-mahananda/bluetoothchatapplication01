package com.example.bluetoothchatapplication02.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetoothchatapplication02.model.BluetoothDevice

@Composable
fun BluetoothHeader(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Bluetooth Chat App",
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
    }
}

@Composable
fun PairedDeviceList(modifier: Modifier = Modifier, devices: List<BluetoothDevice>) {
    Column(modifier = modifier) {
        Text(
            text = "Paired Devices",
            fontWeight = FontWeight.Bold,
            modifier = modifier.padding(bottom = 8.dp)
        )
        if (devices.isEmpty()) {
            Text(text = "No paired devices found.", color = Color.Gray)
        } else {
            LazyColumn {
                items(devices) { device ->
                    DeviceCard(device)
                }
            }
        }
    }
}

@Composable
fun DeviceCard(device: BluetoothDevice) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Text(
                text = device.deviceName,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
            Text(
                text = device.deviceAddress,
                color = Color.Gray,
                fontSize = 12.sp
            )
        }
    }
}
@Composable
fun DiscoveredDeviceList(
    modifier: Modifier = Modifier,
    devices: List<BluetoothDevice>,
    onScanClick: () -> Unit
) {
    Column(modifier = modifier) {
        Text(
            text = "Discovered Devices",
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp, top = 16.dp) // Added top padding for spacing
        )

        if (devices.isEmpty()) {
            Text(
                text = "No devices discovered yet. Press scan to search.",
                color = Color.Gray
            )
        } else {
            LazyColumn {
                items(devices) { device ->
                    // Reusing your existing DeviceCard perfectly!
                    DeviceCard(device)
                }
            }
        }
    }
}