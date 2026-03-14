package com.example.myapplication

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class YieldResultActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yield_result)

        // Retrieve the data passed from the Prediction page
        val yieldValue = intent.getStringExtra("YIELD_COUNT") ?: "0.00 tons/hectare"
        val suggestValue = intent.getStringExtra("SUGGESTIONS") ?: ""

        // Find views and set text
        findViewById<TextView>(R.id.tvYieldValue).text = yieldValue
        findViewById<TextView>(R.id.tvSuggestions).text = suggestValue

        findViewById<Button>(R.id.btnResultBack).setOnClickListener { finish() }
    }
}