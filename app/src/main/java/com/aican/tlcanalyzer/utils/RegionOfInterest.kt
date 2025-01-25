package com.aican.tlcanalyzer.utils

import android.graphics.Bitmap
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

object RegionOfInterest {
    fun drawRectWithROI(inputBitmap: Bitmap, x: Int, y: Int, w: Int, h: Int): Bitmap {
        // Convert the bitmap to Mat format
        val inputMat = Mat()
        Utils.bitmapToMat(inputBitmap, inputMat)

        // Create a copy of the input image
        val outputMat = inputMat.clone()

        // Define the coordinates of the rectangle
        val fullWidth = inputBitmap.width // Get the full width of the image
        val p1 = Point(0.0, y.toDouble()) // Set x coordinate to 0 for full width
        val p2 = Point(fullWidth.toDouble(), (y + h).toDouble()) // Set x coordinate to full width

        val color = Scalar(255.0, 0.0, 255.0) // Blue color
        val thickness = 2

        // Draw the rectangle on the output image
        Imgproc.rectangle(outputMat, p1, p2, color, thickness)

        // Convert the output Mat to Bitmap format
        val outputBitmap =
            Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(outputMat, outputBitmap)

        return outputBitmap
    }


    fun drawOvalWithROI(inputBitmap: Bitmap?, x: Int, y: Int, w: Int, h: Int): Bitmap {
        // Convert the bitmap to Mat format
        val inputMat = Mat()
        Utils.bitmapToMat(inputBitmap, inputMat)

        // Create a copy of the input image
        val outputMat = inputMat.clone()

        // Define the coordinates and axes of the oval
        val center = Point((x + w / 2).toDouble(), (y + h / 2).toDouble())
        val axes = Size((w / 2).toDouble(), (h / 2).toDouble())
        val angle = 0.0 // Rotation angle of the ellipse
        val startAngle = 0.0 // Starting angle of the elliptical arc in degrees
        val endAngle = 360.0 // Ending angle of the elliptical arc in degrees
        val color = Scalar(255.0, 0.0, 255.0) // Blue color
        val thickness = 2

        // Draw the oval on the output image
        Imgproc.ellipse(outputMat, center, axes, angle, startAngle, endAngle, color, thickness)

        // Convert the output Mat to Bitmap format
        val outputBitmap =
            Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(outputMat, outputBitmap)

        return outputBitmap
    }

    fun calculateRectangleArea(w: Int, h: Int): Int {
        // Calculate the area of the rectangle
        val area = w * h
        return area
    }

    // Helper function to calculate RF (Retention Factor)
    private fun calculateRF(contour: MatOfPoint): Double {
        // Example: Placeholder calculation for RF
        val boundingRect = Imgproc.boundingRect(contour)
        return boundingRect.y.toDouble() / boundingRect.height
    }

    // Helper function to calculate CV (Coefficient of Variation)
    private fun calculateCV(contour: MatOfPoint): Double {
        val points = contour.toList()
        val xValues = points.map { it.x }
        val mean = xValues.average()
        val stdDev = kotlin.math.sqrt(xValues.map { (it - mean) * (it - mean) }.average())
        return (stdDev / mean) * 100
    }
}