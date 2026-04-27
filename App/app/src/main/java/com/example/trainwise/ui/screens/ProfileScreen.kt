package com.example.trainwise.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainwise.ui.theme.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

@Composable
fun ProfileScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToWorkouts: () -> Unit,
    onNavigateToGuide: () -> Unit,
    onNavigateToAccountDetails: () -> Unit,
    onNavigateToBiometrics: () -> Unit,
    onNavigateToSecurity: () -> Unit,
    onNavigateToNotifications: () -> Unit,
    onNavigateToTrainingHistory: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var profileImageBase64 by remember { mutableStateOf<String?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    
    val profileBitmap = remember(profileImageBase64) {
        if (profileImageBase64 != null) {
            try {
                val decodedString = Base64.decode(profileImageBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            } catch (e: Exception) {
                null
            }
        } else null
    }

    LaunchedEffect(userId) {
        userId?.let { id ->
            db.collection("users").document(id).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        username = document.getString("username") ?: ""
                        email = document.getString("email") ?: ""
                        profileImageBase64 = document.getString("profileImage")
                    }
                }
        }
    }

    fun saveImageToFirestore(bitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        // Compress to keep it reasonably small for Firestore
        bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        val byteArray = outputStream.toByteArray()
        val base64 = Base64.encodeToString(byteArray, Base64.DEFAULT)
        
        userId?.let { id ->
            db.collection("users").document(id)
                .update("profileImage", base64)
                .addOnSuccessListener {
                    profileImageBase64 = base64
                }
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            saveImageToFirestore(bitmap)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            try {
                val bitmap = if (Build.VERSION.SDK_INT < 28) {
                    MediaStore.Images.Media.getBitmap(context.contentResolver, it)
                } else {
                    val source = ImageDecoder.createSource(context.contentResolver, it)
                    ImageDecoder.decodeBitmap(source)
                }
                saveImageToFirestore(bitmap)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Select Profile Picture", color = White) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            cameraLauncher.launch()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Orange)
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, null, modifier = Modifier.padding(end = 8.dp))
                        Text("Take Photo")
                    }
                    Button(
                        onClick = {
                            showImageSourceDialog = false
                            galleryLauncher.launch("image/*")
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = CardBackground)
                    ) {
                        Icon(Icons.Outlined.PhotoLibrary, null, modifier = Modifier.padding(end = 8.dp))
                        Text("Choose from Gallery")
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false }) {
                    Text("Cancel", color = GrayText)
                }
            },
            containerColor = DarkBackground,
            shape = RoundedCornerShape(24.dp)
        )
    }

    Scaffold(
        bottomBar = {
            ProfileBottomNavigationBar(
                onHomeClick = onNavigateToHome,
                onWorkoutsClick = onNavigateToWorkouts,
                onGuideClick = onNavigateToGuide
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                Spacer(modifier = Modifier.height(40.dp))

                Box(
                    contentAlignment = Alignment.BottomEnd,
                    modifier = Modifier.clickable { showImageSourceDialog = true }
                ) {
                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .clip(CircleShape)
                            .background(CardBackground)
                            .border(2.dp, Orange, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileBitmap != null) {
                            Image(
                                bitmap = profileBitmap.asImageBitmap(),
                                contentDescription = "Profile Picture",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Icon(
                                Icons.Outlined.Person,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize().padding(20.dp),
                                tint = GrayText
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = Orange,
                        modifier = Modifier.size(32.dp).border(2.dp, MaterialTheme.colorScheme.background, CircleShape)
                    ) {
                        Icon(Icons.Outlined.PhotoCamera, null, tint = White, modifier = Modifier.padding(6.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(username, color = MaterialTheme.colorScheme.onBackground, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(email, color = GrayText, fontSize = 14.sp)

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        modifier = Modifier.weight(1f).clickable { onNavigateToTrainingHistory() }, 
                        icon = Icons.Outlined.History, 
                        value = "24", 
                        label = "Sessions"
                    )
                    StatCard(Modifier.weight(1f), Icons.Outlined.EmojiEvents, "12", "Badges")
                    StatCard(Modifier.weight(1f), Icons.Outlined.MonitorWeight, "78kg", "Weight")
                }

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    "Settings",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))
            }

            item { 
                ProfileMenuItem(
                    Icons.Outlined.PersonOutline, 
                    "Account Details",
                    onClick = onNavigateToAccountDetails
                ) 
            }
            item { 
                ProfileMenuItem(
                    Icons.Outlined.History, 
                    "Training History",
                    onClick = onNavigateToTrainingHistory
                ) 
            }
            item { 
                ProfileMenuItem(
                    Icons.Outlined.Watch, 
                    "Biometric Devices",
                    onClick = onNavigateToBiometrics
                ) 
            }
            item { 
                ProfileMenuItem(
                    Icons.Outlined.Notifications, 
                    "Notifications",
                    onClick = onNavigateToNotifications
                ) 
            }
            item { 
                ProfileMenuItem(
                    Icons.Outlined.Shield, 
                    "Privacy & Security",
                    onClick = onNavigateToSecurity
                ) 
            }
            item {
                ProfileMenuItem(
                    Icons.Outlined.Logout,
                    "Sign Out",
                    textColor = Color.Red,
                    iconColor = Color.Red
                )
            }

            item { Spacer(modifier = Modifier.height(20.dp)) }
        }
    }
}

@Composable
fun StatCard(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    value: String,
    label: String
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, null, tint = Orange, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            Text(text = label, color = GrayText, fontSize = 12.sp)
        }
    }
}

@Composable
fun ProfileMenuItem(
    icon: ImageVector,
    title: String,
    textColor: Color = White,
    iconColor: Color = Orange,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = iconColor, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = title, color = if (textColor == White) MaterialTheme.colorScheme.onBackground else textColor, fontSize = 16.sp)
            Spacer(modifier = Modifier.weight(1f))
            Icon(Icons.Outlined.ChevronRight, null, tint = GrayText)
        }
    }
}

@Composable
fun ProfileBottomNavigationBar(
    onHomeClick: () -> Unit,
    onWorkoutsClick: () -> Unit,
    onGuideClick: () -> Unit
) {
    NavigationBar(containerColor = Color.Black, contentColor = White) {
        NavigationBarItem(
            selected = false,
            onClick = onHomeClick,
            icon = { Icon(Icons.Outlined.Home, null) },
            label = { Text("Home") },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = White, unselectedTextColor = White)
        )
        NavigationBarItem(
            selected = false,
            onClick = onWorkoutsClick,
            icon = { Icon(Icons.Outlined.FitnessCenter, null) },
            label = { Text("Workouts") },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = White, unselectedTextColor = White)
        )
        NavigationBarItem(
            selected = false,
            onClick = onGuideClick,
            icon = { Icon(Icons.Outlined.MenuBook, null) },
            label = { Text("Guide") },
            colors = NavigationBarItemDefaults.colors(unselectedIconColor = White, unselectedTextColor = White)
        )
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Outlined.Person, null) },
            label = { Text("Profile") },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = White,
                indicatorColor = Orange,
                selectedTextColor = White
            )
        )
    }
}
