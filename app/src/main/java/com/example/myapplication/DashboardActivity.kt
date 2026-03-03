package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import com.google.android.material.card.MaterialCardView

class DashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // --- Profile & Logout Setup ---
        val btnProfile = findViewById<LinearLayout>(R.id.btnProfileSettings) // Matches your XML ID
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        val btnLogout = findViewById<LinearLayout>(R.id.btnLogout)
        btnLogout.setOnClickListener {
            // Logic to go back to Login screen
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
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