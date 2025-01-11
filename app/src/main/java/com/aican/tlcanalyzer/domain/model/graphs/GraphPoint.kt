package com.aican.tlcanalyzer.domain.model.graphs

data class GraphPoint(
    val x: Float,
    val y: Float,
    val description: String = "Value of point is ${String.format("%.2f", y)}"
)