package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import com.google.android.material.button.MaterialButton

class YieldPredictionActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yield_prediction)

        val spinner = findViewById<Spinner>(R.id.spinnerYieldVariety)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.paddy_varieties)
        )

        findViewById<ImageView>(R.id.btnBackYield).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnPredictYield).setOnClickListener {
            val nitrogen = findDouble(R.id.etNitrogen)
            val phosphorus = findDouble(R.id.etPhosphorus)
            val potassium = findDouble(R.id.etPotassium)
            val temperature = findDouble(R.id.etTemperature)
            val humidity = findDouble(R.id.etHumidity)
            val soilPh = findDouble(R.id.etSoilPh)
            val rainfall = findDouble(R.id.etRainfall)

            if (spinner.selectedItemPosition == 0) {
                Toast.makeText(this, getString(R.string.select_variety_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (listOf(nitrogen, phosphorus, potassium, temperature, humidity, soilPh, rainfall).any { it == null }) {
                Toast.makeText(this, getString(R.string.fill_yield_inputs_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val result = YieldPredictionEngine.analyze(
                YieldPredictionEngine.YieldInputs(
                    variety = spinner.selectedItem.toString(),
                    nitrogen = nitrogen!!,
                    phosphorus = phosphorus!!,
                    potassium = potassium!!,
                    temperature = temperature!!,
                    humidity = humidity!!,
                    soilPh = soilPh!!,
                    rainfall = rainfall!!
                )
            )

            startActivity(
                Intent(this, YieldResultActivity::class.java).apply {
                    putExtra(YieldResultActivity.EXTRA_VARIETY, result.variety)
                    putExtra(YieldResultActivity.EXTRA_YIELD_BAND, result.yieldBand)
                    putExtra(YieldResultActivity.EXTRA_CONFIDENCE, result.confidence)
                    putExtra(YieldResultActivity.EXTRA_YIELD_PER_ACRE, result.estimatedYieldPerAcre)
                    putExtra(YieldResultActivity.EXTRA_SUMMARY, result.performanceSummary)
                    putExtra(YieldResultActivity.EXTRA_RECOMMENDATION, result.recommendation)
                }
            )
        }
    }

    private fun findDouble(id: Int): Double? {
        val raw = findViewById<EditText>(id).text.toString().trim()
        return raw.toDoubleOrNull()
    }
}