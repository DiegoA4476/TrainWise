package com.example.trainwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainwise.ui.components.CustomTextField
import com.example.trainwise.ui.theme.Orange
import com.example.trainwise.ui.viewmodels.AuthViewModel

@Composable
fun LoginScreen(
    onNavigateToSignUp: () -> Unit,
    onLoginSuccess: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val handleLogin = {
        if (email.isNotEmpty() && password.isNotEmpty()) {
            viewModel.login(email, password, onLoginSuccess)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 40.dp, vertical = 60.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Welcome Text
        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 48.sp)) {
                    append("Welcome to\n")
                }
                withStyle(style = SpanStyle(color = Orange, fontWeight = FontWeight.Bold, fontSize = 56.sp)) {
                    append("TrainWise")
                }
            },
            lineHeight = 56.sp
        )

        Spacer(modifier = Modifier.height(80.dp))

        // email Field
        Text(text = "Email", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
        CustomTextField(
            value = email,
            onValueChange = { 
                email = it
                viewModel.clearError()
            },
            placeholder = "Enter email",
            imeAction = ImeAction.Next,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(32.dp)
                )
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Password Field
        Text(text = "Password", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, modifier = Modifier.padding(bottom = 8.dp))
        CustomTextField(
            value = password,
            onValueChange = { 
                password = it
                viewModel.clearError()
            },
            placeholder = "Enter password",
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { handleLogin() }),
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(32.dp)
                )
            }
        )

        viewModel.errorMessage?.let { error ->
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(64.dp))

        // Login Button
        Button(
            onClick = handleLogin,
            modifier = Modifier.fillMaxWidth().height(70.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Orange,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            shape = RoundedCornerShape(35.dp),
            enabled = !viewModel.isLoading
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "LOGIN", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "New here? ", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
            TextButton(onClick = onNavigateToSignUp, contentPadding = PaddingValues(0.dp)) {
                Text(text = "Sign up", color = Orange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
