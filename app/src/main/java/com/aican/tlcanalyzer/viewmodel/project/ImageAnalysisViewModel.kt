package com.aican.tlcanalyzer.viewmodel.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import co.yml.charts.common.model.Point
import com.aican.tlcanalyzer.analysis.IntensityDataProcessor
import com.aican.tlcanalyzer.data.repository.project.image_analysis.ImageAnalysisRepository
import com.aican.tlcanalyzer.domain.model.graphs.GraphPoint
import com.aican.tlcanalyzer.domain.model.graphs.IntensityDataState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.MatOfPoint
import javax.inject.Inject

@HiltViewModel
class ImageAnalysisViewModel @Inject constructor(
    private val imageAnalysisRepository: ImageAnalysisRepository
) : ViewModel() {

    private val _isContoursFetched = MutableStateFlow(false)
    val isContoursFetched: StateFlow<Boolean> = _isContoursFetched

    suspend fun generateSpots(
        imagePath: String, contourImagePath: String, threshold: Int, numberOfSpots: Int
    ): ArrayList<MatOfPoint> {
        return withContext(Dispatchers.IO) {
            // Perform the repository operation
            _isContoursFetched.value = true
            imageAnalysisRepository.generateSpots(imagePath, contourImagePath, threshold, numberOfSpots)
        }
    }


    fun resetContoursFetched() {
        _isContoursFetched.value = false
    }

    private val _intensityDataState =
        MutableStateFlow<IntensityDataState>(IntensityDataState.Empty) // Default state is Empty
    val intensityDataState: StateFlow<IntensityDataState> get() = _intensityDataState

    fun fetchIntensityData(imagePath: String, parts: Int) {
        viewModelScope.launch {
            _intensityDataState.value = IntensityDataState.Loading
            runCatching {
                val rawData = imageAnalysisRepository.fetchIntensityData(imagePath, parts)
                if (rawData.isEmpty()) {
                    IntensityDataState.Empty
                } else {
                    val mappedData = rawData.map {
                        GraphPoint(x = it.rf.toFloat() * parts, y = it.intensity.toFloat())
                    }
                    IntensityDataState.Success(mappedData)
                }
            }.onSuccess {
                _intensityDataState.value = it
            }.onFailure { e ->
                _intensityDataState.value = IntensityDataState.Error(e.message ?: "Unknown error")
            }
        }
    }

    suspend fun fetchIntensityDataWithoutState(imagePath: String, parts: Int): IntensityDataState {
        _intensityDataState.value = IntensityDataState.Loading
        return runCatching {
            val rawData = imageAnalysisRepository.fetchIntensityData(imagePath, parts)
            if (rawData.isEmpty()) {
                IntensityDataState.Empty
            } else {
                val mappedData = rawData.map {
                    GraphPoint(x = it.rf.toFloat() * parts, y = it.intensity.toFloat())
                }
                IntensityDataState.Success(mappedData)
            }
        }.onSuccess {
            _intensityDataState.value = it
        }.onFailure { e ->
            _intensityDataState.value = IntensityDataState.Error(e.message ?: "Unknown error")
        }.getOrElse {
            IntensityDataState.Error("Failed to fetch intensity data")
        }
    }


    fun resetIntensityData() {
        _intensityDataState.value = IntensityDataState.Empty
    }

    suspend fun fetchIntensityDataIntoPointDSet(
        imagePath: String, partIntensities: Int
    ): List<Point> {
//        if (intensityData.isEmpty()) return emptyList()

        println("Intensity Data: calling function")

        return imageAnalysisRepository.fetchIntensityData(imagePath, partIntensities).map {
            println("Intensity Data: $it")
            Point(it.rf.toFloat() * partIntensities, it.intensity.toFloat())

        }
    }

//    suspend fun fetchIntensityDataIntoGraphPointDSet(
//        imagePath: String,
//        partIntensities: Int
//    ): List<GraphPoint> {
////        if (intensityData.isEmpty()) return emptyList()
//
//        println("Intensity Data: calling function")
//
//        return fetchIntensityData(imagePath, partIntensities).map {
//            println("Intensity Data: $it")
//            GraphPoint(it.rf.toFloat() * partIntensities, it.intensity.toFloat())
//
//        }
//    }

}