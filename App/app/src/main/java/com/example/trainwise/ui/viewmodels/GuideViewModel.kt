package com.example.trainwise.ui.viewmodels

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainwise.ui.screens.Message
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import com.example.trainwise.ui.config.AiConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.example.trainwise.data.models.UserProfile


class GuideViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val generativeModel = GenerativeModel(
        modelName = AiConfig.MODEL_NAME,
        apiKey = AiConfig.GEMINI_API_KEY,
        systemInstruction = content { text(AiConfig.SYSTEM_INSTRUCTIONS) }
    )

    private val chatSession = generativeModel.startChat()
    val chatMessages = mutableStateListOf<Message>()
    private var userData: UserProfile? = null

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                userData = doc.toObject(UserProfile::class.java)
                if (chatMessages.isEmpty()) {
                    val name = userData?.username ?: "Athlete"
                    chatMessages.add(Message("Hello $name! I'm WiseBot. Ready to crush your workout today?", false))
                }
            }
    }

    fun sendMessage(userText: String) {
        chatMessages.add(Message(userText, true))

        viewModelScope.launch {
            try {
                val profileContext = AiConfig.getUserContext(
                    userData?.username,
                    userData?.weight,
                    userData?.height
                )

                // We send context + text to ensure it remembers who the user is
                val response = chatSession.sendMessage(profileContext + userText)

                response.text?.let {
                    chatMessages.add(Message(it, false))
                }
            } catch (e: Exception) {
                // This will show the real error in the "Logcat" tab of Android Studio
                android.util.Log.e("GEMINI_ERROR", "Details: ${e.message}", e)

                chatMessages.add(Message("Error: ${e.localizedMessage}", false))
            }
        }
    }
}
