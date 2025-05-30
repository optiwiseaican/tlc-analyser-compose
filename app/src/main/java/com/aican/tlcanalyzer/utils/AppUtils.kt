package com.aican.tlcanalyzer.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

object AppUtils {

    var MAIN_IMAGE_COUNT = 0

    var retake = false

    var SPOT_DETECTION_TYPE = "spot"
    var BAND_DETECTION_TYPE = "band"

    const val APP_NAME = "TLC Analyser"
    val UiColor1 = Color.Black
    val buttonTextSize = 12.sp


    private val baseColors = listOf(
        "#E53935", // Red
        "#43A047", // Green
        "#1E88E5", // Blue
        "#FBC02D", // Yellow
        "#8E24AA", // Purple
        "#00ACC1", // Cyan
        "#6D4C41", // Brown
        "#F57C00", // Orange
        "#7B1FA2", // Dark Purple
        "#C2185B", // Pink
        "#757575", // Gray
        "#D81B60", // Magenta
        "#4CAF50", // Mid Green
        "#00897B", // Teal
        "#5E35B1", // Indigo
        "#F4511E", // Deep Orange
        "#546E7A", // Blue Gray
        "#3949AB", // Deep Blue
        "#8D6E63", // Taupe
        "#009688"  // Dark Turquoise
    )

    private val darkColors = listOf(
        "#B71C1C", // Dark Red
        "#1B5E20", // Dark Green
        "#0D47A1", // Dark Blue
        "#F57F17", // Dark Yellow
        "#4A148C", // Dark Purple
        "#006064", // Dark Cyan
        "#3E2723", // Dark Brown
        "#E65100", // Dark Orange
        "#4A0072", // Dark Violet
        "#880E4F", // Dark Pink
        "#424242", // Dark Gray
        "#AD1457", // Dark Magenta
        "#2E7D32", // Dark Mid Green
        "#004D40", // Dark Teal
        "#311B92", // Dark Indigo
        "#BF360C", // Dark Deep Orange
        "#37474F", // Dark Blue Gray
        "#1A237E", // Dark Deep Blue
        "#5D4037", // Dark Taupe
        "#00796B"  // Dark Dark Turquoise
    )

    private val lightColors = listOf(
        "#E57373", // Slightly Darker Light Red
        "#81C784", // Slightly Darker Light Green
        "#64B5F6", // Slightly Darker Light Blue
        "#FFD54F", // Slightly Darker Light Yellow
        "#BA68C8", // Slightly Darker Light Purple
        "#4DD0E1", // Slightly Darker Light Cyan
        "#A1887F", // Slightly Darker Light Brown
        "#FFB74D", // Slightly Darker Light Orange
        "#CE93D8", // Slightly Darker Light Violet
        "#F06292", // Slightly Darker Light Pink
        "#9E9E9E", // Slightly Darker Light Gray
        "#EC407A", // Slightly Darker Light Magenta
        "#A5D6A7", // Slightly Darker Light Mid Green
        "#80CBC4", // Slightly Darker Light Teal
        "#B39DDB", // Slightly Darker Light Indigo
        "#FF8A65", // Slightly Darker Light Deep Orange
        "#B0BEC5", // Slightly Darker Light Blue Gray
        "#7986CB", // Slightly Darker Light Deep Blue
        "#A1887F", // Slightly Darker Light Taupe
        "#80DEEA"  // Slightly Darker Light Dark Turquoise
    )


    fun getColorByIndex(index: Int): String {
        return baseColors[index % baseColors.size]
    }

    fun getDarkColorByIndex(index: Int): String {
        return darkColors[index % darkColors.size]
    }

    fun getLightColorByIndex(index: Int): String {
        return lightColors[index % lightColors.size]
    }


    fun generateUniqueColor(contourId: String, imageId: String, existingColors: Set<Int>): Int {
        val predefinedColors = listOf(
            0xFF5733, 0x33FF57, 0x3357FF, 0xF3FF33, 0xFF33F3, 0x33FFF3, 0xFF5733.toInt()
        ) // Add as many unique colors as needed

        // Hash the contourId and imageId to create an index
        val hashIndex = (contourId + imageId).hashCode().absoluteValue % predefinedColors.size
        var color = predefinedColors[hashIndex].toInt()

        // Ensure the color is unique for the current image's contours
        var attempt = 0
        while (existingColors.contains(color)) {
            attempt++
            color = predefinedColors[(hashIndex + attempt) % predefinedColors.size].toInt()
        }

        return color
    }

    // Extension function to format Double to two decimal points
    fun Double.format(digits: Int): String = "%.${digits}f".format(this)


    fun generateRandomId(prefix: String, length: Int): String {
        val allowedChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        val randomPart = (1..length)
            .map { allowedChars.random() }
            .joinToString("")
        return "$prefix${System.currentTimeMillis()}$randomPart"
    }

    fun decodeTimestamp(timestamp: String): String {
        return try {
            // Define the format in which the timestamp is stored
            val inputFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            val date = inputFormat.parse(timestamp)

            // Define the output format for decoding the timestamp
            val outputFormat = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            // Fallback in case of parsing errors
            "Invalid Timestamp"
        }
    }

    fun formatToTwoDecimalPlaces(value: String?): String {
        try {
            val decimalValue = BigDecimal(value).setScale(2, RoundingMode.HALF_UP)
            return decimalValue.toString()
        } catch (e: NumberFormatException) {
            return "0.00" // Default to 0.00 if input is invalid
        }
    }


    fun saveImageToFile(
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


}