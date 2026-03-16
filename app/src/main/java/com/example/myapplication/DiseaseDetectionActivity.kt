package com.example.myapplication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.google.android.material.button.MaterialButton
import java.io.File

class DiseaseDetectionActivity : BaseActivity() {

    private var selectedBitmap: Bitmap? = null
    private var isImageValid = false
    private var cameraImageUri: Uri? = null

    private lateinit var ivPreview: ImageView
    private lateinit var uploadPlaceholder: LinearLayout
    private lateinit var tvInvalidImage: TextView
    private lateinit var tvTapToChange: TextView

    data class PixelAnalysis(
        val brownRatio: Float,
        val yellowRatio: Float,
        val darkRatio: Float,
        val greenRatio: Float,
        val neutralRatio: Float,
        val contentHash: Long
    )

    // Launchers
    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { handleSelectedImage(it) } }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success -> if (success) cameraImageUri?.let { handleSelectedImage(it) } }

    private val cameraPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) launchCamera()
        else Toast.makeText(this, getString(R.string.camera_permission_denied), Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_disease_detection)

        ivPreview        = findViewById(R.id.ivImagePreview)
        uploadPlaceholder = findViewById(R.id.uploadPlaceholder)
        tvInvalidImage   = findViewById(R.id.tvInvalidImage)
        tvTapToChange    = findViewById(R.id.tvTapToChange)

        findViewById<ImageView>(R.id.btnBack).setOnClickListener { finish() }

        val spinner = findViewById<Spinner>(R.id.spinnerVariety)
        spinner.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            resources.getStringArray(R.array.paddy_varieties)
        )

        findViewById<MaterialButton>(R.id.btnCamera).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED
            ) launchCamera()
            else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        findViewById<MaterialButton>(R.id.btnGallery).setOnClickListener {
            galleryLauncher.launch("image/*")
        }

        findViewById<MaterialButton>(R.id.btnAnalyze).setOnClickListener {
            val bitmap = selectedBitmap
            when {
                bitmap == null ->
                    Toast.makeText(this, getString(R.string.no_image_error), Toast.LENGTH_SHORT).show()
                !isImageValid ->
                    Toast.makeText(this, getString(R.string.invalid_image_error_toast), Toast.LENGTH_SHORT).show()
                spinner.selectedItemPosition == 0 ->
                    Toast.makeText(this, getString(R.string.select_variety_error), Toast.LENGTH_SHORT).show()
                else -> {
                    val variety  = spinner.selectedItem.toString()
                    val analysis = analyzePixels(bitmap)
                    val result   = DiseaseDatabase.analyze(
                        analysis.brownRatio, analysis.yellowRatio,
                        analysis.darkRatio,  variety, analysis.contentHash
                    )
                    startActivity(
                        Intent(this, DiseaseResultActivity::class.java).apply {
                            putExtra(DiseaseResultActivity.EXTRA_VARIETY,        variety)
                            putExtra(DiseaseResultActivity.EXTRA_DISEASE_NAME,   result.disease.name)
                            putExtra(DiseaseResultActivity.EXTRA_CAUSE,          result.disease.cause)
                            putExtra(DiseaseResultActivity.EXTRA_RECOMMENDATION, result.disease.recommendation)
                            putExtra(DiseaseResultActivity.EXTRA_CONFIDENCE,     result.confidence)
                            putExtra(DiseaseResultActivity.EXTRA_SEVERITY,       result.severity)
                        }
                    )
                }
            }
        }
    }

    private fun launchCamera() {
        val dir = File(cacheDir, "images").also { it.mkdirs() }
        cameraImageUri = FileProvider.getUriForFile(
            this, "${packageName}.provider", File(dir, "camera_capture.jpg")
        )
        cameraLauncher.launch(cameraImageUri!!)
    }

    private fun handleSelectedImage(uri: Uri) {
        val bitmap = getBitmapFromUri(uri) ?: return
        selectedBitmap = bitmap
        ivPreview.setImageBitmap(bitmap)
        ivPreview.visibility      = View.VISIBLE
        uploadPlaceholder.visibility = View.GONE
        tvTapToChange.visibility  = View.VISIBLE

        val analysis = analyzePixels(bitmap)
        isImageValid = isValidCropImage(analysis)
        tvInvalidImage.visibility = if (isImageValid) View.GONE else View.VISIBLE
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? = try {
        contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
    } catch (e: Exception) { null }

    /**
     * Scales the bitmap to 80×80 and classifies each pixel by HSV into:
     *   - brown/red  → disease spots
     *   - yellow     → chlorosis / yellowing
     *   - dark       → necrosis / sheath blight
     *   - green      → healthy plant tissue
     *
     * Also tracks neutral pixels so we can reject walls, furniture, sky and other
     * non-plant photos before producing a disease result.
     */
    private fun analyzePixels(bitmap: Bitmap): PixelAnalysis {
        val scaled = Bitmap.createScaledBitmap(bitmap, 80, 80, true)
        val total  = (scaled.width * scaled.height).toFloat()
        var brownCount = 0; var yellowCount = 0; var darkCount = 0; var greenCount = 0; var neutralCount = 0
        val hsv = FloatArray(3)
        var hashAccum = 0L

        for (x in 0 until scaled.width) {
            for (y in 0 until scaled.height) {
                val pixel = scaled.getPixel(x, y)
                hashAccum = hashAccum * 31L + pixel.toLong()

                Color.colorToHSV(pixel, hsv)
                val h = hsv[0]; val s = hsv[1]; val v = hsv[2]

                when {
                    v < 0.20f                                            -> darkCount++   // very dark/necrotic
                    s > 0.30f && v > 0.25f && (h < 25f || h > 330f)     -> brownCount++  // reddish-brown
                    s > 0.30f && v > 0.40f && h in 25f..75f             -> yellowCount++ // yellow-orange
                    s > 0.18f && v > 0.22f && h in 75f..165f            -> greenCount++  // green
                    else                                                -> neutralCount++
                }
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

