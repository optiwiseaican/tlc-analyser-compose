package com.aican.tlcanalyzer.utils

import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.absoluteValue

object AppUtils {
    const val APP_NAME = "TLC Analyser"
    val UiColor1 = Color.Black


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