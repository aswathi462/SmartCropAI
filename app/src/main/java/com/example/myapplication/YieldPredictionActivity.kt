package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YieldPredictionActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yield_prediction)

        val spinner = findViewById<Spinner>(R.id.spinnerYieldVariety)

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.paddy_varieties)
        )

        findViewById<LinearLayout>(R.id.btnYieldToHome)
            .setOnClickListener { finish() }

        findViewById<Button>(R.id.btnPredictYield)
            .setOnClickListener {

                val n = findViewById<EditText>(R.id.etN).text.toString()
                val p = findViewById<EditText>(R.id.etP).text.toString()
                val k = findViewById<EditText>(R.id.etK).text.toString()
                val rain = findViewById<EditText>(R.id.etRain).text.toString()
                val ph = findViewById<EditText>(R.id.etPH).text.toString()

                // Validation
                if (n.isEmpty() || p.isEmpty() || k.isEmpty() ||
                    rain.isEmpty() || ph.isEmpty()
                ) {
                    Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }

                // Convert to Float (ONLY ONCE)
                val nVal = n.toFloat()
                val pVal = p.toFloat()
                val kVal = k.toFloat()
                val rainVal = rain.toFloat()
                val phVal = ph.toFloat()

                // Create request object for FastAPI
                val input = CropInput(
                    n = nVal,
                    p = pVal,
                    k = kVal,
                    temp = 28f,
                    hum = 70f,
                    ph = phVal,
                    rain = rainVal
                )

                // Call FastAPI using Retrofit
                RetrofitClient.api.predictYield(input)
                    .enqueue(object : Callback<CropResponse> {

                        override fun onResponse(
                            call: Call<CropResponse>,
                            response: Response<CropResponse>
                        ) {
                            if (response.isSuccessful) {

                                val result = response.body()

                                val yieldText =
                                    "${result?.predictedYield} ${result?.unit}"

                                val suggestions =
                                    result?.suggestions?.joinToString("\n") ?: ""

                                val intent = Intent(
                                    this@YieldPredictionActivity,
                                    YieldResultActivity::class.java
                                )

                                intent.putExtra("YIELD_COUNT", yieldText)
                                intent.putExtra("SUGGESTIONS", suggestions)

                                startActivity(intent)

                            } else {
                                Toast.makeText(
                                    this@YieldPredictionActivity,
                                    "Server Error: ${response.code()}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }

                        override fun onFailure(call: Call<CropResponse>, t: Throwable) {
                            Toast.makeText(
                                this@YieldPredictionActivity,
                                "Error: ${t.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
    }
}