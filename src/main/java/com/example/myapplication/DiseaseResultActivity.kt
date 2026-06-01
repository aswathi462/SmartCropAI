package com.example.myapplication

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Button
class DiseaseResultActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_result)


        Log.d("IntentDebug", "DISEASE: ${intent.getStringExtra("DISEASE")}")
        Log.d("IntentDebug", "TREATMENTS Array: ${intent.getStringArrayListExtra("TREATMENTS")}")

        // ✅ Get data safely from intent strings
        val disease = intent.getStringExtra("DISEASE") ?: "Unknown Condition"
        val confidence = intent.getStringExtra("CONFIDENCE") ?: "N/A"

        //  Extract array lists passed from DiseaseDetectionActivity
        val treatments = intent.getStringArrayListExtra("TREATMENTS")
        val preventive = intent.getStringArrayListExtra("PREVENTIVE")
        val fertilizer = intent.getStringArrayListExtra("FERTILIZER")

        // ✅ Bind UI Components matching your XML layout IDs perfectly
        val diseaseText = findViewById<TextView>(R.id.tvDiseaseName)
        val confidenceText = findViewById<TextView>(R.id.tvConfidence)
        val treatmentText = findViewById<TextView>(R.id.tvTreatment)
        val preventiveText = findViewById<TextView>(R.id.tvPreventive)
        val fertilizerText = findViewById<TextView>(R.id.tvFertilizer)
        val backButton = findViewById<Button>(R.id.btnResultBack)

        backButton.setOnClickListener {
            finish() // simple: go back to previous screen
        }

        // ✅ Set Base Metadata Values
        diseaseText.text = disease
        confidenceText.text = confidence

        // ✅ Format lists cleanly with bullet points or fallback to logical descriptive text
        treatmentText.text = if (!treatments.isNullOrEmpty()) {
            treatments.joinToString("\n• ", "• ")
        } else {
            "No specific acute treatments required for this condition."
        }

        preventiveText.text = if (!preventive.isNullOrEmpty()) {
            preventive.joinToString("\n• ", "• ")
        } else {
            "Maintain general field sanitization and crop health monitoring."
        }

        fertilizerText.text = if (!fertilizer.isNullOrEmpty()) {
            fertilizer.joinToString("\n• ", "• ")
        } else {
            "Continue standard balanced NPK soil management guidelines."
        }
    }
}