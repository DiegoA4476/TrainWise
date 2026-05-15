package com.example.trainwise.data.repositories

import com.example.trainwise.data.models.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val userId: String? get() = auth.currentUser?.uid

    suspend fun getUserProfile(): UserProfile? {
        val uid = userId ?: return null
        return try {
            val document = db.collection("users").document(uid).get().await()
            document.toObject(UserProfile::class.java)
        } catch (_: Exception) {
            null
        }
    }

    suspend fun updateUserProfile(updates: Map<String, Any>): Boolean {
        val uid = userId ?: return false
        return try {
            db.collection("users").document(uid).update(updates).await()
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun saveUserProfile(profile: UserProfile): Boolean {
        val uid = userId ?: return false
        return try {
            db.collection("users").document(uid).set(profile).await()
            true
        } catch (_: Exception) {
            false
        }
    }
}
