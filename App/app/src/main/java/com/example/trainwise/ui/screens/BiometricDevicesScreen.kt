package com.example.trainwise.ui.screens

import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainwise.ui.theme.*
import com.google.android.gms.wearable.Wearable

data class Device(
    val id: String,
    val name: String,
    val type: String,
    val isConnected: Boolean,
    val icon: ImageVector,
    val isWearOS: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricDevicesScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var isScanning by remember { mutableStateOf(false) }
    var devices by remember { mutableStateOf(emptyList<Device>()) }

    // Wear OS scanning logic
    val scanForWearOSDevices = {
        isScanning = true
        val nodeClient = Wearable.getNodeClient(context)
        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            val wearOSDevices = nodes.map { node ->
                Device(
                    id = node.id,
                    name = node.displayName,
                    type = "Wear OS Device",
                    isConnected = true, // If it's in connectedNodes, it's connected
                    icon = Icons.Outlined.Watch,
                    isWearOS = true
                )
            }
            // Merge with existing devices, avoiding duplicates
            val updatedList = (devices + wearOSDevices).distinctBy { it.id }
            devices = updatedList
            isScanning = false
        }.addOnFailureListener {
            Log.e("BiometricDevices", "Failed to scan for Wear OS devices", it)
            isScanning = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biometric Devices", color = White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { scanForWearOSDevices() },
                containerColor = Orange,
                contentColor = DarkBackground,
                shape = CircleShape
            ) {
                if (isScanning) {
                    CircularProgressIndicator(color = DarkBackground, modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Icon(Icons.Default.Add, contentDescription = "Scan for Devices")
                }
            }
        }
    ) { padding ->
        val pairedDevices = devices.filter { it.isConnected }
        val availableDevices = devices.filter { !it.isConnected }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Paired Devices",
                    color = GrayText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (pairedDevices.isEmpty()) {
                item {
                    Text("No paired devices found", color = GrayText.copy(alpha = 0.5f), fontSize = 13.sp)
                }
            } else {
                items(pairedDevices) { device ->
                    DeviceCard(device)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Available Devices",
                    color = GrayText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (availableDevices.isEmpty()) {
                item {
                    Text("No available devices nearby", color = GrayText.copy(alpha = 0.5f), fontSize = 13.sp)
                }
            } else {
                items(availableDevices) { device ->
                    DeviceCard(device)
                }
            }

            if (isScanning) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = Orange, modifier = Modifier.size(32.dp))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Searching for Wear OS devices...", color = GrayText, fontSize = 14.sp)
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
                InfoNote("Ensure your Wear OS device is paired via the companion app and has TrainWise installed for full functionality.")
            }
        }
    }
}

@Composable
fun DeviceCard(device: Device) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp),
        border = if (device.isConnected) BorderStroke(1.dp, Orange.copy(alpha = 0.5f)) else null
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(SurfaceColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(device.icon, null, tint = Orange, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(device.name, color = White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(device.type, color = GrayText, fontSize = 13.sp)
            }

            if (device.isConnected) {
                StatusBadge("Connected", Color(0xFF4CAF50))
            } else {
                Text(
                    "Connect",
                    color = Orange,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { /* Connect logic */ }
                )
            }
        }
    }
}

@Composable
fun StatusBadge(text: String, color: Color) {
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, color.copy(alpha = 0.4f))
    ) {
        Text(
            text = text,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun InfoNote(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Orange.copy(alpha = 0.05f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Outlined.Info, null, tint = Orange, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, color = GrayText, fontSize = 12.sp, lineHeight = 18.sp)
    }
}
