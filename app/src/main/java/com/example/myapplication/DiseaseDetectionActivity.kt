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
    private var cameraImageFile: File? = null

    // Launcher for Gallery selection
    private val pickFromGallery = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            ivPreview.setImageURI(it)
        }
    }

    // Launcher for Camera capture
    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success: Boolean ->
        if (success) {
            cameraImageFile?.let { file ->
                val uri = Uri.fromFile(file)
                selectedImageUri = uri
                ivPreview.setImageURI(uri)
            }
        } else {
            Toast.makeText(this, "Failed to capture image", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_detection)

        ivPreview = findViewById(R.id.ivPreview)
        val spinner = findViewById<Spinner>(R.id.spinnerVariety)

        // FIX 1: Populate dynamically via your values/strings.xml array resource mapping
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.paddy_varieties)
        )

        findViewById<Button>(R.id.btnUploadImage).setOnClickListener {
            showImagePickerDialog()
        }

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnSubmit).setOnClickListener {
            val variety = spinner.selectedItem.toString()

            // FIX 2: Safely extract localized dynamic item comparison index [0]
            val selectVarietyPlaceholder = resources.getStringArray(R.array.paddy_varieties)[0]

            if (variety == selectVarietyPlaceholder || selectedImageUri == null) {
                Toast.makeText(this, getString(R.string.select_variety_error), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val file = uriToFile(selectedImageUri!!)
            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData("image", file.name, requestFile)

            DiseaseRetrofitClient.api.uploadLeaf(imagePart)
                .enqueue(object : Callback<DiseaseResponse> {
                    override fun onResponse(call: Call<DiseaseResponse>, response: Response<DiseaseResponse>) {
                        if (response.isSuccessful) {
                            val result = response.body()
                            val intent = Intent(this@DiseaseDetectionActivity, DiseaseResultActivity::class.java)

                            intent.putExtra("VARIETY", variety)
                            intent.putExtra("DISEASE", result?.diagnosis)
                            intent.putExtra("STATUS", result?.status)
                            intent.putExtra("CONFIDENCE", result?.confidence)
                            intent.putExtra("FIREBASE_ID", result?.firebaseId)

                            intent.putStringArrayListExtra("TREATMENTS", ArrayList(result?.recommendation?.treatments ?: emptyList()))
                            intent.putStringArrayListExtra("PREVENTIVE", ArrayList(result?.recommendation?.preventive ?: emptyList()))
                            intent.putStringArrayListExtra("FERTILIZER", ArrayList(result?.recommendation?.fertilizer ?: emptyList()))

                            startActivity(intent)
                        } else {
                            Toast.makeText(this@DiseaseDetectionActivity, "Server Error", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<DiseaseResponse>, t: Throwable) {
                        Toast.makeText(this@DiseaseDetectionActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }
    }

    // FIX 3: Fully localized native pop-up alert selection item tags
    private fun showImagePickerDialog() {
        val options = arrayOf<CharSequence>(
            getString(R.string.btn_camera),
            getString(R.string.btn_gallery),
            getString(android.R.string.cancel)
        )

        val builder = AlertDialog.Builder(this)
        builder.setTitle(getString(R.string.upload_crop_image_title))
        builder.setItems(options) { dialog, item ->
            when (item) {
                0 -> setupCameraIntent()
                1 -> pickFromGallery.launch("image/*")
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun setupCameraIntent() {
        try {
            cameraImageFile = File(cacheDir, "camera_leaf.jpg").apply {
                if (exists()) delete()
                createNewFile()
            }

            cameraImageFile?.let { file ->
                val authority = "${applicationContext.packageName}.fileprovider"
                val imageUri = FileProvider.getUriForFile(this, authority, file)
                takePhoto.launch(imageUri)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("CameraError", "Error starting camera: ${e.message}")
            Toast.makeText(this, "Camera setup error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun uriToFile(uri: Uri): File {
        if (uri.scheme == "file") {
            return File(uri.path ?: "")
        }
        val inputStream = contentResolver.openInputStream(uri)!!
        val file = File(cacheDir, "leaf.jpg")
        val outputStream = file.outputStream()
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return file
    }
}