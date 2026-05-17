package com.example.myapplication

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DiseaseResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_result)

        // ✅ Get data safely
        val disease = intent.getStringExtra("DISEASE") ?: "Unknown"
        val confidence = intent.getStringExtra("CONFIDENCE") ?: "N/A"

        val treatments = intent.getStringArrayListExtra("TREATMENTS") ?: arrayListOf()
        val preventive = intent.getStringArrayListExtra("PREVENTIVE") ?: arrayListOf()
        val fertilizer = intent.getStringArrayListExtra("FERTILIZER") ?: arrayListOf()

        // ✅ Bind UI
        val diseaseText = findViewById<TextView>(R.id.tvDiseaseName)
        val confidenceText = findViewById<TextView>(R.id.tvConfidence)
        val treatmentText = findViewById<TextView>(R.id.tvTreatment)
        val preventiveText = findViewById<TextView>(R.id.tvPreventive)
        val fertilizerText = findViewById<TextView>(R.id.tvFertilizer)

        // ✅ Set values
        diseaseText.text = disease
        confidenceText.text = confidence

        treatmentText.text = if (treatments.isNotEmpty())
            treatments.joinToString("\n• ", "• ")
        else
            "No treatment available"

        preventiveText.text = if (preventive.isNotEmpty())
            preventive.joinToString("\n• ", "• ")
        else
            "No preventive measures available"

        fertilizerText.text = if (fertilizer.isNotEmpty())
            fertilizer.joinToString("\n• ", "• ")
        else
            "No fertilizer recommendation available"
    }
}