package com.aican.tlcanalyzer.viewmodel.project

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.ContourPoint
import com.aican.tlcanalyzer.data.database.project.entities.ContourType
import com.aican.tlcanalyzer.data.repository.project.ContourRepository
import com.aican.tlcanalyzer.data.repository.project.image_analysis.ImageAnalysisRepository
import com.aican.tlcanalyzer.domain.model.graphs.GraphPoint
import com.aican.tlcanalyzer.domain.states.graph.IntensityDataState
import com.aican.tlcanalyzer.domain.model.spots.AutoSpotModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.opencv.core.MatOfPoint
import org.opencv.core.Point
import org.opencv.imgproc.Imgproc
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ImageAnalysisViewModel @Inject constructor(
    private val imageAnalysisRepository: ImageAnalysisRepository,
    private val contourRepository: ContourRepository
) : ViewModel() {

    ////////////

    private val _isContoursFetched = MutableStateFlow(false)
    val isContoursFetched: StateFlow<Boolean> = _isContoursFetched

    suspend fun saveAutoContourDataToDatabase(imageId: String, contours: ArrayList<MatOfPoint>) {
//        viewModelScope.launch {
        try {
            // Lists to hold data for batch insertion
            val contourDataList = mutableListOf<ContourData>()
            val contourPointList = mutableListOf<ContourPoint>()

            // Iterate through contours and prepare data
            contours.forEachIndexed { index, contour ->
                val contourId = "C_$imageId${index + 1}"

                // Calculate area
                val area = Imgproc.contourArea(contour)

                // Calculate volume (assuming a height of 1 for a 2D contour)
                val volume = area * 1

                // Calculate RF (Retention Factor) as a placeholder value
                // Formula: RF = distance moved by solute / distance moved by solvent
                val rf = calculateRF(contour)

                // Calculate RF Top and Bottom as placeholders
                val rfTop = rf * 1.1 // Example multiplier for top
                val rfBottom = rf * 0.9 // Example multiplier for bottom

                // Calculate CV (Coefficient of Variation) as a placeholder
                // Formula: CV = (Standard Deviation / Mean) * 100
                val cv = calculateCV(contour)

                // Create ContourData
                contourDataList.add(
                    ContourData(
                        contourId = contourId,
                        imageId = imageId,
                        name = (index + 1).toString(),
                        area = area.toString(),
                        volume = volume.toString(),
                        rf = rf.toString(),
                        rfTop = rfTop.toString(),
                        rfBottom = rfBottom.toString(),
                        cv = cv.toString(),
                        chemicalName = "Unknown",
                        type = ContourType.AUTO
                    )
                )

                // Create ContourPoints
                contourPointList.addAll(
                    contour.toList().mapIndexed { i, point ->
                        val contourPointId = "P_$contourId${i + 1}"
                        ContourPoint(
                            contourPointId = contourPointId,
                            contourId = contourId,
                            x = point.x.toFloat(),
                            y = point.y.toFloat()
                        )
                    }
                )
            }

            // Batch insert all contour points and data
            contourRepository.insertContoursAndPoints(contourDataList, contourPointList)

            println("Successfully saved ${contourDataList.size} contours and ${contourPointList.size} points.")

        } catch (e: Exception) {
            println("Error saving contours: ${e.message}")
            e.printStackTrace()
        }
//        }
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

    suspend fun generateSpots(
        imagePath: String,
        contourImagePath: String,
        threshold: Int,
        numberOfSpots: Int,
        message: (String) -> Unit
    ): ArrayList<MatOfPoint> {
        return withContext(Dispatchers.IO) {
            // Perform the repository operation
            _isContoursFetched.value = true
            imageAnalysisRepository.generateSpots(
                imagePath, contourImagePath, threshold, numberOfSpots, message
            )


        }
    }


    fun resetContoursFetched() {
        _isContoursFetched.value = false
    }


    //////////


    suspend fun clearAllContours(imageId: String, imagePath: String, contourImagePath: String) {
        contourRepository.clearAllContours(imageId)

        // clear image
        val outFile = File(imagePath)
        val outputFile = File(contourImagePath)
        outFile.copyTo(outputFile, overwrite = true)
        //

    }


    //////////////

    private val _autoGeneratedSpots = MutableStateFlow<List<AutoSpotModel>>(emptyList())
    val autoGeneratedSpots: StateFlow<List<AutoSpotModel>> = _autoGeneratedSpots

    fun fetchAutoGeneratedSpotsFromDatabase(imageId: String) {
        viewModelScope.launch(Dispatchers.IO) { // Use Dispatchers.IO for database operations

            _autoGeneratedSpots.value = emptyList()
            val allContours = contourRepository.getAllContoursByImageId(imageId = imageId)

            println("Contour length from database in viewmodel : ${allContours.size}")

            val autoSpotList = allContours.map { contour ->
                val contourPoints =
                    contourRepository.getAllContourPointsByContourId(contour.contourId)
                val contourMatOfPoint = MatOfPoint().apply {
                    fromList(contourPoints.map { point ->
                        Point(point.x.toDouble(), point.y.toDouble())
                    })
                }

                AutoSpotModel(
                    imageId = contour.imageId,
                    contourId = contour.contourId,
                    name = contour.name,
                    matOfPoint = contourMatOfPoint
                )
            }

            // Update the StateFlow with a new list
            _autoGeneratedSpots.value = autoSpotList
        }
    }

    fun plotContourOnImage(
        imagePath: String,
        contourImagePath: String,
        autoSpotModelList: List<AutoSpotModel>
    ) {
        viewModelScope.launch {
            imageAnalysisRepository.plotContourOnImage(
                imagePath,
                contourImagePath,
                autoSpotModelList
            )
        }
    }


    //////////
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

    suspend fun fetchIntensityDataWithoutState(
        imagePath: String, parts: Int
    ): IntensityDataState {
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


    //////////


}