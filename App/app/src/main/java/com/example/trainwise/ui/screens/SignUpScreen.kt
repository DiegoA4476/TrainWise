package com.example.trainwise.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.trainwise.ui.components.CustomTextField
import com.example.trainwise.ui.theme.*
import com.example.trainwise.ui.viewmodels.AuthViewModel

@Composable
fun SignUpScreen(
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }

    val handleSignUp: () -> Unit = {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            validationError = "Please fill in all fields"
        } else if (password != confirmPassword) {
            validationError = "Passwords do not match"
        } else {
            validationError = null
            viewModel.signUp(name, email, password, onNavigateToLogin)
        }
    }

    val displayError = validationError ?: viewModel.errorMessage

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 40.dp, vertical = 60.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.Start
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = buildAnnotatedString {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.Bold, fontSize = 48.sp)) {
                    append("Join\n")
                }
                withStyle(style = SpanStyle(color = Orange, fontWeight = FontWeight.Bold, fontSize = 56.sp)) {
                    append("WiseTrain")
                }
            },
            lineHeight = 56.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Text(text = "Full Name", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
        CustomTextField(
            value = name,
            onValueChange = { name = it; viewModel.clearError(); },
            placeholder = "Enter your name",
            imeAction = ImeAction.Next,
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Outlined.Person, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onBackground, 
                    modifier = Modifier.size(28.dp)
                ) 
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Email", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
        CustomTextField(
            value = email,
            onValueChange = { email = it; viewModel.clearError(); },
            placeholder = "Enter your email",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next,
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Outlined.Email, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onBackground, 
                    modifier = Modifier.size(28.dp)
                ) 
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Password", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
        CustomTextField(
            value = password,
            onValueChange = { password = it; viewModel.clearError(); },
            placeholder = "Create password",
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Next,
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Outlined.Lock, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onBackground, 
                    modifier = Modifier.size(28.dp)
                ) 
            }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(text = "Confirm Password", color = MaterialTheme.colorScheme.onBackground, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
        CustomTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it; viewModel.clearError(); },
            placeholder = "Confirm password",
            visualTransformation = PasswordVisualTransformation(),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            keyboardActions = KeyboardActions(onDone = { handleSignUp() }),
            leadingIcon = { 
                Icon(
                    imageVector = Icons.Outlined.Lock, 
                    contentDescription = null, 
                    tint = MaterialTheme.colorScheme.onBackground, 
                    modifier = Modifier.size(28.dp)
                ) 
            }
        )

        if (displayError != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = displayError, color = Color.Red, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = handleSignUp,
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
                Text(text = "SIGN UP", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Footer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "Already a member? ", color = MaterialTheme.colorScheme.onBackground, fontSize = 16.sp)
            TextButton(onClick = onNavigateToLogin) {
                Text(text = "Login", color = Orange, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
