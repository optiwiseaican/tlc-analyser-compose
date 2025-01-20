package com.aican.tlcanalyzer.analysis

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.ContourType
import com.aican.tlcanalyzer.domain.model.spots.AutoSpotModel
import com.aican.tlcanalyzer.domain.model.spots.ContourResult
import com.aican.tlcanalyzer.utils.AppUtils.format
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import java.io.File
import javax.inject.Inject
import kotlin.math.pow


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
        numberOfSpots: Int,
        message: (String) -> Unit
    ): ArrayList<ContourResult> {
        val outFile = File(imagePath)
        if (!outFile.exists()) {
            message("Image file not found at path: $imagePath")
            return ArrayList()
        }

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

        // Limit to the first `numberOfSpots` contours
        val limitedContours = ArrayList<MatOfPoint>(contours.take(numberOfSpots + 1))
        if (limitedContours.isNotEmpty()) {
            limitedContours.removeAt(0)
        }

        val contourResults = ArrayList<ContourResult>()

        limitedContours.forEachIndexed { index, contour ->
            val contourId = "C_${index + 1}"

            // Calculate area
            val area = Imgproc.contourArea(contour).format(2)

            // Calculate bounding rectangle and center point
            val boundingRect = Imgproc.boundingRect(contour)
            val centerPoint = Point(
                boundingRect.x + (boundingRect.width / 2.0),
                boundingRect.y + (boundingRect.height / 2.0)
            )

            // Calculate contour distance (distance traveled by center point)
            val contourDistance = Math.sqrt(centerPoint.x.pow(2) + centerPoint.y.pow(2))

            // Calculate solvent front distance (assuming a linear gradient)
            val solventFrontDistance =
                ((firstImage.height() / 2.0) * (1.0 - (threshVal.toDouble() / 255.0)))

            // Calculate RF value
            val rf = (10 - (contourDistance / solventFrontDistance)) / 10.0

            // Calculate RF Top and Bottom
            val rfTop = (rf * 1.1).format(2)
            val rfBottom = (rf * 0.9).format(2)

//            // assuming you have the image height stored in a variable called "imageHeight"
//            val normalizedTopY: Double = 1 - (topY.toDouble() / imageHeight)
//            val normalizedBaseY: Double = 1 - (baseY.toDouble() / imageHeight)

            // Calculate normalized positions
            val normalizedTopY = (1 - (boundingRect.y.toDouble() / firstImage.height())).format(2)
            val normalizedBaseY =
                (1 - ((boundingRect.y + boundingRect.height).toDouble() / firstImage.height())).format(
                    2
                )

            val normalizedCenterY =
                (1 - (boundingRect.y + (boundingRect.height / 2.0)) / firstImage.height()).format(2)

            // Calculate CV
            val cv = (1 / rf.toDouble()).format(2)

            // Calculate volume (area * distance difference)
            val volume =
                (area.toDouble() * Math.abs(solventFrontDistance - contourDistance)).format(2)

            // Create ContourData
            val contourData = ContourData(
                contourId = contourId,
                imageId = imagePath,
                name = (index + 1).toString(),
                area = area,
                volume = volume,
                rf = rf.format(2),
                rfTop = normalizedTopY,
                rfBottom = normalizedBaseY,
                cv = cv,
                chemicalName = "Unknown",
                type = ContourType.AUTO
            )

            // Add the combined result to the list
            contourResults.add(ContourResult(matOfPoint = contour, contourData = contourData))
        }

        return contourResults
    }


    suspend fun generateSpots2(
        imagePath: String,
        threshVal: Int,
        contourImagePath: String,
        numberOfSpots: Int,
        message: (String) -> Unit
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

//            if (contours.size < numberOfSpots + 1) {
//                message.invoke("Only " + (contours.size - 1) + " number of spots available, You can check with different threshold value")
//            }

            // Limit to the first `numberOfSpots` contours
            val limitedContours = ArrayList<MatOfPoint>(contours.take(numberOfSpots + 1))
            if (limitedContours.isNotEmpty()) {
                limitedContours.removeAt(0)
            }


//            plotContour(imagePath, contourImagePath, limitedContours)

            return limitedContours
        } else {
            return ArrayList()
        }
    }

    suspend fun plotContourOnImage(
        imagePath: String,
        contourImagePath: String,
        autoSpotModelList: List<AutoSpotModel>
    ) {

        println("From opencv plotContourOnImage: ${autoSpotModelList.size}")
        val outFile = File(imagePath)

        if (outFile.exists()) {
            // If the list is empty, simply copy the image to contourImagePath
            if (autoSpotModelList.isEmpty()) {
                println("AutoSpotModel list is empty, copying original image to contour image path.")
                val outputFile = File(contourImagePath)
                outFile.copyTo(outputFile, overwrite = true)
                return // Exit the function after copying the image
            }

            val contours = ArrayList<MatOfPoint>()
            val contourNames = mutableListOf<String>()

            // Extract contours and their names from AutoSpotModel
            autoSpotModelList.forEach { autoSpotModel ->
                contours.add(autoSpotModel.matOfPoint)
                contourNames.add(autoSpotModel.name)
            }

            val myBitmap = BitmapFactory.decodeFile(outFile.absolutePath)

            // Convert Bitmap to Mat
            val contourMat = Mat()
            Utils.bitmapToMat(myBitmap, contourMat)

            val contourColor = Scalar(255.0, 244.0, 143.0) // Yellow color in BGR format
            val textColor = Scalar(0.0, 0.0, 255.0) // Red color for text

            // Draw contours and add text for each contour
            contours.forEachIndexed { index, contour ->
                // Draw the contour
                Imgproc.drawContours(
                    contourMat,
                    listOf(contour), // Draw a single contour at a time
                    -1, // Index -1 to draw all parts of the contour
                    contourColor,
                    2
                )

                // Compute the center of the contour for placing the name
                val boundingRect = Imgproc.boundingRect(contour)
                val centerX = boundingRect.x + boundingRect.width / 2
                val centerY = boundingRect.y + boundingRect.height / 2

                // Draw the contour name
                Imgproc.putText(
                    contourMat,
                    contourNames[index], // Name of the contour
                    Point(
                        centerX.toDouble(),
                        centerY.toDouble()
                    ), // Position (center of the contour)
                    Imgproc.FONT_HERSHEY_SIMPLEX, // Font type
                    0.5, // Font scale
                    textColor, // Color of the text
                    1 // Thickness of the text
                )
            }

            // Convert the Mat with contours and text back to Bitmap
            val outputBitmap = Bitmap.createBitmap(
                contourMat.cols(),
                contourMat.rows(),
                Bitmap.Config.ARGB_8888
            )
            Utils.matToBitmap(contourMat, outputBitmap)

            // Save the output Bitmap to the specified path
            val outputFile = File(contourImagePath)
            outputFile.outputStream().use { outputStream ->
                outputBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
            }
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
//            val outputFile = File(contourImagePath)
//            outputFile.outputStream().use { outputStream ->
//                outputBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
//            }
        }


    }

}