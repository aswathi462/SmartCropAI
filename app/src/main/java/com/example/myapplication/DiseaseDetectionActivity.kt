package com.example.myapplication

import android.net.Uri
import android.os.Bundle
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class DiseaseDetectionActivity : AppCompatActivity() {

    // Launcher for the Gallery/File picker
    private val getImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            Toast.makeText(this, "Image Selected!", Toast.LENGTH_SHORT).show()
            // Optional: If you have an ImageView inside uploadArea, you can set it here:
            // findViewById<ImageView>(R.id.ivPreview).setImageURI(it)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_detection)

        // 1. Setup Spinner (Paddy Varieties)
        val spinner = findViewById<Spinner>(R.id.spinnerVariety)
        val varieties = arrayOf("Select Variety", "Jyothi", "Kanchana", "Uma", "Jaya", "Matta")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, varieties)
        spinner.adapter = adapter

        // 2. Setup Image Upload Area
        val uploadArea = findViewById<FrameLayout>(R.id.uploadArea)
        uploadArea.setOnClickListener {
            getImage.launch("image/*") // Opens file manager for images
        }

        // 3. Setup Back Button
        findViewById<LinearLayout>(R.id.btnBack).setOnClickListener {
            finish() // Returns to Dashboard
        }

        // 4. Submit Button
        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val selectedVariety = spinner.selectedItem.toString()
            if (selectedVariety == "Select Variety") {
                Toast.makeText(this, "Please select a paddy variety", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Analyzing for $selectedVariety...", Toast.LENGTH_LONG).show()
            }
        }
    }
}