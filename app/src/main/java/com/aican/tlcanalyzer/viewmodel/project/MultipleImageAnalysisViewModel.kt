package com.aican.tlcanalyzer.viewmodel.project

import androidx.lifecycle.ViewModel
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.IntensityPlotData
import com.aican.tlcanalyzer.data.repository.project.ContourRepository
import com.aican.tlcanalyzer.data.repository.project.IntensityPlotRepository
import com.aican.tlcanalyzer.data.repository.project.image_analysis.ImageAnalysisRepository
import com.aican.tlcanalyzer.domain.model.multiple_analysis.ImageAnalysisData
import com.aican.tlcanalyzer.domain.model.multiple_analysis.SelectedImage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MultipleImageAnalysisViewModel @Inject constructor(
    private val imageAnalysisRepository: ImageAnalysisRepository,
    private val contourRepository: ContourRepository,
    val intensityPlotRepository: IntensityPlotRepository
) : ViewModel() {

    private val _imageAnalysisDataList: MutableStateFlow<List<ImageAnalysisData>> =
        MutableStateFlow(emptyList())
    val imageAnalysisDataList: StateFlow<List<ImageAnalysisData>> get() = _imageAnalysisDataList


    suspend fun fetchImageAnalysisData(imageIdList: List<SelectedImage>) {
        withContext(Dispatchers.IO) { // Switch to background thread
            val imageAnalysisDataList = mutableListOf<ImageAnalysisData>()

            imageIdList.forEach { selectedImage ->
                val intensityData =
                    intensityPlotRepository.getAllIntensityPlots(selectedImage.imageId)
                val allContours =
                    contourRepository.getAllContoursByImageId(imageId = selectedImage.imageId)

                // here we are getting the intensity data directly from database and plot it into graph

                imageAnalysisDataList.add(
                    ImageAnalysisData(
                        selectedImage.imageId,
                        selectedImage.imageName,
                        intensityData,
                        allContours
                    )
                )
            }

            _imageAnalysisDataList.value = imageAnalysisDataList // Post result to StateFlow
        }
    }

    suspend fun doesIntensityPlotExist(imageId: String): Boolean =
        intensityPlotRepository.doesIntensityPlotExist(imageId)


}

