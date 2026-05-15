package com.example.trainwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainwise.ui.theme.*
import com.example.trainwise.ui.viewmodels.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onNavigateBack: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authViewModel.successMessage) {
        authViewModel.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearMessages()
        }
    }

    LaunchedEffect(authViewModel.errorMessage) {
        authViewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearMessages()
        }
    }

    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { 
                showChangePasswordDialog = false
                newPassword = ""
                confirmPassword = ""
            },
            title = { Text("Change Password") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPassword == confirmPassword && newPassword.isNotEmpty()) {
                            authViewModel.changePassword(newPassword)
                            showChangePasswordDialog = false
                            newPassword = ""
                            confirmPassword = ""
                        }
                    },
                    enabled = newPassword.isNotEmpty() && newPassword == confirmPassword,
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showChangePasswordDialog = false
                    newPassword = ""
                    confirmPassword = ""
                }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    "Appearance",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            item {
                SecurityToggleItem(
                    title = "Light Mode",
                    subtitle = "Switch between dark and light themes",
                    icon = if (!isDarkMode) Icons.Outlined.LightMode else Icons.Outlined.DarkMode,
                    checked = !isDarkMode,
                    onCheckedChange = { onToggleDarkMode() }
                )
            }

            item {
                Text(
                    "Security Features",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                SecurityActionItem(
                    title = "Two-Factor Authentication",
                    subtitle = "Add an extra layer of security",
                    icon = Icons.Outlined.VerifiedUser,
                    onClick = { /* Not implemented */ }
                )
            }

            item {
                SecurityActionItem(
                    title = "Change Password",
                    subtitle = "Update your login credentials",
                    icon = Icons.Outlined.Lock,
                    onClick = { showChangePasswordDialog = true }
                )
            }

            item {
                Text(
                    "Privacy Control",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(top = 16.dp)
                )
            }

            item {
                SecurityToggleItem(
                    title = "Data Sharing",
                    subtitle = "Share anonymized data to improve AI",
                    icon = Icons.Outlined.Share,
                    checked = true,
                    onCheckedChange = { }
                )
            }

            item {
                SecurityActionItem(
                    title = "Clear Local Cache",
                    subtitle = "Remove offline data and reset app state",
                    icon = Icons.Outlined.DeleteSweep,
                    onClick = { /* Not implemented */ }
                )
            }
            
            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun SecurityToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Orange.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Orange, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontSize = 12.sp
                )
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Orange,
                    uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                    uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun SecurityActionItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Orange.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = Orange, modifier = Modifier.size(24.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, 
                    color = MaterialTheme.colorScheme.onSurface, 
                    fontSize = 16.sp, 
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant, 
                    fontSize = 12.sp
                )
            }

            Icon(
                Icons.Outlined.ChevronRight, 
                null, 
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
