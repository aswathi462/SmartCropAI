package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts

class DiseaseDetectionActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

    // Dummy values (you will replace with real image processing later)
    private var brownCount = 10f
    private var yellowCount = 15f
    private var darkCount = 5f
    private var greenCount = 70f
    private var neutralCount = 0f
    private var hashAccum = 12345

    private val getImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                findViewById<ImageView>(R.id.ivPreview).visibility = View.VISIBLE
                findViewById<ImageView>(R.id.ivPreview).setImageURI(it)
                findViewById<ImageView>(R.id.ivUploadIcon).visibility = View.GONE
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_detection)

        val spinner = findViewById<Spinner>(R.id.spinnerVariety)
        val varieties = arrayOf("Select Variety", "Jyothi", "Kanchana", "Uma", "Jaya", "Matta")

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            varieties
        )

        findViewById<FrameLayout>(R.id.uploadArea)
            .setOnClickListener { getImage.launch("image/*") }

        findViewById<View>(R.id.btnBack)
            .setOnClickListener { finish() }

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {

            val variety = spinner.selectedItem.toString()

            if (variety == "Select Variety" || selectedImageUri == null) {
                Toast.makeText(this, "Please select variety and image", Toast.LENGTH_SHORT).show()
            } else {

                val analysis = createPixelAnalysis()

                val intent = Intent(this, DiseaseResultActivity::class.java)
                intent.putExtra("VARIETY", variety)

                // OPTIONAL: pass results to next activity
                intent.putExtra("GREEN", analysis.greenRatio)
                intent.putExtra("YELLOW", analysis.yellowRatio)
                intent.putExtra("BROWN", analysis.brownRatio)

                startActivity(intent)
            }
        }
    }

    // FIXED: Proper function instead of invalid return in onCreate
    private fun createPixelAnalysis(): PixelAnalysis {

        val total = brownCount + yellowCount + darkCount + greenCount + neutralCount

        return PixelAnalysis(
            brownRatio = brownCount / total,
            yellowRatio = yellowCount / total,
            darkRatio = darkCount / total,
            greenRatio = greenCount / total,
            neutralRatio = neutralCount / total,
            contentHash = kotlin.math.abs(hashAccum)
        )
    }

    // Your validation logic (kept same)
    private fun isValidCropImage(analysis: PixelAnalysis): Boolean {

        val plantRatio = analysis.greenRatio + analysis.yellowRatio + analysis.brownRatio
        val diseasedLeafRatio = analysis.yellowRatio + analysis.brownRatio

        return when {
            analysis.greenRatio >= 0.14f && plantRatio >= 0.22f && analysis.neutralRatio <= 0.72f -> true
            diseasedLeafRatio >= 0.20f && plantRatio >= 0.24f && analysis.darkRatio <= 0.38f -> true
            else -> false
        }
    }
}

// Data class (IMPORTANT)
data class PixelAnalysis(
    val brownRatio: Float,
    val yellowRatio: Float,
    val darkRatio: Float,
    val greenRatio: Float,
    val neutralRatio: Float,
    val contentHash: Int
)