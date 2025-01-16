package com.aican.tlcanalyzer.domain.model.graphs

sealed class IntensityDataState {
    data object Loading : IntensityDataState()
    data class Success(val data: List<GraphPoint>) : IntensityDataState()
    data class Error(val message: String) : IntensityDataState()
    data object Empty : IntensityDataState()
}
