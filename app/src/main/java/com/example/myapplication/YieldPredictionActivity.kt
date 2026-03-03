package com.example.myapplication

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class YieldPredictionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yield_prediction)

        // 1. Setup Spinner
        val spinner = findViewById<Spinner>(R.id.spinnerYieldVariety)
        val varieties = arrayOf("Select Variety", "Jyothi", "Kanchana", "Uma", "Jaya")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, varieties)

        // 2. Back Button
        findViewById<LinearLayout>(R.id.btnBack).setOnClickListener {
            finish()
        }

        // 3. Predict Button Logic
        findViewById<Button>(R.id.btnPredict).setOnClickListener {
            val n = findViewById<EditText>(R.id.etNitrogen).text.toString()
            val p = findViewById<EditText>(R.id.etPhosphorus).text.toString()
            val k = findViewById<EditText>(R.id.etPotassium).text.toString()

            if (n.isEmpty() || p.isEmpty() || k.isEmpty()) {
                Toast.makeText(this, "Please fill all nutrient values", Toast.LENGTH_SHORT).show()
            } else {
                // Here you would normally send data to your AI model
                Toast.makeText(this, "Calculating yield based on N:$n, P:$p, K:$k", Toast.LENGTH_LONG).show()
            }
        }
    }
}