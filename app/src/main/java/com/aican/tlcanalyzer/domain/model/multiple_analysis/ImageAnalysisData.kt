package com.aican.tlcanalyzer.domain.model.multiple_analysis

import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.IntensityPlotData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class ImageAnalysisData(
    val imageId: String,
    val imageName: String,
    val intensityData: List<IntensityPlotData>,
    val contourData: List<ContourData>
)