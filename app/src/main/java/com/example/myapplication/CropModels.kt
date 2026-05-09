package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class CropInput(
    @SerializedName("N") val n: Float,
    @SerializedName("P") val p: Float,
    @SerializedName("K") val k: Float,
    val temp: Float,
    val hum: Float,
    val ph: Float,
    val rain: Float
)

data class CropResponse(
    @SerializedName("Predicted_Yield") val predictedYield: Float,
    @SerializedName("Unit") val unit: String,
    @SerializedName("Suggestions") val suggestions: List<String>
)