package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class DiseaseDetectionActivity : BaseActivity() {

    private var selectedImageUri: Uri? = null
    private lateinit var ivPreview: ImageView
    private var cameraImageUri: Uri? = null

    // Gallery Selection Setup
    private val pickFromGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                showImage(it)
            }
        }

    // Camera Capture Setup
    private val takePhoto =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
            if (success) {
                cameraImageUri?.let {
                    selectedImageUri = it
                    showImage(it)
                }
            } else {
                Toast.makeText(this, "Camera capture failed", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_detection)

        // FIX: Find your custom TextView back button and assign its click action
        val btnBack = findViewById<TextView>(R.id.btnBack)
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        ivPreview = findViewById(R.id.ivPreview)
        val spinner = findViewById<Spinner>(R.id.spinnerVariety)

        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.paddy_varieties)
        )

        findViewById<Button>(R.id.btnUploadImage).setOnClickListener {
            showImagePickerDialog()
        }

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            uploadImage(spinner)
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Camera", "Gallery", "Cancel")

        AlertDialog.Builder(this)
            .setTitle("Upload Crop Image")
            .setItems(options) { dialog, which ->
                when (which) {
                    0 -> openCamera()
                    1 -> pickFromGallery.launch("image/*")
                    2 -> dialog.dismiss()
                }
            }
            .show()
    }

    private fun openCamera() {
        val file = File(cacheDir, "camera_leaf.jpg").apply {
            if (exists()) delete()
            createNewFile()
        }

        cameraImageUri = FileProvider.getUriForFile(
            this,
            "${packageName}.fileprovider",
            file
        )

        takePhoto.launch(cameraImageUri)
    }

    private fun showImage(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = android.graphics.BitmapFactory.decodeStream(inputStream)
            ivPreview.setImageBitmap(bitmap)
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Image load failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImage(spinner: Spinner) {
        val variety = spinner.selectedItem.toString()
        val placeholder = resources.getStringArray(R.array.paddy_varieties)[0]

        if (variety == placeholder || selectedImageUri == null) {
            Toast.makeText(this, "Select variety & image", Toast.LENGTH_SHORT).show()
            return
        }

        val file = uriToFile(selectedImageUri!!)
        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

        Log.d("DiseaseDetection", "Uploading image to backend...")

        DiseaseRetrofitClient.api.uploadLeaf(imagePart)
            .enqueue(object : Callback<DiseaseResponse> {

                override fun onResponse(
                    call: Call<DiseaseResponse>,
                    response: Response<DiseaseResponse>
                ) {
                    if (response.isSuccessful) {
                        val result = response.body()

                        if (result != null && result.status == "Success") {
                            val intent = Intent(
                                this@DiseaseDetectionActivity,
                                DiseaseResultActivity::class.java
                            ).apply {
                                putExtra("VARIETY", variety)
                                putExtra("DISEASE", result.diagnosis ?: "Unknown")
                                putExtra("STATUS", result.status)

                                val rawConfidence = result.confidence ?: "0"
                                val formattedConfidence = if (rawConfidence.contains("%")) {
                                    rawConfidence
                                } else {
                                    rawConfidence.toDoubleOrNull()?.let {
                                        String.format("%.2f%%", it * 100)
                                    } ?: rawConfidence
                                }
                                putExtra("CONFIDENCE", formattedConfidence)

                                val treatmentList = ArrayList(result.recommendation?.treatments ?: emptyList())
                                val preventiveList = ArrayList(result.recommendation?.preventive ?: emptyList())
                                val fertilizerList = ArrayList(result.recommendation?.fertilizer ?: emptyList())

                                putStringArrayListExtra("TREATMENTS", treatmentList)
                                putStringArrayListExtra("PREVENTIVE", preventiveList)
                                putStringArrayListExtra("FERTILIZER", fertilizerList)
                            }
                            startActivity(intent)
                        } else {
                            val errorMsg = result?.message ?: "The uploaded image was rejected by the server."
                            Toast.makeText(this@DiseaseDetectionActivity, errorMsg, Toast.LENGTH_LONG).show()
                        }
                    } else {
                        if (response.code() == 400) {
                            Toast.makeText(
                                this@DiseaseDetectionActivity,
                                "Validation Rejected: Please upload a clear image of a rice plant leaf.",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(this@DiseaseDetectionActivity, "Server Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun onFailure(call: Call<DiseaseResponse>, t: Throwable) {
                    Toast.makeText(this@DiseaseDetectionActivity, "Network Failure: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val file = File(cacheDir, "upload.jpg")
        file.outputStream().use { output ->
            inputStream.copyTo(output)
        }
        inputStream.close()
        return file
    }
}