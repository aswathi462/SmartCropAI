package com.example.myapplication

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class YieldResultActivity : BaseActivity() {

    companion object {
        const val EXTRA_VARIETY = "yield_variety"
        const val EXTRA_YIELD_BAND = "yield_band"
        const val EXTRA_CONFIDENCE = "yield_confidence"
        const val EXTRA_YIELD_PER_ACRE = "yield_per_acre"
        const val EXTRA_SUMMARY = "yield_summary"
        const val EXTRA_RECOMMENDATION = "yield_recommendation"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yield_result)

        val variety = intent.getStringExtra(EXTRA_VARIETY) ?: getString(R.string.unknown_variety)
        val yieldBand = intent.getStringExtra(EXTRA_YIELD_BAND) ?: "MODERATE"
        val confidence = intent.getIntExtra(EXTRA_CONFIDENCE, 80)
        val yieldPerAcre = intent.getDoubleExtra(EXTRA_YIELD_PER_ACRE, 0.0)
        val summary = intent.getStringExtra(EXTRA_SUMMARY).orEmpty()
        val recommendation = intent.getStringExtra(EXTRA_RECOMMENDATION).orEmpty()

        data class BandStyle(val color: Int, val label: String, val iconRes: Int)
        val style = when (yieldBand) {
            "EXCELLENT" -> BandStyle(Color.parseColor("#2E7D32"), getString(R.string.yield_band_excellent), R.drawable.ic_check_white)
            "GOOD" -> BandStyle(Color.parseColor("#4F8A3C"), getString(R.string.yield_band_good), R.drawable.ic_check_white)
            "LOW" -> BandStyle(Color.parseColor("#C0392B"), getString(R.string.yield_band_low), R.drawable.ic_alert_white)
            else -> BandStyle(Color.parseColor("#E67E22"), getString(R.string.yield_band_moderate), R.drawable.ic_warning_white)
        }

        findViewById<ImageView>(R.id.btnBackYieldResult).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnPredictAgain).setOnClickListener { finish() }

        val circleCard = findViewById<MaterialCardView>(R.id.yieldCircleCard)
        circleCard.setCardBackgroundColor(style.color)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            circleCard.outlineAmbientShadowColor = style.color
            circleCard.outlineSpotShadowColor = style.color
        }

        findViewById<ImageView>(R.id.ivYieldIcon).setImageResource(style.iconRes)
        findViewById<TextView>(R.id.tvYieldBand).apply {
            text = style.label
            setTextColor(style.color)
        }

        findViewById<TextView>(R.id.tvYieldConfidence).apply {
            text = "$confidence%"
            setTextColor(style.color)
            background = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 60f
                setColor(Color.WHITE)
                setStroke(4, style.color)
            }
        }

        findViewById<TextView>(R.id.tvYieldVariety).text = variety
        findViewById<TextView>(R.id.tvYieldPerAcre).text = getString(R.string.yield_value_format, yieldPerAcre)
        findViewById<TextView>(R.id.tvYieldSummary).text = summary
        findViewById<TextView>(R.id.tvYieldRecommendation).text = recommendation
    }
}
