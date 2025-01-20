package com.aican.tlcanalyzer.domain.model.spots

import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import org.opencv.core.MatOfPoint

data class AutoSpotModel(
    val imageId: String,
    val contourId: String,
    val name: String,
    val matOfPoint: MatOfPoint
)

data class ContourResult(
    val matOfPoint: MatOfPoint,
    val contourData: ContourData
)
