package com.example.trainwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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

@Composable
fun LoginScreen(onNavigateToSignUp: () -> Unit, onLoginSuccess: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .padding(horizontal = 40.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Welcome Text
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = White, fontWeight = FontWeight.Bold, fontSize = 48.sp)) {
                    append("Welcome to\n")
                }
                withStyle(style = SpanStyle(color = Orange, fontWeight = FontWeight.Bold, fontSize = 56.sp)) {
                    append("WiseTrain")
                }
            },
            lineHeight = 56.sp
        )

        Spacer(modifier = Modifier.height(80.dp))

        // Username Field
        Text(text = "Username", color = White, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
        CustomTextField(
            value = username,
            onValueChange = { 
                username = it
                showError = false
            },
            placeholder = "Enter username",
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(32.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Password Field
        Text(text = "Password", color = White, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
        CustomTextField(
            value = password,
            onValueChange = { 
                password = it
                showError = false
            },
            placeholder = "Enter password",
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(32.dp)
                )
            }
        )

        if (showError) {
            Text(
                text = "Invalid username or password",
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        // Login Button
        Button(
            onClick = { 
                if (username == "123" && password == "123") {
                    onLoginSuccess()
                } else {
                    showError = true
                }
            },
            modifier = Modifier.fillMaxWidth().height(70.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Orange),
            shape = RoundedCornerShape(35.dp)
        ) {
            Text(text = "LOGIN", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "New here? ", color = White, fontSize = 16.sp)
            TextButton(onClick = onNavigateToSignUp, contentPadding = PaddingValues(0.dp)) {
                Text(text = "Sign up", color = Orange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
