package com.example.myapplication

object DiseaseDatabase {

    data class DiseaseEntry(
        val name: String,
        val cause: String,
        val recommendation: String,
        val affectedVarieties: List<String> = emptyList(),
        val colorPattern: String = "brown"   // "brown" | "yellow" | "dark" | "healthy"
    )

    data class AnalysisResult(
        val disease: DiseaseEntry,
        val severity: String,    // HEALTHY | MILD | MODERATE | SEVERE
        val confidence: Int      // 0–100
    )

    private val diseases = listOf(

        // ── Brown / red-brown pattern ────────────────────────────────────────
        DiseaseEntry(
            name = "Bacterial Leaf Blight",
            cause = "Caused by Xanthomonas oryzae pv. oryzae. Spreads through infected irrigation water, wind-driven rain, and contaminated farm tools.",
            recommendation = "• Apply copper hydroxide (0.3%) or streptomycin sulfate solution.\n• Remove and destroy infected leaves immediately.\n• Avoid waterlogging in the field.\n• Disinfect farm tools with bleach solution before use.\n• Consider resistant varieties for the next season.",
            affectedVarieties = listOf("Uma", "Matta", "Jaya"),
            colorPattern = "brown"
        ),
        DiseaseEntry(
            name = "Brown Spot",
            cause = "Caused by Cochliobolus miyabeanus fungus. Commonly occurs in soils deficient in nutrients, especially potassium, and under water-stress conditions.",
            recommendation = "• Apply Mancozeb (0.25%) or Iprodione fungicide at 7-day intervals.\n• Ensure balanced NPK fertilization, especially potassium.\n• Avoid water stress during tillering stage.\n• Treat seeds with Thiram (2.5 g/kg seed) before sowing.\n• Improve field drainage to reduce humidity.",
            affectedVarieties = listOf("Kanchana", "Jaya", "Jyothi"),
            colorPattern = "brown"
        ),

        // ── Yellow / orange pattern ──────────────────────────────────────────
        DiseaseEntry(
            name = "Rice Blast",
            cause = "Caused by Magnaporthe oryzae fungus. Favored by cool temperatures (24–28°C), high humidity, and excessive nitrogen fertilization.",
            recommendation = "• Apply Tricyclazole (0.06%) or Carbendazim (0.1%) at boot leaf stage.\n• Avoid excess nitrogen – split application is recommended.\n• Drain fields periodically to reduce field humidity.\n• Remove and burn infected plant debris after harvest.\n• Use blast-resistant varieties in severely affected areas.",
            affectedVarieties = listOf("Jyothi", "Uma", "Kanchana"),
            colorPattern = "yellow"
        ),
        DiseaseEntry(
            name = "Tungro Virus",
            cause = "Caused by Rice Tungro Bacilliform Virus (RTBV) and Rice Tungro Spherical Virus (RTSV), transmitted by the green leafhopper (Nephotettix virescens).",
            recommendation = "• Remove and destroy infected plants immediately to halt spread.\n• Apply Carbofuran (3%) granules or Imidacloprid for leafhopper control.\n• Avoid transplanting near infected fields.\n• Monitor fields regularly for leafhopper populations.\n• Use tungro-tolerant varieties in next crop cycle.",
            affectedVarieties = listOf("Matta", "Kanchana", "Uma"),
            colorPattern = "yellow"
        ),

        // ── Dark / grayish pattern ───────────────────────────────────────────
        DiseaseEntry(
            name = "Sheath Blight",
            cause = "Caused by Rhizoctonia solani fungus. Spreads through infected soil, water, and plant debris. Worsened by dense planting and high humidity.",
            recommendation = "• Apply Propiconazole (0.1%) or Validamycin at tillering & panicle initiation.\n• Reduce plant density to improve air circulation.\n• Avoid excessive nitrogen fertilization.\n• Drain standing water regularly from field edges.\n• Use biological agent Trichoderma viride as a soil drench.",
            affectedVarieties = listOf("Jaya", "Jyothi", "Uma"),
            colorPattern = "dark"
        ),
        DiseaseEntry(
            name = "False Smut",
            cause = "Caused by Ustilaginoidea virens fungus. Infects individual grains at milky to dough stage under high humidity (>90%) conditions.",
            recommendation = "• Apply Propiconazole (0.1%) at boot leaf stage as preventive spray.\n• Avoid excessive nitrogen fertilization during panicle stage.\n• Remove and destroy infected panicles carefully before spores spread.\n• Collect fallen smut balls and dispose by burning.\n• Practice crop rotation with non-host crops.",
            affectedVarieties = listOf("Uma", "Kanchana", "Jaya"),
            colorPattern = "dark"
        ),

        // ── Healthy ──────────────────────────────────────────────────────────
        DiseaseEntry(
            name = "No Disease Detected",
            cause = "Your crop appears to be in excellent health. The image analysis did not detect significant disease symptoms.",
            recommendation = "• Continue field scouting twice a week for early detection.\n• Maintain balanced NPK fertilization schedule.\n• Ensure proper irrigation and field drainage.\n• Apply preventive fungicide at flag leaf stage as a precaution.\n• Monitor for pest activity and leafhopper populations.",
            affectedVarieties = emptyList(),
            colorPattern = "healthy"
        )
    )

    /**
     * @param brownRatio  Fraction of reddish-brown pixels (0..1)
     * @param yellowRatio Fraction of yellow/orange pixels   (0..1)
     * @param darkRatio   Fraction of very dark pixels       (0..1)
     * @param variety     Selected paddy variety name
     * @param imageHash   Content-based seed for deterministic selection
     */
    fun analyze(
        brownRatio: Float,
        yellowRatio: Float,
        darkRatio: Float,
        variety: String,
        imageHash: Long
    ): AnalysisResult {

        // Composite "affected" score
        val affectedRatio = brownRatio * 1.0f + yellowRatio * 0.85f + darkRatio * 0.55f

        val severity = when {
            affectedRatio < 0.06f -> "HEALTHY"
            affectedRatio < 0.20f -> "MILD"
            affectedRatio < 0.42f -> "MODERATE"
            else                  -> "SEVERE"
        }

        if (severity == "HEALTHY") {
            val healthy = diseases.last()
            val conf = (88 + Math.abs(imageHash % 9)).toInt().coerceIn(85, 97)
            return AnalysisResult(healthy, severity, conf)
        }

        // Dominant color determines disease family
        val pattern = when {
            brownRatio >= yellowRatio && brownRatio >= darkRatio -> "brown"
            yellowRatio >= brownRatio && yellowRatio >= darkRatio -> "yellow"
            else -> "dark"
        }

        val candidates = diseases.filter { it.colorPattern == pattern }

        // Prefer a variety-specific match; fall back to hash-based deterministic pick
        val selected = candidates.firstOrNull { entry ->
            entry.affectedVarieties.any { v ->
                variety.contains(v, ignoreCase = true) || v.contains(variety, ignoreCase = true)
            }
        } ?: run {
            val idx = Math.abs((imageHash % candidates.size).toInt())
            candidates[idx]
        }

        val baseConf = when (severity) {
            "MILD"     -> 63
            "MODERATE" -> 74
            else       -> 83   // SEVERE
        }
        val conf = (baseConf + Math.abs(imageHash % 13)).toInt().coerceIn(63, 97)

        return AnalysisResult(selected, severity, conf)
    }
}
