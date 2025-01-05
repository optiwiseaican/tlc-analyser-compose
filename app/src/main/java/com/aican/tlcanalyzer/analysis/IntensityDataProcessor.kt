package com.aican.tlcanalyzer.analysis

import android.graphics.BitmapFactory
import android.util.Log
import com.aican.tlcanalyzer.data.database.project.entities.IntensityPlotData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.opencv.android.Utils
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import javax.inject.Inject

class IntensityDataProcessor @Inject constructor() {

    suspend fun fetchIntensityData(
        imagePath: String,
        partIntensities: Int
    ): List<IntensityPlotData> = withContext(Dispatchers.IO) {
        val intensityPlotDataList = mutableListOf<IntensityPlotData>()

        try {
            // Step 1: Load the image from the file path
            val bitmap = BitmapFactory.decodeFile(imagePath)
            if (bitmap == null) {
                Log.e("FetchIntensityData", "Failed to load the image at $imagePath")
                return@withContext emptyList()
            }

            // Step 2: Convert Bitmap to OpenCV Mat
            val colorImageMat = Mat()
            Utils.bitmapToMat(bitmap, colorImageMat)

            // Step 3: Convert the image to grayscale
            val grayImageMat = Mat(colorImageMat.rows(), colorImageMat.cols(), CvType.CV_8UC1)
            Imgproc.cvtColor(colorImageMat, grayImageMat, Imgproc.COLOR_BGR2GRAY)

            // Step 4: Calculate RF values
            val rfValues = calculateRFValues(partIntensities)

            // Step 5: Calculate average intensity for each RF value
            rfValues.forEachIndexed { index, rfValue ->
                // Determine the Y-coordinate for the current RF value
                val horizontalLineY = (rfValue * grayImageMat.rows()).toInt()

                // Check if the line is within bounds
                if (horizontalLineY in 0 until grayImageMat.rows()) {
                    val intensities = mutableListOf<Double>()

                    // Fetch intensity values along the line
                    for (x in 0 until grayImageMat.cols()) {
                        val pixelIntensity = grayImageMat.get(horizontalLineY, x)?.get(0) ?: 0.0
                        intensities.add(pixelIntensity)
                    }

                    // Calculate the average intensity for the current RF value
                    val averageIntensity = intensities.average()

                    // Create an IntensityPlotData object and add it to the list
                    intensityPlotDataList.add(
                        IntensityPlotData(
                            intensityPlotId = 0, // Auto-generated in the database
                            imageId = imagePath, // Use imagePath as an identifier
                            rf = rfValue,
                            intensity = averageIntensity
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(
                "FetchIntensityData",
                "Error occurred during intensity data fetching: ${e.message}",
                e
            )
        }

        return@withContext intensityPlotDataList
    }

    // Helper function to calculate RF values
    private fun calculateRFValues(numParts: Int): List<Double> {
        val rfValues = mutableListOf<Double>()
        val increment = 1.0 / numParts

        for (i in 0 until numParts) {
            rfValues.add(i * increment)
        }

        return rfValues
    }


}