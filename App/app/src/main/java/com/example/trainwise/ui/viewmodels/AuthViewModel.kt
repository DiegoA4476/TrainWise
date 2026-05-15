package com.example.trainwise.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainwise.data.repositories.AuthRepository
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    var currentUser by mutableStateOf<FirebaseUser?>(repository.currentUser)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var successMessage by mutableStateOf<String?>(null)
        private set

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.login(email, password)
            result.onSuccess {
                currentUser = it
                onSuccess()
            }.onFailure {
                errorMessage = it.message ?: "Login failed"
            }
            isLoading = false
        }
    }

    fun signUp(name: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            val result = repository.signUp(name, email, password)
            result.onSuccess {
                currentUser = it
                onSuccess()
            }.onFailure {
                errorMessage = it.message ?: "Sign up failed"
            }
            isLoading = false
        }
    }

    fun changePassword(newPassword: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            val result = repository.changePassword(newPassword)
            result.onSuccess {
                successMessage = "Password updated successfully"
            }.onFailure {
                errorMessage = it.message ?: "Failed to update password"
            }
            isLoading = false
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun clearMessages() {
        errorMessage = null
        successMessage = null
    }
}
