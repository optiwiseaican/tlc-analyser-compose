package com.aican.tlcanalyzer.data.repository.project.image_analysis

import com.aican.tlcanalyzer.analysis.IntensityDataProcessor
import com.aican.tlcanalyzer.analysis.OpenCVOperations
import javax.inject.Inject

class ImageAnalysisRepository @Inject constructor(
    private val intensityDataProcessor: IntensityDataProcessor,
    private val openCVOperations: OpenCVOperations
) {
    suspend fun fetchIntensityData(imagePath: String, partIntensities: Int) =
        intensityDataProcessor.fetchIntensityData(imagePath, partIntensities)

    suspend fun generateSpots(imagePath: String, contourImagePath: String, thresholdVal: Int, numberOfSpots: Int) =
        openCVOperations.generateSpots(imagePath, thresholdVal, contourImagePath, numberOfSpots)

}