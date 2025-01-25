package com.aican.tlcanalyzer.utils

import android.graphics.Rect
import com.aican.tlcanalyzer.domain.model.spots.ManualContourResult
import com.aican.tlcanalyzer.domain.model.spots.manul_spots.ManualContour
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SharedStates {


    // for manual rectangle contour
    private val _manualRectContourListState = MutableStateFlow<List<ManualContourResult>>(emptyList())
    val manualRectContourListState: StateFlow<List<ManualContourResult>> = _manualRectContourListState

    fun updateManualRectContourList(newList: List<ManualContourResult>) {
        _manualRectContourListState.value = newList
    }


    // sample
    private val _sharedStateFlow = MutableStateFlow<String>("Initial Value")
    val sharedStateFlow: StateFlow<String> = _sharedStateFlow

    fun updateValue(newValue: String) {
        _sharedStateFlow.value = newValue
    }
}