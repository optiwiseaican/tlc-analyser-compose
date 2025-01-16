package com.aican.tlcanalyzer.analysis

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.File
import javax.inject.Inject

//import java.util.ArrayList

class OpenCVOperations @Inject constructor() {

    fun convertToGrayAndReturnBitmap(inputColorMat: Mat): Bitmap? {
        // Check if input color Mat is valid
        if (inputColorMat.empty()) {
            return null // Return null if input is invalid
        }

        // Create an output Mat to store the grayscale image
        val grayMat = Mat()

        // Convert the input color Mat to grayscale
        Imgproc.cvtColor(inputColorMat, grayMat, Imgproc.COLOR_BGR2GRAY)

        // Create a Bitmap from the grayscale Mat
        val bitmap = Bitmap.createBitmap(grayMat.cols(), grayMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(grayMat, bitmap)

        return bitmap // Return the Bitmap representing the grayscale image
    }

    suspend fun generateSpots(
        imagePath: String,
        threshVal: Int,
        contourImagePath: String,
        numberOfSpots: Int
    ): ArrayList<MatOfPoint> {
        val outFile = File(imagePath)
        if (outFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(outFile.absolutePath)

            val firstImage = Mat()
            Utils.bitmapToMat(myBitmap, firstImage)

            val originalImageCopy = Mat()
            firstImage.copyTo(originalImageCopy)

            // Grayscale conversion
            val grayScaleImage = Mat()
            Imgproc.cvtColor(firstImage, grayScaleImage, Imgproc.COLOR_BGR2GRAY)

            // Apply threshold to create a binary image
            val binary = Mat()
            Imgproc.threshold(
                grayScaleImage,
                binary,
                threshVal.toDouble(),
                255.0,
                Imgproc.THRESH_BINARY
            )

            // Find contours
            val contours = ArrayList<MatOfPoint>()
            val hierarchy = Mat()
            Imgproc.findContours(
                binary,
                contours,
                hierarchy,
                Imgproc.RETR_TREE,
                Imgproc.CHAIN_APPROX_SIMPLE
            )

            // Sort contours by area in descending order
            contours.sortWith { c1, c2 ->
                val area1 = Imgproc.contourArea(c1)
                val area2 = Imgproc.contourArea(c2)
                area2.compareTo(area1)
            }

            // Plot contours on the image


            // Fetch all contours from the repository

            // Limit to the first `numberOfSpots` contours
            val limitedContours = ArrayList<MatOfPoint>(contours.take(numberOfSpots))



            plotContour(imagePath, contourImagePath, limitedContours)

            return contours
        } else {
            return ArrayList()
        }
    }

    suspend fun plotContour(
        imagePath: String,
        contourImagePath: String,
        contours: ArrayList<MatOfPoint>
    ) {
        val outFile = File(imagePath)

        if (outFile.exists()) {
            val myBitmap = BitmapFactory.decodeFile(outFile.absolutePath)

            val contourMat = Mat()
            Utils.bitmapToMat(myBitmap, contourMat)

            val contourColor = Scalar(0.0, 0.0, 255.0) // Red color in BGR format

            // Draw contours on the image
            Imgproc.drawContours(
                contourMat,
                contours,
                -1, // Draw all contours
                contourColor,
                2
            )

            // Convert the Mat with contours to Bitmap
            val outputBitmap = Bitmap.createBitmap(
                contourMat.cols(),
                contourMat.rows(),
                Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(contourMat, outputBitmap)

            // Save the output Bitmap to the specified path
            val outputFile = File(contourImagePath)
            outputFile.outputStream().use { outputStream ->
                outputBitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }
    }


}