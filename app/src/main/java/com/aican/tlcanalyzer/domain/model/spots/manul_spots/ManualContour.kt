package com.aican.tlcanalyzer.domain.model.spots.manul_spots

data class ManualContour(
    var shape: Int,
    var roi: android.graphics.Rect,
    var indexName: String,
    var mainContIndex: String,
    var rfIndex: Int
)