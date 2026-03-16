package com.example.myapplication

import kotlin.math.abs
import kotlin.math.roundToInt

object YieldPredictionEngine {

    data class VarietyProfile(
        val name: String,
        val baseYieldTonsPerAcre: Double,
        val nitrogenRange: ClosedFloatingPointRange<Double>,
        val phosphorusRange: ClosedFloatingPointRange<Double>,
        val potassiumRange: ClosedFloatingPointRange<Double>,
        val temperatureRange: ClosedFloatingPointRange<Double>,
        val humidityRange: ClosedFloatingPointRange<Double>,
        val phRange: ClosedFloatingPointRange<Double>,
        val rainfallRange: ClosedFloatingPointRange<Double>
    )

    data class YieldInputs(
        val variety: String,
        val nitrogen: Double,
        val phosphorus: Double,
        val potassium: Double,
        val temperature: Double,
        val humidity: Double,
        val soilPh: Double,
        val rainfall: Double
    )

    data class PredictionResult(
        val variety: String,
        val yieldBand: String,
        val confidence: Int,
        val estimatedYieldPerAcre: Double,
        val performanceSummary: String,
        val recommendation: String
    )

    private val profiles = listOf(
        VarietyProfile("Jyothi", 2.35, 85.0..115.0, 35.0..55.0, 35.0..55.0, 24.0..31.0, 68.0..84.0, 5.6..6.8, 180.0..320.0),
        VarietyProfile("Kanchana", 2.20, 80.0..108.0, 32.0..52.0, 32.0..52.0, 24.0..30.0, 70.0..86.0, 5.8..6.9, 170.0..310.0),
        VarietyProfile("Uma", 2.55, 90.0..120.0, 40.0..60.0, 38.0..58.0, 25.0..31.5, 70.0..85.0, 5.7..6.7, 190.0..330.0),
        VarietyProfile("Jaya", 2.10, 82.0..110.0, 34.0..54.0, 34.0..54.0, 24.0..30.0, 68.0..82.0, 5.5..6.7, 175.0..305.0),
        VarietyProfile("Matta", 2.45, 88.0..118.0, 36.0..58.0, 36.0..58.0, 23.0..30.0, 72.0..88.0, 5.4..6.6, 200.0..340.0)
    )

    fun analyze(inputs: YieldInputs): PredictionResult {
        val profile = profiles.firstOrNull { it.name.equals(inputs.variety, ignoreCase = true) }
            ?: profiles.first()

        val nitrogenScore = scoreForRange(inputs.nitrogen, profile.nitrogenRange)
        val phosphorusScore = scoreForRange(inputs.phosphorus, profile.phosphorusRange)
        val potassiumScore = scoreForRange(inputs.potassium, profile.potassiumRange)
        val temperatureScore = scoreForRange(inputs.temperature, profile.temperatureRange)
        val humidityScore = scoreForRange(inputs.humidity, profile.humidityRange)
        val phScore = scoreForRange(inputs.soilPh, profile.phRange)
        val rainfallScore = scoreForRange(inputs.rainfall, profile.rainfallRange)

        val overallScore = (
            nitrogenScore * 0.18 +
            phosphorusScore * 0.12 +
            potassiumScore * 0.12 +
            temperatureScore * 0.16 +
            humidityScore * 0.14 +
            phScore * 0.13 +
            rainfallScore * 0.15
        )

        val stressPenalty = nutrientImbalancePenalty(inputs) + climatePenalty(inputs)
        val normalizedScore = (overallScore - stressPenalty).coerceIn(0.38, 1.08)

        val estimatedYieldPerAcre = (profile.baseYieldTonsPerAcre * normalizedScore).round2()

        val yieldBand = when {
            normalizedScore >= 0.97 -> "EXCELLENT"
            normalizedScore >= 0.84 -> "GOOD"
            normalizedScore >= 0.68 -> "MODERATE"
            else -> "LOW"
        }

        val confidence = (72 + (overallScore * 20).roundToInt() - (stressPenalty * 25).roundToInt())
            .coerceIn(64, 96)

        return PredictionResult(
            variety = profile.name,
            yieldBand = yieldBand,
            confidence = confidence,
            estimatedYieldPerAcre = estimatedYieldPerAcre,
            performanceSummary = buildSummary(inputs, profile, yieldBand, estimatedYieldPerAcre),
            recommendation = buildRecommendation(inputs, profile)
        )
    }

    private fun scoreForRange(value: Double, range: ClosedFloatingPointRange<Double>): Double {
        if (value in range) return 1.0
        val midpoint = (range.start + range.endInclusive) / 2.0
        val tolerance = (range.endInclusive - range.start) / 2.0
        val deviation = abs(value - midpoint)
        return (1.0 - (deviation / (tolerance * 2.2))).coerceIn(0.45, 0.98)
    }

    private fun nutrientImbalancePenalty(inputs: YieldInputs): Double {
        val maxNpk = maxOf(inputs.nitrogen, inputs.phosphorus, inputs.potassium)
        val minNpk = minOf(inputs.nitrogen, inputs.phosphorus, inputs.potassium)
        val spread = (maxNpk - minNpk) / 100.0
        return (spread * 0.06).coerceAtMost(0.12)
    }

    private fun climatePenalty(inputs: YieldInputs): Double {
        var penalty = 0.0
        if (inputs.temperature < 20.0 || inputs.temperature > 35.0) penalty += 0.07
        if (inputs.humidity < 55.0 || inputs.humidity > 92.0) penalty += 0.05
        if (inputs.soilPh < 4.8 || inputs.soilPh > 7.8) penalty += 0.07
        if (inputs.rainfall < 100.0 || inputs.rainfall > 450.0) penalty += 0.06
        return penalty
    }

    private fun buildSummary(
        inputs: YieldInputs,
        profile: VarietyProfile,
        yieldBand: String,
        estimatedYieldPerAcre: Double
    ): String {
        val varietyFit = if (inputs.soilPh in profile.phRange && inputs.rainfall in profile.rainfallRange) {
            "Field conditions are broadly aligned with ${profile.name} cultivation needs."
        } else {
            "Some field conditions are outside the preferred range for ${profile.name}."
        }

        val bandLine = when (yieldBand) {
            "EXCELLENT" -> "Current inputs indicate a high-performing crop cycle with strong production potential."
            "GOOD" -> "Crop conditions look stable and should deliver a healthy harvest with minor tuning."
            "MODERATE" -> "Yield is achievable, but environmental or nutrient gaps are limiting full productivity."
            else -> "The crop is underperforming and needs intervention to avoid further yield reduction."
        }

        return "$bandLine $varietyFit Estimated output is ${estimatedYieldPerAcre.round2()} tons per acre."
    }

    private fun buildRecommendation(inputs: YieldInputs, profile: VarietyProfile): String {
        val notes = mutableListOf<String>()

        if (inputs.nitrogen < profile.nitrogenRange.start) notes += "Increase nitrogen application in split doses during tillering and panicle initiation."
        if (inputs.nitrogen > profile.nitrogenRange.endInclusive) notes += "Reduce nitrogen load slightly to avoid lush vegetative growth and lodging risk."
        if (inputs.phosphorus < profile.phosphorusRange.start) notes += "Raise phosphorus availability to support root vigor and early establishment."
        if (inputs.potassium < profile.potassiumRange.start) notes += "Increase potassium to improve grain filling, drought tolerance, and disease resistance."
        if (inputs.temperature !in profile.temperatureRange) notes += "Adjust irrigation timing and canopy cooling practices to reduce temperature stress."
        if (inputs.humidity !in profile.humidityRange) notes += "Improve airflow and field drainage to keep canopy humidity in a safer range."
        if (inputs.soilPh !in profile.phRange) notes += "Correct soil pH with lime or gypsum-based amendments before the next fertilizer cycle."
        if (inputs.rainfall !in profile.rainfallRange) notes += "Review irrigation scheduling because rainfall is outside the ideal range for this variety."

        if (notes.isEmpty()) {
            notes += "Maintain the current nutrient program and continue weekly field monitoring to preserve yield potential."
            notes += "Use timely irrigation and pest scouting to protect the projected harvest output."
        }

        return notes.joinToString(separator = "\n• ", prefix = "• ")
    }

    private fun Double.round2(): Double = ((this * 100.0).roundToInt() / 100.0)
}
