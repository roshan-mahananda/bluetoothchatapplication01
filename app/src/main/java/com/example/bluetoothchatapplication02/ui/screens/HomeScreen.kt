package com.example.bluetoothchatapplication02.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.bluetoothchatapplication02.ui.components.BluePrimary
import com.example.bluetoothchatapplication02.ui.components.CardBackground

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
    Column(modifier = Modifier.padding(top = 16.dp, start = 16.dp, end = 16.dp)) {
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
            badgeBgColor = Color(0xFFE0E0E0),
            badgeTextColor = if (queuedMessagesCount > 0) Color.DarkGray else Color.Gray
        )
    }
}