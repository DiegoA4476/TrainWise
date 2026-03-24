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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainwise.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onNavigateBack: () -> Unit
) {
    // Dynamic Colors based on Dark Mode state
    val backgroundColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground
    val cardColor = CardBackground
    val secondaryTextColor = GrayText

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Privacy & Security", color = textColor, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "Back", tint = textColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor
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
                    color = secondaryTextColor,
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
                    onCheckedChange = { onToggleDarkMode() },
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    cardColor = cardColor
                )
            }

            item {
                Text(
                    "Security Features",
                    color = secondaryTextColor,
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
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    cardColor = cardColor
                )
            }

            item {
                SecurityActionItem(
                    title = "Change Password",
                    subtitle = "Update your login credentials",
                    icon = Icons.Outlined.Lock,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    cardColor = cardColor
                )
            }

            item {
                Text(
                    "Privacy Control",
                    color = secondaryTextColor,
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
                    onCheckedChange = { },
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    cardColor = cardColor
                )
            }

            item {
                SecurityActionItem(
                    title = "Clear Local Cache",
                    subtitle = "Remove offline data and reset app state",
                    icon = Icons.Outlined.DeleteSweep,
                    textColor = textColor,
                    secondaryTextColor = secondaryTextColor,
                    cardColor = cardColor
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
    onCheckedChange: (Boolean) -> Unit,
    textColor: Color,
    secondaryTextColor: Color,
    cardColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = cardColor),
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
                Text(title, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = secondaryTextColor, fontSize = 12.sp)
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = White,
                    checkedTrackColor = Orange,
                    uncheckedThumbColor = GrayText,
                    uncheckedTrackColor = cardColor.copy(alpha = 0.5f)
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
    textColor: Color,
    secondaryTextColor: Color,
    cardColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* No action implemented as requested */ },
        colors = CardDefaults.cardColors(containerColor = cardColor),
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
                Text(title, color = textColor, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Text(subtitle, color = secondaryTextColor, fontSize = 12.sp)
            }

            Icon(Icons.Outlined.ChevronRight, null, tint = secondaryTextColor)
        }
    }
}
