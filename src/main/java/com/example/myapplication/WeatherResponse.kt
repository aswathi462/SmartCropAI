package com.example.myapplication

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    @SerializedName("main") val mainMetrics: MainMetrics?,
    @SerializedName("rain") val rainMetrics: RainMetrics?
)

data class MainMetrics(
    @SerializedName("temp") val temperature: Double,
    @SerializedName("humidity") val humidity: Int
)

data class RainMetrics(
    @SerializedName("1h") val rain1h: Double? = 0.0
)