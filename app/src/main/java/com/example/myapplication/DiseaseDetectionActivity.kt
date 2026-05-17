package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.result.contract.ActivityResultContracts
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class DiseaseDetectionActivity : AppCompatActivity() {

    private var selectedImageUri: Uri? = null

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
                return@setOnClickListener
            }

            val file = uriToFile(selectedImageUri!!)

            val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
            val imagePart = MultipartBody.Part.createFormData(
                "image",
                file.name,
                requestFile
            )

            DiseaseRetrofitClient.api.uploadLeaf(imagePart)
                .enqueue(object : Callback<DiseaseResponse> {

                    override fun onResponse(
                        call: Call<DiseaseResponse>,
                        response: Response<DiseaseResponse>
                    ) {
                        if (response.isSuccessful) {

                            val result = response.body()

                            val intent = Intent(
                                this@DiseaseDetectionActivity,
                                DiseaseResultActivity::class.java
                            )

                            intent.putExtra("VARIETY", variety)
                            intent.putExtra("DISEASE", result?.diagnosis)
                            intent.putExtra("STATUS", result?.status)
                            intent.putExtra("CONFIDENCE", result?.confidence)
                            intent.putExtra("FIREBASE_ID", result?.firebaseId)

                            intent.putStringArrayListExtra(
                                "TREATMENTS",
                                ArrayList(result?.recommendation?.treatments ?: emptyList())
                            )

                            intent.putStringArrayListExtra(
                                "PREVENTIVE",
                                ArrayList(result?.recommendation?.preventive ?: emptyList())
                            )

                            intent.putStringArrayListExtra(
                                "FERTILIZER",
                                ArrayList(result?.recommendation?.fertilizer ?: emptyList())
                            )

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

    private fun uriToFile(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val file = File(cacheDir, "leaf.jpg")
        val outputStream = file.outputStream()

        inputStream.copyTo(outputStream)

        inputStream.close()
        outputStream.close()

        return file
    }

    private val getImage =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageUri = it
                findViewById<ImageView>(R.id.ivPreview).setImageURI(it)
                findViewById<ImageView>(R.id.ivUploadIcon).visibility = View.GONE
            }
        }
}