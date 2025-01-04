package com.aican.tlcanalyzer.ui.activities

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.data.database.project.dao.ImageDao
import com.aican.tlcanalyzer.data.database.project.dao.ProjectDetailsDao
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.data.database.project.entities.ImageType
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails
import com.aican.tlcanalyzer.databinding.ActivityCapturedImagePreviewBinding
import com.aican.tlcanalyzer.utils.AppUtils
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import com.google.firebase.installations.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@AndroidEntryPoint
class CapturedImagePreview : AppCompatActivity() {

    lateinit var binding: ActivityCapturedImagePreviewBinding
    private var projectImageUriString = ""
    private var projectName = ""
    private var projectDescription = ""
    var projectImageUri: Uri? = null


    private val projectViewModel: ProjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCapturedImagePreviewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        projectImageUriString = intent.getStringExtra("projectImageUri") ?: ""
        projectName = intent.getStringExtra("projectName") ?: ""
        projectDescription = intent.getStringExtra("projectDescription") ?: ""

        // Display the image using the URI
        if (projectImageUriString.isNotEmpty()) {
            projectImageUri = Uri.parse(projectImageUriString)
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, projectImageUri)
            binding.ivCrop.setImageToCrop(bitmap)

        }

        binding.btnRetake.setOnClickListener {
            projectImageUri = null
            finish()

        }

        binding.btnSave.setOnClickListener {
            val projectId = AppUtils.generateRandomId("TLC_IDN", 8)
            val projectFolder =
                "${ContextWrapper(this).externalMediaDirs[0]}/TLC_Analyzer/$projectId"
            File(projectFolder).mkdirs()

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())

            val mainImageBitmap = binding.ivCrop.bitmap
            val croppedBitmap = binding.ivCrop.crop()

            if (mainImageBitmap == null || croppedBitmap == null) {
                Toast.makeText(this, "Bitmap is null. Cannot save images.", Toast.LENGTH_SHORT)
                    .show()
                return@setOnClickListener
            }

            val savedMainImagePath =
                saveImageToFile(this, mainImageBitmap, "main_image.jpg", projectFolder)
            val saveCroppedImagePath =
                saveImageToFile(this, croppedBitmap, "cropped_image.jpg", projectFolder)
            val saveContourImagePath =
                saveImageToFile(this, croppedBitmap, "contour_image.jpg", projectFolder)

            if (savedMainImagePath == null || saveCroppedImagePath == null || saveContourImagePath == null) {
                Toast.makeText(this, "Error saving images", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val imageId = AppUtils.generateRandomId("IMAGE", 8)

            lifecycleScope.launch {
                val projectCount = projectViewModel.getProjectCount()
                projectViewModel.insertProjectDetails(
                    ProjectDetails(
                        projectId = projectId,
                        projectName = projectName,
                        projectDescription = projectDescription,
                        timeStamp = timestamp,
                        mainImagePath = saveCroppedImagePath,
                        projectNumber = projectCount.toString(),
                        imageSplitAvailable = false,
                    )
                )
                projectViewModel.insertImage(
                    Image(
                        imageId = imageId,
                        name = "Main Image",
                        originalImagePath = savedMainImagePath,
                        croppedImagePath = saveCroppedImagePath,
                        contourImagePath = saveContourImagePath,
                        timeStamp = timestamp,
                        thresholdVal = 0,
                        noOfSpots = 0,
                        description = "Main Image with Cropped and Contour Images",
                        projectId = projectId,
                        imageType = ImageType.MAIN
                    )
                )
            }

            Toast.makeText(
                this,
                "Images saved and metadata stored successfully!",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    private fun saveImageToFile(
        context: Context,
        bitmap: Bitmap,
        fileName: String,
        folderName: String
    ): String? {
        var outStream: FileOutputStream? = null
        return try {
            // Create directory path
            val dir = File(
                folderName
            )

            if (!dir.exists()) {
                dir.mkdirs() // Create directories if they don't exist
            }

            // File to save the image
            val outFile = File(dir, fileName)
            outStream = FileOutputStream(outFile)

            // Compress and write bitmap to file
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            outStream.flush()
            outStream.close()

            // Log success and return file path
            Log.d("TAG", "Image saved at: ${outFile.absolutePath}")
            outFile.absolutePath // Return the absolute path of the saved file
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
            null
        } catch (e: IOException) {
            e.printStackTrace()
            null
        } finally {
            outStream?.close()
        }
    }


    private fun saveImageUriToFile(
        imageUri: Uri,
        fileName: String,
        folderLocation: String
    ): String {
        val folder = File(folderLocation)
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val outputFile = File(folder, fileName)
        contentResolver.openInputStream(imageUri)?.use { inputStream ->
            FileOutputStream(outputFile).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return outputFile.absolutePath
    }


}