package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView

class YieldResultActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yield_result)

        // ✅ Get data from Intent with localized safe fallbacks
        val yieldValue = intent.getStringExtra("YIELD_COUNT")
            ?: getString(R.string.invalid_yield_input_error) // Maps to a localized error placeholder

        val suggestValue = intent.getStringExtra("SUGGESTIONS")
            ?: getString(R.string.select_option_get_started) // Maps to a localized generic fallback

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