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
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onNavigateBack: () -> Unit,
    onAccountDeleted: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    
    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var deletePassword by remember { mutableStateOf("") }
    
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(authViewModel.successMessage, authViewModel.errorMessage) {
        authViewModel.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearMessages()
        }
        authViewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            authViewModel.clearMessages()
        }
    }

    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { 
                showPasswordDialog = false
                currentPassword = ""
                newPassword = ""
            },
            title = { Text(("Change Password")) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = currentPassword,
                        onValueChange = { currentPassword = it },
                        label = { Text("Current Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        authViewModel.changePassword(currentPassword, newPassword)
                        showPasswordDialog = false
                        currentPassword = ""
                        newPassword = ""
                    },
                    enabled = newPassword.length >= 6 && currentPassword.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Orange)
                ) { Text("Update") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPasswordDialog = false
                    currentPassword = ""
                    newPassword = ""
                }) { Text("Cancel") }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { 
                showDeleteDialog = false
                deletePassword = ""
            },
            title = { Text("Delete account", color = Color.Red) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("To confirm deletion, please enter your current password. This will permanently delete your account and all data.")
                    OutlinedTextField(
                        value = deletePassword,
                        onValueChange = { deletePassword = it },
                        label = { Text("Confirm password") },
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { 
                        authViewModel.deleteAccount(deletePassword) {
                            onAccountDeleted()
                        }
                        showDeleteDialog = false
                    },
                    enabled = deletePassword.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showDeleteDialog = false
                    deletePassword = ""
                }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Spacer(modifier = Modifier.height(8.dp)) }

            item {
                SectionLabel("Preferences")
                SecurityToggleItem(
                    title = "Light Mode",
                    subtitle = "Switch between dark and light themes",
                    icon = if (isDarkMode) Icons.Outlined.DarkMode else Icons.Outlined.LightMode,
                    checked = isDarkMode,
                    onCheckedChange = { onToggleDarkMode() }
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                SecurityActionItem(
                    title = "Language",
                    subtitle = "English (Coming soon: Portuguese)",
                    icon = Icons.Outlined.Language,
                    onClick = {
                        scope.launch {
                            snackbarHostState.showSnackbar("Language selection will be available in a future update!")
                        }
                    }
                )
            }

            item {
                SectionLabel("Security Features")
                SecurityActionItem(
                    title = "Change Password",
                    subtitle = "Update your login credentials",
                    icon = Icons.Outlined.Lock,
                    onClick = { showPasswordDialog = true }
                )
            }

            item {
                SectionLabel("Danger zone")
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { showDeleteDialog = true },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    ),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.2f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Outlined.DeleteForever, null, tint = Color.Red)
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Delete account", color = Color.Red, fontWeight = FontWeight.Bold)
                            Text("Delete all the information of the user", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                        }
                    }
                }
            }
            
            item { Spacer(modifier = Modifier.height(40.dp)) }
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        color = Orange,
        fontSize = 13.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
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
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(Orange.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), Alignment.Center) {
                Icon(icon, null, tint = Orange, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Switch(
                checked = checked, 
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(checkedTrackColor = Orange)
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
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(40.dp).background(Orange.copy(alpha = 0.1f), RoundedCornerShape(8.dp)), Alignment.Center) {
                Icon(icon, null, tint = Orange, modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
            Icon(Icons.Outlined.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
