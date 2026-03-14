package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class YieldPredictionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yield_prediction)

        val spinner = findViewById<Spinner>(R.id.spinnerYieldVariety)
        val varieties = arrayOf("Select Variety", "Jyothi", "Kanchana", "Uma", "Jaya")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, varieties)

        // Navigation back to Dashboard
        findViewById<LinearLayout>(R.id.btnYieldToHome).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnPredictYield).setOnClickListener {
            // Collecting all factors from your requirement
            val n = findViewById<EditText>(R.id.etN).text.toString()
            val p = findViewById<EditText>(R.id.etP).text.toString()
            val k = findViewById<EditText>(R.id.etK).text.toString()
            val rain = findViewById<EditText>(R.id.etRain).text.toString()
            val ph = findViewById<EditText>(R.id.etPH).text.toString()

            // Note: Ensure these IDs exist in your XML or the app will crash
            // If you haven't added Temp/Humidity IDs to XML yet, let me know.
            val temp = "28" // Placeholder if not in XML yet
            val humid = "80" // Placeholder if not in XML yet

            if (n.isEmpty() || p.isEmpty() || k.isEmpty() || rain.isEmpty() || ph.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                val nVal = n.toDouble()
                val pVal = p.toDouble()

                // Logic for Yield Count in tons/hectare
                val prediction = (nVal * 0.004) + (rain.toDouble() * 0.0001)
                val yieldText = "${String.format("%.2f", prediction)} tons/hectare"

                // Logic for Suggestions based on your image
                val suggestions = mutableListOf<String>()
                if (nVal < 50) suggestions.add("- Apply urea fertilizer to increase nitrogen.")
                if (pVal < 30) suggestions.add("- Apply DAP fertilizer for root development.")
                if (suggestions.isEmpty()) suggestions.add("- Soil levels are optimal.")

                // Passing to Result Activity
                val intent = Intent(this, YieldResultActivity::class.java)
                intent.putExtra("YIELD_COUNT", yieldText)
                intent.putExtra("SUGGESTIONS", suggestions.joinToString("\n"))
                startActivity(intent)
            }
        }
    }
}