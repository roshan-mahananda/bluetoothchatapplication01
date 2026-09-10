package com.example.bluetoothchatapplication02.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetoothchatapplication02.model.BluetoothDevice
import com.example.bluetoothchatapplication02.ui.components.BluePrimary
import com.example.bluetoothchatapplication02.ui.components.CardBackground

@Composable
fun DiscoverScreen(
    devices: List<BluetoothDevice>,
    onDeviceClick: (BluetoothDevice) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Discovered Mesh Nodes",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (devices.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Scanning for nearby HopLink nodes & paired devices...", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(devices) { device ->
                    Card(
                        onClick = { onDeviceClick(device) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBackground)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.deviceName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = device.deviceAddress,
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }

                            Button(
                                onClick = { onDeviceClick(device) },
                                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Connect", color = Color.White, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}