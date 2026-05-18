package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class YieldPredictionActivity : BaseActivity() {

    private val WEATHER_API_KEY = "1fecdd1c067f7177b1b0d0c0a5c8a175"
    private val WEATHER_BASE_URL = "https://api.openweathermap.org/"

    private lateinit var etTemp: EditText
    private lateinit var etHum: EditText
    private lateinit var etRain: EditText
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_yield_prediction)

        val spinner = findViewById<Spinner>(R.id.spinnerYieldVariety)
        progressBar = findViewById(R.id.progressBar)
        val btnPredictYield = findViewById<Button>(R.id.btnPredictYield)
        val btnEnableLocation = findViewById<Button>(R.id.btnEnableLocation)

        etTemp = findViewById(R.id.etTemp)
        etHum = findViewById(R.id.etHum)
        etRain = findViewById(R.id.etRain)

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.paddy_varieties)
        )

        findViewById<LinearLayout>(R.id.btnYieldToHome).setOnClickListener { finish() }

        btnEnableLocation.setOnClickListener {
            fetchLocationAndWeather()
        }

        btnPredictYield.setOnClickListener {
            val n = findViewById<EditText>(R.id.etN).text.toString()
            val p = findViewById<EditText>(R.id.etP).text.toString()
            val k = findViewById<EditText>(R.id.etK).text.toString()
            val ph = findViewById<EditText>(R.id.etPH).text.toString()
            val temp = etTemp.text.toString()
            val hum = etHum.text.toString()
            val rain = etRain.text.toString()

            // FIX 1: Pulled dynamic form error validation toast string
            if (n.isEmpty() || p.isEmpty() || k.isEmpty() || ph.isEmpty() || temp.isEmpty() || hum.isEmpty() || rain.isEmpty()) {
                Toast.makeText(this, getString(R.string.fill_yield_inputs_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val input = CropInput(
                n = n.toFloat(),
                p = p.toFloat(),
                k = k.toFloat(),
                temp = temp.toFloat(),
                hum = hum.toFloat(),
                ph = ph.toFloat(),
                rain = rain.toFloat()
            )

            sendDataToFastAPI(input)
        }
    }

    private fun fetchLocationAndWeather() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                1002
            )
            return
        }

        progressBar.visibility = View.VISIBLE
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                getWeatherMetricsFromAPI(location.latitude, location.longitude)
            } else {
                requestFreshGPSLocation(fusedLocationClient)
            }
        }.addOnFailureListener {
            progressBar.visibility = View.GONE
            // FIX 2: Localized fallback error handling text
            Toast.makeText(this, getString(R.string.invalid_yield_input_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestFreshGPSLocation(client: FusedLocationProviderClient) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            progressBar.visibility = View.GONE
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
            .setMaxUpdates(1)
            .build()

        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val freshLocation = locationResult.lastLocation
                if (freshLocation != null) {
                    getWeatherMetricsFromAPI(freshLocation.latitude, freshLocation.longitude)
                } else {
                    progressBar.visibility = View.GONE
                    // FIX 3: Localized hardware failure alert layout notice
                    Toast.makeText(this@YieldPredictionActivity, getString(R.string.invalid_yield_input_error), Toast.LENGTH_LONG).show()
                }
            }
        }

        client.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
    }

    private fun getWeatherMetricsFromAPI(lat: Double, lon: Double) {
        val weatherRetrofit = Retrofit.Builder()
            .baseUrl(WEATHER_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val weatherService = weatherRetrofit.create(OpenWeatherService::class.java)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = weatherService.getCurrentWeather(lat, lon, WEATHER_API_KEY)
                if (response.isSuccessful && response.body() != null) {
                    val weatherData = response.body()!!

                    val parsedTemp = weatherData.mainMetrics?.temperature ?: 28.0
                    val parsedHum = weatherData.mainMetrics?.humidity ?: 70
                    val parsedRain = weatherData.rainMetrics?.rain1h ?: 0.0

                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        etTemp.setText(parsedTemp.toString())
                        etHum.setText(parsedHum.toString())
                        etRain.setText(parsedRain.toString())
                        // FIX 4: Updated success banner confirmation label
                        Toast.makeText(this@YieldPredictionActivity, getString(R.string.language_updated), Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@YieldPredictionActivity, getString(R.string.invalid_yield_input_error), Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Log.e("WeatherError", "Exception: ${e.message}")
                    Toast.makeText(this@YieldPredictionActivity, getString(R.string.invalid_yield_input_error), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchLocationAndWeather()
        } else if (requestCode == 1002) {
            // FIX 5: Use localized camera_permission_denied or alternative text block indicator mapping
            Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendDataToFastAPI(input: CropInput) {
        RetrofitClient.api.predictYield(input)
            .enqueue(object : Callback<CropResponse> {
                override fun onResponse(call: Call<CropResponse>, response: Response<CropResponse>) {
                    if (response.isSuccessful) {
                        val result = response.body()
                        val yieldText = "${result?.predictedYield} ${result?.unit}"
                        val suggestions = result?.suggestions?.joinToString("\n") ?: ""

                        val intent = Intent(this@YieldPredictionActivity, YieldResultActivity::class.java)
                        intent.putExtra("YIELD_COUNT", yieldText)
                        intent.putExtra("SUGGESTIONS", suggestions)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this@YieldPredictionActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<CropResponse>, t: Throwable) {
                    Toast.makeText(this@YieldPredictionActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }
}