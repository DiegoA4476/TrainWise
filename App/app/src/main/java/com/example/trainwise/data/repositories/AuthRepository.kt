package com.example.trainwise.data.repositories

import com.example.trainwise.data.models.UserProfile
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    val currentUser: FirebaseUser? get() = auth.currentUser

    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) Result.success(user)
            else Result.failure(Exception("User is null"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUp(name: String, email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user
            if (user != null) {
                user.updateProfile(userProfileChangeRequest { displayName = name }).await()
                
                val userProfile = UserProfile(
                    uid = user.uid,
                    username = name,
                    email = email
                )
                db.collection("users").document(user.uid).set(userProfile).await()
                
                Result.success(user)
            } else {
                Result.failure(Exception("User creation failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val credential = EmailAuthProvider.getCredential(user.email!!, currentPassword)
            user.reauthenticate(credential).await()
            user.updatePassword(newPassword).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(password: String): Result<Unit> {
        return try {
            val user = auth.currentUser ?: return Result.failure(Exception("No user logged in"))
            val uid = user.uid
            val email = user.email ?: return Result.failure(Exception("User email not found"))


            val credential = EmailAuthProvider.getCredential(email, password)
            user.reauthenticate(credential).await()

            try {
                val workouts = db.collection("workouts").whereEqualTo("userId", uid).get().await()
                for (doc in workouts.documents) {
                    doc.reference.delete().await()
                }

                val completed = db.collection("completed_workouts").whereEqualTo("userId", uid).get().await()
                for (doc in completed.documents) {
                    doc.reference.delete().await()
                }

                db.collection("users").document(uid).delete().await()
            } catch (e: Exception) {

            }

            user.delete().await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
