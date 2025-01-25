package com.aican.tlcanalyzer.utils

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

object AppUtils {

    var retake = false

    const val APP_NAME = "TLC Analyser"
    val UiColor1 = Color.Black

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

}