package com.example.myapplication

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class DiseaseResultActivity : BaseActivity() {

    companion object {
        const val EXTRA_VARIETY        = "variety"
        const val EXTRA_DISEASE_NAME   = "disease_name"
        const val EXTRA_CAUSE          = "cause"
        const val EXTRA_RECOMMENDATION = "recommendation"
        const val EXTRA_CONFIDENCE     = "confidence"
        const val EXTRA_SEVERITY       = "severity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_result)

        val variety        = intent.getStringExtra(EXTRA_VARIETY)        ?: getString(R.string.unknown_variety)
        val diseaseName    = intent.getStringExtra(EXTRA_DISEASE_NAME)   ?: "Unknown"
        val cause          = intent.getStringExtra(EXTRA_CAUSE)          ?: ""
        val recommendation = intent.getStringExtra(EXTRA_RECOMMENDATION) ?: ""
        val confidence     = intent.getIntExtra(EXTRA_CONFIDENCE, 75)
        val severity       = intent.getStringExtra(EXTRA_SEVERITY)       ?: "MILD"

        // Severity → color, label, icon
        data class SeverityStyle(val color: Int, val label: String, val iconRes: Int)
        val style = when (severity) {
            "HEALTHY"  -> SeverityStyle(Color.parseColor("#2E7D32"), getString(R.string.severity_healthy),  R.drawable.ic_check_white)
            "MILD"     -> SeverityStyle(Color.parseColor("#F59E0B"), getString(R.string.severity_mild),     R.drawable.ic_warning_white)
            "MODERATE" -> SeverityStyle(Color.parseColor("#E67E22"), getString(R.string.severity_moderate), R.drawable.ic_warning_white)
            else       -> SeverityStyle(Color.parseColor("#C0392B"), getString(R.string.severity_severe),   R.drawable.ic_alert_white)
        }

        // Back
        findViewById<ImageView>(R.id.btnBackResult).setOnClickListener { finish() }

        // Circle card
        val circleCard = findViewById<MaterialCardView>(R.id.severityCircleCard)
        circleCard.setCardBackgroundColor(style.color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            circleCard.outlineAmbientShadowColor = style.color
            circleCard.outlineSpotShadowColor    = style.color
        }

        // Icon
        findViewById<ImageView>(R.id.ivSeverityIcon).setImageResource(style.iconRes)

        // Severity label
        val tvLabel = findViewById<TextView>(R.id.tvSeverityLabel)
        tvLabel.text = style.label
        tvLabel.setTextColor(style.color)

        // Confidence badge
        val tvConf = findViewById<TextView>(R.id.tvConfidence)
        tvConf.text = "$confidence%"
        tvConf.setTextColor(style.color)
        tvConf.background = GradientDrawable().apply {
            shape        = GradientDrawable.RECTANGLE
            cornerRadius = 60f
            setColor(Color.WHITE)
            setStroke(4, style.color)
        }

        // Disease, cause, recommendation
    findViewById<TextView>(R.id.tvVarietyValue).text  = variety
        findViewById<TextView>(R.id.tvDiseaseName).text    = diseaseName
        findViewById<TextView>(R.id.tvCause).text          = cause
        findViewById<TextView>(R.id.tvRecommendation).text = recommendation

        // Analyze again → go back to input screen
        findViewById<MaterialButton>(R.id.btnAnalyzeAgain).setOnClickListener { finish() }
    }
}
