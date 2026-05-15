package com.example.trainwise.data.repositories

import com.example.trainwise.data.models.CompletedWorkout
import com.example.trainwise.data.models.Exercise
import com.example.trainwise.data.models.Workout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class WorkoutRepository {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    val userId: String? get() = auth.currentUser?.uid

    fun getWorkouts(): Flow<List<Workout>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null
        
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            listenerRegistration?.remove()
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                listenerRegistration = db.collection("workouts")
                    .whereEqualTo("userId", uid)
                    .addSnapshotListener { snapshot, _ ->
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(Workout::class.java)?.copy(id = doc.id)
                            }.sortedByDescending { it.createdAt }
                            trySend(list)
                        }
                    }
            } else {
                trySend(emptyList())
            }
        }
        
        auth.addAuthStateListener(authListener)
        
        awaitClose {
            auth.removeAuthStateListener(authListener)
            listenerRegistration?.remove()
        }
    }

    fun getCompletedWorkouts(): Flow<List<CompletedWorkout>> = callbackFlow {
        var listenerRegistration: ListenerRegistration? = null
        
        val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            listenerRegistration?.remove()
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                listenerRegistration = db.collection("completed_workouts")
                    .whereEqualTo("userId", uid)
                    .addSnapshotListener { snapshot, error ->
                        if (snapshot != null) {
                            val list = snapshot.documents.mapNotNull { doc ->
                                doc.toObject(CompletedWorkout::class.java)?.copy(id = doc.id)
                            }.sortedByDescending { it.timestamp }
                            trySend(list)
                        }
                    }
            } else {
                trySend(emptyList())
            }
        }
        
        auth.addAuthStateListener(authListener)
        
        awaitClose {
            auth.removeAuthStateListener(authListener)
            listenerRegistration?.remove()
        }
    }

    suspend fun deleteWorkout(workoutId: String) {
        if (workoutId.isEmpty()) return
        db.collection("workouts").document(workoutId).delete().await()
    }

    suspend fun deleteCompletedWorkouts(workoutIds: Set<String>) {
        if (workoutIds.isEmpty()) return
        val batch = db.batch()
        workoutIds.forEach { id ->
            if (id.isNotEmpty()) {
                val docRef = db.collection("completed_workouts").document(id)
                batch.delete(docRef)
            }
        }
        batch.commit().await()
    }

    suspend fun saveCompletedWorkout(completed: CompletedWorkout) {
        val uid = userId ?: return
        val id = completed.id.ifEmpty { db.collection("completed_workouts").document().id }
        db.collection("completed_workouts").document(id).set(completed.copy(id = id, userId = uid)).await()
    }
    
    suspend fun saveWorkout(workout: Workout) {
        val uid = userId ?: return
        val id = workout.id.ifEmpty { db.collection("workouts").document().id }
        db.collection("workouts").document(id).set(workout.copy(id = id, userId = uid)).await()
    }

    fun getAllExercises(): List<Exercise> {
        return listOf(
            // Chest
            Exercise(1, "Bench Press", "Chest"),
            Exercise(2, "Push Ups", "Chest"),
            Exercise(13, "Incline Bench Press", "Chest"),
            Exercise(14, "Decline Bench Press", "Chest"),
            Exercise(15, "Chest Flys", "Chest"),
            Exercise(16, "Cable Crossover", "Chest"),
            Exercise(17, "Dips (Chest Focus)", "Chest"),

            // Back
            Exercise(3, "Pull Ups", "Back"),
            Exercise(4, "Deadlift", "Back"),
            Exercise(18, "Lat Pulldowns", "Back"),
            Exercise(19, "Seated Row", "Back"),
            Exercise(20, "Bent Over Row", "Back"),
            Exercise(21, "T-Bar Row", "Back"),
            Exercise(22, "Hyperextensions", "Back"),
            Exercise(23, "Single Arm Dumbbell Row", "Back"),

            // Legs
            Exercise(5, "Squats", "Legs"),
            Exercise(6, "Lunges", "Legs"),
            Exercise(24, "Leg Press", "Legs"),
            Exercise(25, "Leg Extension", "Legs"),
            Exercise(26, "Leg Curl", "Legs"),
            Exercise(27, "Calf Raises", "Legs"),
            Exercise(28, "Romanian Deadlift", "Legs"),
            Exercise(29, "Bulgarian Split Squats", "Legs"),
            Exercise(30, "Hack Squats", "Legs"),

            // Shoulders
            Exercise(7, "Shoulder Press", "Shoulders"),
            Exercise(8, "Lateral Raises", "Shoulders"),
            Exercise(31, "Front Raises", "Shoulders"),
            Exercise(32, "Reverse Flys", "Shoulders"),
            Exercise(33, "Arnold Press", "Shoulders"),
            Exercise(34, "Face Pulls", "Shoulders"),
            Exercise(35, "Upright Row", "Shoulders"),

            // Arms
            Exercise(9, "Bicep Curls", "Arms"),
            Exercise(10, "Tricep Dips", "Arms"),
            Exercise(36, "Hammer Curls", "Arms"),
            Exercise(37, "Preacher Curls", "Arms"),
            Exercise(38, "Skull Crushers", "Arms"),
            Exercise(39, "Overhead Tricep Extension", "Arms"),
            Exercise(40, "Concentration Curls", "Arms"),
            Exercise(41, "Tricep Pushdowns", "Arms"),

            // Core
            Exercise(11, "Plank", "Core"),
            Exercise(12, "Crunches", "Core"),
            Exercise(42, "Leg Raises", "Core"),
            Exercise(43, "Russian Twists", "Core"),
            Exercise(44, "Mountain Climbers", "Core"),
            Exercise(45, "Bicycle Crunches", "Core"),
            Exercise(46, "Dead Bug", "Core"),
            Exercise(47, "Hanging Leg Raises", "Core")
        )
    }
}
