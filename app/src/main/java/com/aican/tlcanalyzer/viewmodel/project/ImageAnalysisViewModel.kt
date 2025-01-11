package com.aican.tlcanalyzer.viewmodel.project

import androidx.lifecycle.ViewModel
import co.yml.charts.common.model.Point
import com.aican.tlcanalyzer.analysis.IntensityDataProcessor
import com.aican.tlcanalyzer.domain.model.graphs.GraphPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ImageAnalysisViewModel @Inject constructor(
    private val intensityDataProcessor: IntensityDataProcessor
) : ViewModel() {

    private suspend fun fetchIntensityData(imagePath: String, partIntensities: Int) =
        intensityDataProcessor.fetchIntensityData(imagePath, partIntensities)

    suspend fun fetchIntensityDataIntoPointDSet(
        imagePath: String,
        partIntensities: Int
    ): List<Point> {
//        if (intensityData.isEmpty()) return emptyList()

        println("Intensity Data: calling function")

        return fetchIntensityData(imagePath, partIntensities).map {
            println("Intensity Data: $it")
            Point(it.rf.toFloat() * partIntensities, it.intensity.toFloat())

        }
    }

    suspend fun fetchIntensityDataIntoGraphPointDSet(
        imagePath: String,
        partIntensities: Int
    ): List<GraphPoint> {
//        if (intensityData.isEmpty()) return emptyList()

        println("Intensity Data: calling function")

        return fetchIntensityData(imagePath, partIntensities).map {
            println("Intensity Data: $it")
            GraphPoint(it.rf.toFloat() * partIntensities, it.intensity.toFloat())

        }
    }

}