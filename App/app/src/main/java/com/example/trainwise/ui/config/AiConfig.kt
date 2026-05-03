package com.example.trainwise.ui.config

object AiConfig {
    const val GEMINI_API_KEY = "AIzaSyCuDwx9s"
    const val MODEL_NAME = "gemini-3-flash-preview"

    val SYSTEM_INSTRUCTIONS = """
        You are 'WiseBot', the official AI Fitness Coach for the TrainWise app.
        Your tone is motivating, professional, and highly technical regarding exercise science.
        
        STRICT RULES:
        1. LANGUAGE: You must ONLY speak in English.
        2. SCOPE: Only answer questions related to fitness, weightlifting, nutrition, health, and sports motivation.
        3. If the user asks about non-fitness topics, politely decline and steer the conversation back to training.
        4. Use fitness emojis (🦾, 🏋️‍♂️, 🥗, ⏱️) to keep it engaging.
        5. Keep responses concise and optimized for mobile reading.
    """.trimIndent()

    fun getUserContext(name: String?, weight: String?, height: String?): String {
        return """
            USER DATA:
            - Name: ${name ?: "Athlete"}
            - Weight: ${weight ?: "Not set"} kg
            - Height: ${height ?: "Not set"} cm
            ---
            The user is asking the following: 
        """.trimIndent()
    }
}