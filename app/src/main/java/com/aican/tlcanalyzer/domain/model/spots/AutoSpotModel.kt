package com.aican.tlcanalyzer.domain.model.spots

import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.ContourType
import org.opencv.core.MatOfPoint
import android.graphics.Rect

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

data class ManualContourResult(
    val type: ContourType,
    val contourData: ContourData,
    val rect: Rect
)
