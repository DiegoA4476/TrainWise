package com.example.trainwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trainwise.ui.components.CustomTextField
import com.example.trainwise.ui.theme.DarkBackground
import com.example.trainwise.ui.theme.Orange
import com.example.trainwise.ui.theme.White
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore // IMPORTANTE


@Composable
fun SignUpScreen(onNavigateToLogin: () -> Unit) {
    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance() // Instancia de la base de datos

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 40.dp, vertical = 60.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = White, fontWeight = FontWeight.Bold, fontSize = 48.sp)) {
                    append("Join\n")
                }
                withStyle(style = SpanStyle(color = Orange, fontWeight = FontWeight.Bold, fontSize = 56.sp)) {
                    append("WiseTrain")
                }
            },
            lineHeight = 56.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(text = "Full Name", color = White, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
        CustomTextField(
            value = name,
            onValueChange = { name = it; errorMessage = null },
            placeholder = "Enter your name",
            leadingIcon = { Icon(Icons.Outlined.Person, null, tint = White, modifier = Modifier.size(28.dp)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Email", color = White, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
        CustomTextField(
            value = email,
            onValueChange = { email = it; errorMessage = null },
            placeholder = "Enter your email",
            keyboardType = KeyboardType.Email,
            leadingIcon = { Icon(Icons.Outlined.Email, null, tint = White, modifier = Modifier.size(28.dp)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Password", color = White, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
        CustomTextField(
            value = password,
            onValueChange = { password = it; errorMessage = null },
            placeholder = "Create password",
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = White, modifier = Modifier.size(28.dp)) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Confirm Password", color = White, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
        CustomTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; errorMessage = null },
            placeholder = "Confirm password",
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = White, modifier = Modifier.size(28.dp)) }
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage!!, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                if (name.isBlank() || email.isBlank() || password.isBlank()) {
                    errorMessage = "Please fill in all fields"
                    return@Button
                }
                if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                    return@Button
                }

                isLoading = true
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val userId = user?.uid ?: ""


                            val profileUpdates = userProfileChangeRequest { displayName = name }
                            user?.updateProfile(profileUpdates)

                            val userProfile = hashMapOf(
                                "uid" to userId,
                                "username" to name,
                                "email" to email,
                                "phone" to "",
                                "height" to "",   // empty at start, can complete in account details
                                "weight" to ""
                            )

                            db.collection("users").document(userId)
                                .set(userProfile)
                                .addOnSuccessListener {
                                    isLoading = false
                                    onNavigateToLogin()
                                }
                                .addOnFailureListener { e ->
                                    isLoading = false
                                    errorMessage = "Error saving data: ${e.message}"
                                }
                        } else {
                            isLoading = false
                            val exception = task.exception
                            errorMessage = when (exception) {
                                is FirebaseAuthWeakPasswordException -> "Password is too weak"
                                else -> exception?.message ?: "Registration failed"
                            }
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth().height(70.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(35.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "SIGN UP", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Already a member? ", color = White, fontSize = 16.sp)
            TextButton(onClick = onNavigateToLogin) {
                Text(text = "Login", color = Orange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}