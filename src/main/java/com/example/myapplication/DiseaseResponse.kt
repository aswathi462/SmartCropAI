package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class DiseaseResponse(

    val status: String,
    val diagnosis: String,

    val confidence: String,   // NEW FIELD

    @SerializedName("firebase_id")
    val firebaseId: String,

    val recommendation: Recommendation // NEW BLOCK
)

data class Recommendation(
    val treatments: List<String>,
    val preventive: List<String>,
    val fertilizer: List<String>
)