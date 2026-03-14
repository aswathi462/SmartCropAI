package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DiseaseResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_result)

        // Simulated results for now
        val confidence = 87
        updateSeverityUI(confidence)
    }

    private fun updateSeverityUI(percent: Int) {
        val circle = findViewById<FrameLayout>(R.id.statusCircle)
        val severityText = findViewById<TextView>(R.id.tvSeverityText)
        val badge = findViewById<TextView>(R.id.tvConfidenceBadge)

        badge.text = "$percent%"

        when {
            percent < 30 -> {
                circle.background.setTint(Color.parseColor("#22C55E")) // Safe Green
                severityText.text = "Safe"
                severityText.setTextColor(Color.parseColor("#22C55E"))
            }
            percent < 60 -> {
                circle.background.setTint(Color.parseColor("#EAB308")) // Mild Yellow
                severityText.text = "Mild"
                severityText.setTextColor(Color.parseColor("#EAB308"))
            }
            percent < 85 -> {
                circle.background.setTint(Color.parseColor("#F97316")) // Mild-High Orange
                severityText.text = "Mild-High"
                severityText.setTextColor(Color.parseColor("#F97316"))
            }
            else -> {
                circle.background.setTint(Color.parseColor("#EF4444")) // Severe Red
                severityText.text = "Severe"
                severityText.setTextColor(Color.parseColor("#EF4444"))
            }
        }
    }
}