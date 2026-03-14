package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        // -------- Profile Button --------
        val btnProfile = findViewById<LinearLayout>(R.id.btnProfileSettings)
        btnProfile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        // -------- Logout Button --------
        val btnLogout = findViewById<LinearLayout>(R.id.btnLogout)
        btnLogout.setOnClickListener {

            val intent = Intent(this, LoginActivity::class.java)

            // Clears activity stack
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

            startActivity(intent)
            finish()
        }

        // -------- Disease Detection Card --------
        val cardDisease = findViewById<MaterialCardView>(R.id.cardDisease)
        cardDisease.setOnClickListener {

            val intent = Intent(this, DiseaseDetectionActivity::class.java)
            startActivity(intent)

        }

        // -------- Yield Prediction Card --------
        val cardYield = findViewById<MaterialCardView>(R.id.cardYield)
        cardYield.setOnClickListener {

            val intent = Intent(this, YieldPredictionActivity::class.java)
            startActivity(intent)

        }
    }
}