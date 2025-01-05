package com.aican.tlcanalyzer.viewmodel.project

import androidx.lifecycle.ViewModel
import com.aican.tlcanalyzer.analysis.IntensityDataProcessor
import javax.inject.Inject

class ImageAnalysisViewModel @Inject constructor(
    private val intensityDataProcessor: IntensityDataProcessor
) : ViewModel() {

    suspend fun fetchIntensityData(imagePath: String, partIntensities: Int) =
        intensityDataProcessor.fetchIntensityData(imagePath, partIntensities)



}