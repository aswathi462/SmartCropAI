package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class DiseaseResponse(
    val status: String,
    val diagnosis: String?,      // Nullable because a 400 rejection won't include this
    val confidence: String?,     // Nullable
    @SerializedName("firebase_id")
    val firebaseId: String?,    // Nullable maps snake_case to camelCase
    val recommendation: Recommendation?, // Nested structural data block
    val message: String?         // Captures explicit warning messages from the backend
)

data class Recommendation(
    val treatments: List<String>,
    val preventive: List<String>,
    val fertilizer: List<String>
)