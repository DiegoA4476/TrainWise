package com.example.trainwise.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainwise.ui.components.CustomTextField
import com.example.trainwise.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    onNavigateBack: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid

    // Estados para los campos
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("********") } // La contraseña no se suele editar aquí por seguridad
    var phone by remember { mutableStateOf("") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

    LaunchedEffect(userId) {
        userId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        username = document.getString("username") ?: ""
                        email = document.getString("email") ?: ""
                        phone = document.getString("phone") ?: ""
                        height = document.getString("height") ?: ""
                        weight = document.getString("weight") ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }
    val saveChanges = {
        if (userId != null) {
            isSaving = true
            val updatedData = mapOf(
                "username" to username,
                "phone" to phone,
                "height" to height,
                "weight" to weight
            )
            db.collection("users").document(userId).update(updatedData)
                .addOnSuccessListener {
                    isSaving = false
                    onNavigateBack()
                }
                .addOnFailureListener {
                    isSaving = false
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Account Details", color = White, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back", tint = White)
                    }
                },
                actions = {
                    if (isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Orange, strokeWidth = 2.dp)
                    } else {
                        TextButton(onClick = { saveChanges() }) {
                            Text("Save", color = Orange, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Orange)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(30.dp))

                // Profile Picture Section
                Box(contentAlignment = Alignment.BottomEnd) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(CardBackground)
                            .border(2.dp, Orange, CircleShape)
                            .clickable { /* Lógica para cambiar foto */ }
                    ) {
                        Icon(
                            Icons.Outlined.Person,
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize().padding(20.dp),
                            tint = GrayText
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = Orange,
                        modifier = Modifier
                            .size(36.dp)
                            .border(2.dp, DarkBackground, CircleShape)
                            .clickable { /* Lógica para cambiar foto */ }
                    ) {
                        Icon(
                            Icons.Outlined.CameraAlt,
                            null,
                            tint = White,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Change Profile Picture", color = Orange, fontSize = 14.sp, fontWeight = FontWeight.Medium)

                Spacer(modifier = Modifier.height(40.dp))

                // Information Fields
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    InfoField(
                        label = "Username",
                        value = username,
                        onValueChange = { username = it },
                        icon = Icons.Outlined.Person
                    )
                    InfoField(
                        label = "Email Address",
                        value = email,
                        onValueChange = { /* El email suele ser de solo lectura */ },
                        icon = Icons.Outlined.Email
                    )
                    InfoField(
                        label = "Password",
                        value = password,
                        onValueChange = { password = it },
                        icon = Icons.Outlined.Lock,
                        isPassword = true
                    )
                    InfoField(
                        label = "Phone Number",
                        value = phone,
                        onValueChange = { phone = it },
                        icon = Icons.Outlined.Phone
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            InfoField(
                                label = "Height (cm)",
                                value = height,
                                onValueChange = { height = it },
                                icon = Icons.Outlined.Height
                            )
                        }
                        Box(modifier = Modifier.weight(1f)) {
                            InfoField(
                                label = "Weight (kg)",
                                value = weight,
                                onValueChange = { weight = it },
                                icon = Icons.Outlined.MonitorWeight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(40.dp))

                Button(
                    onClick = { /* complete to deactivate account */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    border = BorderStroke(1.dp, Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Deactivate Account", color = Color.Red, modifier = Modifier.padding(vertical = 8.dp))
                }

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun InfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector,
    isPassword: Boolean = false
) {
    Column {
        Text(label, color = GrayText, fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
        CustomTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = label,
            leadingIcon = { Icon(icon, null, tint = Orange, modifier = Modifier.size(20.dp)) }
        )
    }
}