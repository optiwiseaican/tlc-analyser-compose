package com.aican.tlcanalyzer.domain.model.spots

import org.opencv.core.MatOfPoint

data class AutoSpotModel(
    val imageId: String,
    val contourId: String,
    val name: String,
    val matOfPoint: MatOfPoint
)