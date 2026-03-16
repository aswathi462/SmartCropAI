package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import java.io.File

class DiseaseDetectionActivity : AppCompatActivity() {
    private var selectedImageUri: Uri? = null

    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
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
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, varieties)

        findViewById<FrameLayout>(R.id.uploadArea).setOnClickListener { getImage.launch("image/*") }
        findViewById<LinearLayout>(R.id.btnBack).setOnClickListener { finish() }

        // THIS IS THE PART THAT OPENS THE RESULT PAGE
        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val variety = spinner.selectedItem.toString()
            if (variety == "Select Variety" || selectedImageUri == null) {
                Toast.makeText(this, "Please select variety and image", Toast.LENGTH_SHORT).show()
            } else {
                // GO TO RESULT PAGE
                val intent = Intent(this, DiseaseResultActivity::class.java)
                intent.putExtra("VARIETY", variety)
                startActivity(intent)
            }
        }

        return PixelAnalysis(
            brownRatio  = brownCount  / total,
            yellowRatio = yellowCount / total,
            darkRatio   = darkCount   / total,
            greenRatio  = greenCount  / total,
            neutralRatio = neutralCount / total,
            contentHash = Math.abs(hashAccum)
        )
    }

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

