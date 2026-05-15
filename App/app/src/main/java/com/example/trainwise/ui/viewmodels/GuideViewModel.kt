package com.example.trainwise.ui.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trainwise.data.models.Message
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch
import com.example.trainwise.ui.config.AiConfig
import com.example.trainwise.data.models.UserProfile
import com.example.trainwise.data.models.Workout
import com.example.trainwise.data.models.SelectedExercise
import com.example.trainwise.data.models.Exercise
import com.example.trainwise.data.repositories.UserRepository
import com.example.trainwise.data.repositories.WorkoutRepository
import com.google.firebase.auth.FirebaseAuth
import org.json.JSONObject

class GuideViewModel(
    private val userRepository: UserRepository = UserRepository(),
    private val workoutRepository: WorkoutRepository = WorkoutRepository()
) : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val generativeModel = GenerativeModel(
        modelName = AiConfig.MODEL_NAME,
        apiKey = AiConfig.GEMINI_API_KEY,
        systemInstruction = content {
            text(AiConfig.SYSTEM_INSTRUCTIONS + AiConfig.WORKOUT_INSTRUCTIONS) 
        }
    )

    private val chatSession = generativeModel.startChat()
    val chatMessages = mutableStateListOf<Message>()
    var isLoading by mutableStateOf(false)
        private set
        
    private var userData: UserProfile? = null

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            userData = userRepository.getUserProfile()
            if (chatMessages.isEmpty()) {
                val name = userData?.username ?: "Athlete"
                chatMessages.add(Message("Hello $name! I'm WiseBot. Ready to crush your workout today?", false))
            }
        }
    }

    fun sendMessage(userText: String) {
        chatMessages.add(Message(userText, true))
        isLoading = true

        viewModelScope.launch {
            try {
                val profileContext = AiConfig.getUserContext(
                    userData?.username,
                    userData?.weight,
                    userData?.height
                )

                val response = chatSession.sendMessage(profileContext + userText)

                response.text?.let {
                    chatMessages.add(Message(it, false))
                }
            } catch (e: Exception) {
                android.util.Log.e("GEMINI_ERROR", "Details: ${e.message}", e)
                
                val errorMessage = when {
                    e.message?.contains("503") == true || e.message?.contains("high demand") == true -> {
                        "WiseBot is currently very busy helping other athletes. Please try again in a moment! 💪"
                    }
                    else -> "Oops! Something went wrong. Please check your connection."
                }
                chatMessages.add(Message(errorMessage, false))
            } finally {
                isLoading = false
            }
        }
    }

    suspend fun importWorkout(jsonString: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        try {
            val userId = auth.currentUser?.uid ?: return
            val json = JSONObject(jsonString)
            
            val exercisesArray = json.getJSONArray("exercises")
            val selectedExercises = mutableListOf<SelectedExercise>()
            
            for (i in 0 until exercisesArray.length()) {
                val exJson = exercisesArray.getJSONObject(i)
                selectedExercises.add(
                    SelectedExercise(
                        exercise = Exercise(
                            id = (100..999).random(),
                            name = exJson.getString("name"),
                            muscleGroup = exJson.getString("muscleGroup")
                        ),
                        reps = exJson.getInt("reps"),
                        sets = exJson.getInt("sets")
                    )
                )
            }

            val workout = Workout(
                userId = userId,
                title = json.getString("title"),
                category = json.getString("category"),
                exercises = selectedExercises,
                restTime = json.optInt("restTime", 60)
            )

            workoutRepository.saveWorkout(workout)
            onSuccess()

        } catch (e: Exception) {
            onError("Invalid workout format: ${e.message}")
        }
    }
}
