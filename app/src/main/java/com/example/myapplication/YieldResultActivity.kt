package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class YieldResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yield_result)

        // Get data from Intent (safe handling)
        val yieldValue = intent.getStringExtra("YIELD_COUNT")
            ?: "0.00 tons/hectare"

        val suggestValue = intent.getStringExtra("SUGGESTIONS")
            ?: "No suggestions available"

        // Bind views
        val yieldText = findViewById<TextView>(R.id.tvYieldValue)
        val suggestionsText = findViewById<TextView>(R.id.tvSuggestions)
        val backBtn = findViewById<Button>(R.id.btnResultBack)

        // Set values
        yieldText.text = yieldValue
        suggestionsText.text = suggestValue

        // Back button
        backBtn.setOnClickListener {
            finish()
        }
    }
}