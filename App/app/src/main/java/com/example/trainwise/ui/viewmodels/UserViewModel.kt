package com.example.trainwise.ui.viewmodels

import android.graphics.Bitmap
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainwise.data.models.UserProfile
import com.example.trainwise.data.repositories.UserRepository
import com.example.trainwise.data.repositories.AuthRepository
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

class UserViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    var userProfile by mutableStateOf<UserProfile?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var isSaving by mutableStateOf(false)
        private set

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            isLoading = true
            userProfile = userRepository.getUserProfile()
            isLoading = false
        }
    }

    fun updateProfileImage(bitmap: Bitmap) {
        viewModelScope.launch {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
            val byteArray = outputStream.toByteArray()
            val base64 = Base64.encodeToString(byteArray, Base64.DEFAULT)

            val success = userRepository.updateUserProfile(mapOf("profileImage" to base64))
            if (success) {
                userProfile = userProfile?.copy(profileImage = base64)
            }
        }
    }

    fun updateAccountDetails(
        username: String,
        phone: String,
        height: String,
        weight: String,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            isSaving = true
            val updates = mapOf(
                "username" to username,
                "phone" to phone,
                "height" to height,
                "weight" to weight
            )
            val success = userRepository.updateUserProfile(updates)
            if (success) {
                userProfile = userProfile?.copy(
                    username = username,
                    phone = phone,
                    height = height,
                    weight = weight
                )
                onSuccess()
            }
            isSaving = false
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
