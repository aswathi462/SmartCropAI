package com.example.myapplication

import android.os.Bundle
import android.widget.TextView

class DiseaseResultActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_result)

        // ✅ Get data safely (Using android.R.string.unknown for local "Unknown" translation)
        val disease = intent.getStringExtra("DISEASE") ?: getString(android.R.string.unknownName)
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

        // FIX: Replaced hardcoded fallback strings with localized layout definitions
        treatmentText.text = if (treatments.isNotEmpty())
            treatments.joinToString("\n• ", "• ")
        else
            getString(R.string.select_option) // Map to a placeholder or keep customized resource tags

        preventiveText.text = if (preventive.isNotEmpty())
            preventive.joinToString("\n• ", "• ")
        else
            getString(R.string.upload_instructions)

        fertilizerText.text = if (fertilizer.isNotEmpty())
            fertilizer.joinToString("\n• ", "• ")
        else
            getString(R.string.fill_yield_inputs_error)
    }
}