package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView

class DashboardActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // --- Profile & Logout Setup ---
        val btnProfile = findViewById<LinearLayout>(R.id.btnProfileSettings) // Matches your XML ID
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // --- NEW: Disease Detection Connection ---
        val cardDisease = findViewById<MaterialCardView>(R.id.cardDisease)
        cardDisease.setOnClickListener {
            val intent = Intent(this, DiseaseDetectionActivity::class.java)
            startActivity(intent)
        }

        // --- NEW: Yield Prediction Connection ---
        val cardYield = findViewById<MaterialCardView>(R.id.cardYield)
        cardYield.setOnClickListener {
            val intent = Intent(this, YieldPredictionActivity::class.java)
            startActivity(intent)
        }
    }
}