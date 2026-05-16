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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainwise.ui.components.CustomTextField
import com.example.trainwise.ui.theme.*
import com.example.trainwise.ui.viewmodels.UserViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountDetailsScreen(
    onNavigateBack: () -> Unit,
    viewModel: UserViewModel = viewModel()
) {
    val userProfile = viewModel.userProfile
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val userId = auth.currentUser?.uid
    val context = LocalContext.current

    var username by remember(userProfile) { mutableStateOf(userProfile?.username ?: "") }
    var phone by remember(userProfile) { mutableStateOf(userProfile?.phone ?: "") }
    var height by remember(userProfile) { mutableStateOf(userProfile?.height ?: "") }
    var weight by remember(userProfile) { mutableStateOf(userProfile?.weight ?: "") }
    // Estados para los campos
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("********") }
    var profileImageBase64 by remember { mutableStateOf<String?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isSaving by remember { mutableStateOf(false) }

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
                        phone = document.getString("phone") ?: ""
                        height = document.getString("height") ?: ""
                        weight = document.getString("weight") ?: ""
                        profileImageBase64 = document.getString("profileImage")
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    isLoading = false
                }
        }
    }

    fun saveImageToFirestore(bitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
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
        topBar = {
            TopAppBar(
                title = { Text("Account Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (viewModel.isSaving) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Orange, strokeWidth = 2.dp)
                    } else {
                        TextButton(onClick = {
                            viewModel.updateAccountDetails(username, phone, height, weight, onNavigateBack)
                        }) {
                            Text("Save", color = Orange, fontWeight = FontWeight.Bold)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        if (viewModel.isLoading) {
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
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(2.dp, Orange, CircleShape)
                            .clickable { showImageSourceDialog = true },
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
                        modifier = Modifier
                            .size(36.dp)
                            .border(2.dp, DarkBackground, CircleShape)

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
                        value = userProfile?.email ?: "",
                        onValueChange = { /* Read only */ },
                        icon = Icons.Outlined.Email
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
            }
        }
    }
}

@Composable
fun InfoField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: ImageVector
) {
    Column {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        CustomTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = label,
            leadingIcon = {
                Icon(
                    icon,
                    null,
                    tint = Orange,
                    modifier = Modifier.size(20.dp)
                )
            }
        )
    }
}
