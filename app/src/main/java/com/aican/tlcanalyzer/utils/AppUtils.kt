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

    const val APP_NAME = "TLC Analyser"
    val UiColor1 = Color.Black
    val buttonTextSize = 12.sp


    fun getColorByIndex(index: Int): String {
        val colorList = listOf(
            "#FF0000", // Red
            "#00FF00", // Green
            "#0000FF", // Blue
            "#FFFF00", // Yellow
            "#FF00FF", // Magenta
            "#00FFFF", // Cyan
            "#800000", // Maroon
            "#808000", // Olive
            "#008000", // Dark Green
            "#800080", // Purple
            "#808080", // Gray
            "#FFA500", // Orange
            "#A52A2A", // Brown
            "#8A2BE2", // Blue Violet
            "#5F9EA0", // Cadet Blue
            "#7FFF00", // Chartreuse
            "#D2691E", // Chocolate
            "#6495ED", // Cornflower Blue
            "#DC143C", // Crimson
            "#00CED1"  // Dark Turquoise
        )
        return colorList[index % colorList.size] // Cycle through colors if index exceeds the list
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