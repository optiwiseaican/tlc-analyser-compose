package com.aican.tlcanalyzer.utils

import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.widget.Toast
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object SplitImageProjectManager {

    /**
     * 📌 Data Class for Split Image Project Metadata
     */
    @Serializable
    data class ProjectMetadata(
        val projectId: String,
        val projectName: String,
        val created_at: String,
        var total_main_images: Int = 0,
        var main_images: MutableList<MainImage> = mutableListOf()
    )

    @Serializable
    data class MainImage(
        val mainId: String,
        val path: String,
        var split_images: MutableList<SplitImage> = mutableListOf()
    )

    @Serializable
    data class SplitImage(
        val splitId: String,
        val path: String
    )

    /**
     * 📌 Function to Get the External Storage Project Path
     */
    private fun getProjectPath(context: Context, projectId: String): String {
        return "${ContextWrapper(context).externalMediaDirs[0]}/TLC_Analyzer/$projectId"
    }

    /**
     * 📌 Create Metadata File for Split Image Project (Stored in External Storage)
     */
    fun createMetadataFile(context: Context, projectId: String, projectName: String): String? {
        return try {
            val timestamp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

            val metadata = ProjectMetadata(projectId, projectName, timestamp)
            val jsonObject = Json.encodeToString(metadata)

            val projectFolder = getProjectPath(context, projectId)
            val projectDir = File(projectFolder)

            if (!projectDir.exists()) projectDir.mkdirs()

            val metadataFile = File(projectFolder, "metadata.json")

            BufferedWriter(FileWriter(metadataFile)).use { it.write(jsonObject) }

            Toast.makeText(context, "Metadata file saved!", Toast.LENGTH_LONG).show()

            metadataFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            null
        }
    }

    /**
     * 📌 Read Metadata for Split Image Project (From External Storage)
     */
    fun readMetadataFile(context: Context, projectId: String): ProjectMetadata? {
        return try {
            val metadataFile = File(getProjectPath(context, projectId), "metadata.json")

            if (!metadataFile.exists()) {
                Toast.makeText(context, "Metadata file not found", Toast.LENGTH_LONG).show()
                return null
            }

            Json.decodeFromString(metadataFile.readText())
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error reading metadata: ${e.message}", Toast.LENGTH_LONG)
                .show()
            null
        }
    }

    /**
     * 📌 Add a New Main Image to Project Metadata
     */
    fun addMainImageToProject(
        context: Context,
        projectId: String,
        mainImageId: String,
        mainImageBitmap: Bitmap,
        originalImageBitmap: Bitmap,
        contourImageBitmap: Bitmap
    ): List<String?> {
        return try {
            val metadataFile = File(getProjectPath(context, projectId), "metadata.json")

            if (!metadataFile.exists()) {
                Toast.makeText(context, "Metadata file not found", Toast.LENGTH_LONG).show()
                return listOf(null, null, null)
            }

            val metadata = Json.decodeFromString<ProjectMetadata>(metadataFile.readText())

            // ✅ Save main image
            val mainImagePath =
                saveMainImageBitmapToFile(context, projectId, mainImageId, mainImageBitmap)
            if (mainImagePath == null) {
                Toast.makeText(context, "Failed to save main image", Toast.LENGTH_LONG).show()
                return listOf(null, null, null)
            }

            // ✅ Save original image
            val originalImagePath = saveMainImageBitmapToFile(
                context,
                projectId,
                "original_$mainImageId",
                originalImageBitmap
            )
            if (originalImagePath == null) {
                Toast.makeText(context, "Failed to save original image", Toast.LENGTH_LONG).show()
                return listOf(null, null, null)
            }

            // ✅ Save contour image
            val contourImagePath = saveMainImageBitmapToFile(
                context,
                projectId,
                "contour_$mainImageId",
                contourImageBitmap
            )
            if (contourImagePath == null) {
                Toast.makeText(context, "Failed to save contour image", Toast.LENGTH_LONG).show()
                return listOf(null, null, null)
            }

            // ✅ Add main image to metadata
            val newMainImage = MainImage(
                mainId = mainImageId,
                path = mainImagePath,
            )
            metadata.main_images.add(newMainImage)
            metadata.total_main_images = metadata.main_images.size

            // ✅ Write updated metadata back to file
            metadataFile.writeText(Json.encodeToString(metadata))

            Toast.makeText(context, "Main Image added successfully", Toast.LENGTH_LONG).show()

            // ✅ Return all paths
            listOf(mainImagePath, originalImagePath, contourImagePath)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error updating metadata: ${e.message}", Toast.LENGTH_LONG)
                .show()
            listOf(null, null, null)
        }
    }

    /**
     * 📌 Add a Split Image to an Existing Main Image in Metadata
     */
    fun addSplitImageToProject(
        context: Context,
        projectId: String,
        mainImageId: String,
        splitImageId: String,
        bitmap: Bitmap
    ): List<String?> {
        return try {
            val metadataFile = File(getProjectPath(context, projectId), "metadata.json")

            if (!metadataFile.exists()) {
                Toast.makeText(context, "Metadata file not found", Toast.LENGTH_LONG).show()
                return listOf(null, null, null)
            }

            val metadata = Json.decodeFromString<ProjectMetadata>(metadataFile.readText())

            val mainImage = metadata.main_images.find { it.mainId == mainImageId }
            if (mainImage == null) {
                Toast.makeText(context, "Main Image not found in metadata", Toast.LENGTH_LONG)
                    .show()
                return listOf(null, null, null)
            }

            // ✅ Save the split image
            val splitImagePath =
                saveSplitBitmapToFile(context, projectId, mainImageId, splitImageId, bitmap)
            if (splitImagePath == null) {
                Toast.makeText(context, "Failed to save split image", Toast.LENGTH_LONG).show()
                return listOf(null, null, null)
            }

            // ✅ Save the temp split image
            val tempSplitImagePath =
                saveSplitBitmapToFile(context, projectId, mainImageId, "temp_$splitImageId", bitmap)
            if (tempSplitImagePath == null) {
                Toast.makeText(context, "Failed to save temp split image", Toast.LENGTH_LONG).show()
                return listOf(null, null, null)
            }

            // ✅ Save the contour split image
            val contSplitImagePath =
                saveSplitBitmapToFile(context, projectId, mainImageId, "cont_$splitImageId", bitmap)
            if (contSplitImagePath == null) {
                Toast.makeText(context, "Failed to save contour split image", Toast.LENGTH_LONG)
                    .show()
                return listOf(null, null, null)
            }

            // ✅ Add the split image to metadata only after all images are successfully saved
            val newSplitImage = SplitImage(splitId = splitImageId, path = splitImagePath)
            mainImage.split_images.add(newSplitImage)

            // ✅ Write updated metadata back to file
            metadataFile.writeText(Json.encodeToString(metadata))

            Toast.makeText(context, "Split Image added successfully", Toast.LENGTH_LONG).show()

            // ✅ Return paths of all saved images
            listOf(splitImagePath, tempSplitImagePath, contSplitImagePath)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Error updating metadata: ${e.message}", Toast.LENGTH_LONG)
                .show()
            listOf(null, null, null)
        }
    }

    /**
     * 📌 Save Main Image to File
     */
    fun saveMainImageBitmapToFile(
        context: Context,
        projectId: String,
        imageId: String,  // Can be main, original, or contour image
        bitmap: Bitmap
    ): String? {
        return try {
            val imageFolder = File("${getProjectPath(context, projectId)}/main_images/$imageId")
            if (!imageFolder.exists()) imageFolder.mkdirs()

            // ✅ File path for the image
            val imageFile = File(imageFolder, "$imageId.jpg")

            // ✅ Save image
            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

            imageFile.absolutePath  // Return saved image path
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    /**
     * 📌 Save Split Image to File
     */
    fun saveSplitBitmapToFile(
        context: Context,
        projectId: String,
        mainImageId: String,
        splitImageId: String,
        bitmap: Bitmap
    ): String? {
        return try {
            val splitImageFolder =
                File("${getProjectPath(context, projectId)}/main_images/$mainImageId/split_images")
            if (!splitImageFolder.exists()) splitImageFolder.mkdirs()

            val imageFile = File(splitImageFolder, "$splitImageId.jpg")

            FileOutputStream(imageFile).use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }

            imageFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
