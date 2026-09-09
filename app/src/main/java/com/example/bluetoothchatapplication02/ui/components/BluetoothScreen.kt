package com.example.bluetoothchatapplication02.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bluetoothchatapplication02.R
import com.example.bluetoothchatapplication02.model.BluetoothDevice

val BluePrimary = Color(0xFF1877F2)
val CardBackground = Color(0xFFF5F7FA)

sealed class Screen(val route: String, val title: String, val iconResId: Int) {
    object Home : Screen(route = "home", title = "Home", iconResId = R.drawable.home_24px)
    object Discover : Screen(route = "discover", title = "Discover", iconResId = R.drawable.nearby_24px)
    object Chats : Screen(route = "chats", title = "Chats", iconResId = R.drawable.sms_24px)
    object SOS : Screen(route = "sos", title = "SOS", iconResId = R.drawable.sos_24px)
}

@Composable
fun HopLinkBottomNav(currentRoute: String, onItemSelected: (Screen) -> Unit) {
    val items = listOf(Screen.Home, Screen.Discover, Screen.Chats, Screen.SOS)

    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        items.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        painter = painterResource(id = screen.iconResId),
                        contentDescription = screen.title
                    )
                },
                label = { Text(screen.title, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                selected = currentRoute == screen.route,
                onClick = { onItemSelected(screen) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = BluePrimary,
                    selectedTextColor = BluePrimary,
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = Color(0xFFE8F0FE)
                )
            )
        }
    }
}

@Composable
fun SosScreen(onTriggerSos: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(160.dp)
                .background(Color(0xFFFF4D4D), shape = RoundedCornerShape(80.dp)),
            contentAlignment = Alignment.Center
        ) {
            IconButton(onClick = onTriggerSos) {
                Text(
                    text = "SOS",
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 36.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "Emergency Broadcast",
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = Color.Black
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Tap to instantly flood nearby mesh nodes with your GPS and emergency status without internet.",
            color = Color.Gray,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
fun ChatScreen() {
    var messageText by remember { mutableStateOf("") }
    val chatMessages = remember { mutableStateListOf("Emergency broadcast node initialized.", "Relay path open to Node #2.") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "HopLink Direct Chat",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 24.sp,
            color = Color.Black,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(chatMessages) { message ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = CardBackground
                    ) {
                        Text(
                            text = message,
                            modifier = Modifier.padding(12.dp),
                            color = Color.DarkGray,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                placeholder = { Text("Type emergency text...") },
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp),
                shape = RoundedCornerShape(12.dp)
            )
            Button(
                onClick = {
                    if (messageText.isNotBlank()) {
                        chatMessages.add(messageText)
                        messageText = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Send")
            }
        }
    }
}

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
                Text(text = "Scanning for nearby HopLink nodes...", color = Color.Gray)
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
                            Column {
                                Text(
                                    text = device.deviceName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )
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

@Composable
fun HopLinkHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 24.dp)
    ) {
        Text(
            text = "HopLink",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 32.sp,
            color = Color.Black
        )
        Text(
            text = "Works without internet or signal",
            color = Color.Gray,
            fontSize = 16.sp
        )
    }
}

@Composable
fun MainToggleCard(isBluetoothOn: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = BluePrimary)
    ) {
        Row(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isBluetoothOn) "HopLink is ON" else "HopLink is OFF",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                Text(
                    text = "You are visible to nearby devices",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            Switch(
                checked = isBluetoothOn,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = BluePrimary,
                    checkedTrackColor = Color.White,
                    uncheckedThumbColor = Color.LightGray,
                    uncheckedTrackColor = Color.DarkGray
                )
            )
        }
    }
}

@Composable
fun DashboardCard(
    title: String,
    subtitle: String,
    icon: Painter,
    iconBgColor: Color,
    badgeText: String,
    badgeBgColor: Color,
    badgeTextColor: Color = Color.White,
    isCircleBadge: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = icon,
                    contentDescription = null,
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black
                )
                Text(
                    text = subtitle,
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(if (isCircleBadge) CircleShape else RoundedCornerShape(12.dp))
                    .background(badgeBgColor)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeText,
                    color = badgeTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun HopLinkDashboardScreen(
    discoveredCount: Int,
    isBluetoothOn: Boolean,
    activeRelaysCount: Int,
    queuedMessagesCount: Int,
    onToggleBluetooth: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        HopLinkHeader()
        MainToggleCard(isBluetoothOn = isBluetoothOn, onToggle = onToggleBluetooth)

        DashboardCard(
            title = "Nearby Devices",
            subtitle = if (isBluetoothOn) "Scanning via\nBluetooth" else "Bluetooth is off",
            icon = painterResource(R.drawable.nearby_24px),
            iconBgColor = Color(0xFFE8F0FE),
            badgeText = discoveredCount.toString(),
            badgeBgColor = if (discoveredCount > 0) BluePrimary else Color.LightGray,
            isCircleBadge = true
        )

        DashboardCard(
            title = "Relay Active",
            subtitle = if (activeRelaysCount > 0) "You are forwarding\n$activeRelaysCount messages" else "No active relays",
            icon = painterResource(R.drawable.schedule_send_24px),
            iconBgColor = Color(0xFFE6F4EA),
            badgeText = if (activeRelaysCount > 0) "ON" else "OFF",
            badgeBgColor = if (activeRelaysCount > 0) Color(0xFF34A853) else Color.LightGray
        )

        DashboardCard(
            title = "Messages Queued",
            subtitle = if (queuedMessagesCount > 0) "Waiting for a relay\npath" else "Queue is empty",
            icon = painterResource(R.drawable.message_queue),
            iconBgColor = Color(0xFFFFF3E0),
            badgeText = queuedMessagesCount.toString(),
            badgeBgColor = if (queuedMessagesCount > 0) Color(0xFFE0E0E0) else Color(0xFFE0E0E0),
            badgeTextColor = if (queuedMessagesCount > 0) Color.White else Color.Gray
        )
    }
}