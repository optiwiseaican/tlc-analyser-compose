package com.aican.tlcanalyzer.ui.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.databinding.ActivityNewCameraBinding
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class NewCameraActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNewCameraBinding
    private lateinit var imageCapture: ImageCapture
    private lateinit var pickImageLauncher: ActivityResultLauncher<String>
    private lateinit var cameraPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var progressDialog: ProgressDialog
    private var camera: Camera? = null
    private var outputDirectory: File? = null

    private var projectName: String = ""
    private var projectDescription: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        // Initialize variables and listeners
        initVariables()
        setupClickListeners()

        // Request camera permission
        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
    }

    /**
     * Initializes necessary variables and register activity result contracts.
     */
    private fun initVariables() {
        projectName = intent.getStringExtra("projectName").orEmpty()
        projectDescription = intent.getStringExtra("projectDescription").orEmpty()

        outputDirectory = getOutputDirectory()

        progressDialog = ProgressDialog(this).apply {
            setMessage("Processing...")
            setCancelable(false)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()

        // Initialize permission launcher
        cameraPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                startCamera()
                Toast.makeText(this, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            }
        }

        // Initialize image picker
        pickImageLauncher = registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            uri?.let {


                handleSelectedImage(it)

                val intent = Intent(this, CapturedImagePreview::class.java)
                intent.putExtra("projectImageUri", uri.toString())
                intent.putExtra("projectName", projectName)
                intent.putExtra("projectDescription", projectDescription)
                startActivity(intent)

            }
        }
    }

    /**
     * Sets up click listeners for UI elements.
     */
    private fun setupClickListeners() {
        binding.pickImage.setOnClickListener { pickImageLauncher.launch("image/*") }

        binding.flashOff.setOnClickListener { toggleFlash(true) }

        binding.flashOn.setOnClickListener { toggleFlash(false) }

        binding.cameraCaptureButton.setOnClickListener {
            progressDialog.show()
            takePhoto()
        }

        binding.exposureSeekBar.setOnSeekBarChangeListener(seekBarChangeListener { progress ->
            setExposure(progress.toFloat())
        })

        binding.saturationSeekBar.setOnSeekBarChangeListener(seekBarChangeListener { progress ->
            setSaturation(progress / 100f)
        })
    }

    /**
     * Toggles the camera flash on or off.
     */
    private fun toggleFlash(enable: Boolean) {
        if (camera?.cameraInfo?.hasFlashUnit() == true) {
            camera?.cameraControl?.enableTorch(enable)
            binding.flashOff.visibility = if (enable) View.GONE else View.VISIBLE
            binding.flashOn.visibility = if (enable) View.VISIBLE else View.GONE
        }
    }

    /**
     * Starts the camera preview and initializes the ImageCapture.
     */
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .build()

                cameraProvider.unbindAll()
                camera = cameraProvider.bindToLifecycle(
                    this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture
                )

                setExposure(30f) // Default exposure
            } catch (e: Exception) {
                Toast.makeText(this, "Error starting camera: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    /**
     * Captures an image and saves it to the output directory.
     */
    private fun takePhoto() {
        val photoFile = File(
            outputDirectory,
            SimpleDateFormat(
                "yyyy-MM-dd-HH-mm-ss-SSS",
                Locale.US
            ).format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@NewCameraActivity,
                        "Photo saved: ${photoFile.path}",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onError(exception: ImageCaptureException) {
                    progressDialog.dismiss()
                    Toast.makeText(
                        this@NewCameraActivity,
                        "Capture failed: ${exception.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    /**
     * Handles the image selected from the gallery.
     */
    private fun handleSelectedImage(uri: Uri) {
        Toast.makeText(this, "Selected Image: $uri", Toast.LENGTH_SHORT).show()
    }

    /**
     * Retrieves the output directory for storing images.
     */
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, getString(R.string.app_name)).apply { mkdirs() }
        }
        return mediaDir ?: filesDir
    }

    /**
     * Sets the exposure level for the camera.
     */
    private fun setExposure(progress: Float) {
        camera?.let {
            val cameraControl = it.cameraControl
            val cameraInfo = it.cameraInfo

            if (cameraInfo.exposureState.isExposureCompensationSupported) {
                val minExposure = cameraInfo.exposureState.exposureCompensationRange.lower
                val maxExposure = cameraInfo.exposureState.exposureCompensationRange.upper
                val exposureCompensation =
                    (minExposure + (maxExposure - minExposure) * (progress / 100f)).toInt()
                cameraControl.setExposureCompensationIndex(exposureCompensation)
            }
        }
    }

    /**
     * Adjusts the saturation for the camera preview.
     */
    private fun setSaturation(value: Float) {
        val colorMatrix = ColorMatrix().apply { setSaturation(value) }
        val colorFilter = ColorMatrixColorFilter(colorMatrix)
        binding.viewFinder.background.colorFilter = colorFilter
    }

    /**
     * Helper function for setting SeekBar listeners.
     */
    private fun seekBarChangeListener(onProgressChanged: (Int) -> Unit) =
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                onProgressChanged(progress)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
