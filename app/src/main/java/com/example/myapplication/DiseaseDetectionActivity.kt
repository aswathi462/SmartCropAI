package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

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
    }
}