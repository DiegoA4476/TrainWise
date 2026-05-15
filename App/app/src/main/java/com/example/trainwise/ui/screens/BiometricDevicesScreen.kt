package com.example.trainwise.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainwise.data.models.Device
import com.example.trainwise.ui.theme.*
import com.example.trainwise.ui.viewmodels.BiometricViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BiometricDevicesScreen(
    onNavigateBack: () -> Unit,
    viewModel: BiometricViewModel = viewModel()
) {
    val context = LocalContext.current
    val devices by viewModel.devices
    val isScanning by viewModel.isScanning

    LaunchedEffect(Unit) {
        viewModel.updateDeviceConnectionStatus(context)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Biometric Devices", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.scanForBiometricDevices(context) },
                containerColor = Orange,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = CircleShape
            ) {
                if (isScanning) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary, 
                        modifier = Modifier.size(24.dp), 
                        strokeWidth = 2.dp
                    )
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
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Connected Devices",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (pairedDevices.isEmpty()) {
                item {
                    Text(
                        "No connected devices found", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), 
                        fontSize = 13.sp
                    )
                }
            } else {
                items(pairedDevices) { device ->
                    DeviceCard(device)
                }
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Available Nearby",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            if (availableDevices.isEmpty()) {
                item {
                    Text(
                        "No other devices found", 
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), 
                        fontSize = 13.sp
                    )
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
                            CircularProgressIndicator(
                                color = Orange, 
                                modifier = Modifier.size(32.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "Searching for biometric devices...", 
                                color = MaterialTheme.colorScheme.onSurfaceVariant, 
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
                InfoNote("Biometric Syncing:\n\n" +
                        "1. Ensure your Watch is connected to the phone.\n" +
                        "2. Wear your Watch tightly during the session.\n" +
                        "3. Start a workout on your Watch (e.g. Samsung Health) to enable continuous monitoring.\n" +
                        "4. TrainWise will automatically sync your heart rate and calories at the end of the workout.")
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun DeviceCard(device: Device) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    device.icon, 
                    null, 
                    tint = Orange, 
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    device.name, 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    device.type, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontSize = 13.sp
                )
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
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            Icons.Outlined.Info, 
            null, 
            tint = Orange, 
            modifier = Modifier.size(20.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text, 
            color = MaterialTheme.colorScheme.onSurfaceVariant, 
            fontSize = 13.sp, 
            lineHeight = 20.sp
        )
    }
}
