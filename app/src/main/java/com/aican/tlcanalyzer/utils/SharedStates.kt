package com.aican.tlcanalyzer.utils

import android.graphics.Rect
import com.aican.tlcanalyzer.domain.model.multiple_analysis.HrVsAreaPer
import com.aican.tlcanalyzer.domain.model.spots.ManualContourResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object SharedStates {

    // for manual rectangle contour edit
    private val _manualContourEditState = MutableStateFlow<Boolean?>(false)
    val manualContourEditState: StateFlow<Boolean?> = _manualContourEditState

    fun updateManualContourEditState(newState: Boolean?) {
        _manualContourEditState.value = newState
    }


    // for manual rectangle contour
    private val _manualRectContourListState =
        MutableStateFlow<List<ManualContourResult>>(emptyList())
    val manualRectContourListState: StateFlow<List<ManualContourResult>> =
        _manualRectContourListState

    fun updateManualRectContourList(newList: List<ManualContourResult>) {
        _manualRectContourListState.value = newList
    }

    fun clearManualRectContourList() {
        _manualRectContourListState.value = emptyList()

    }


    // sample
    private val _sharedStateFlow = MutableStateFlow<String>("Initial Value")
    val sharedStateFlow: StateFlow<String> = _sharedStateFlow

    fun updateValue(newValue: String) {
        _sharedStateFlow.value = newValue
    }
}

object SharedData {

    var editRectangleContourRect: Rect? = null


    var hrVsAreaPerArrayListRM: List<HrVsAreaPer>? = null

    var hrVsAreaPerArrayListFinal: List<HrVsAreaPer>? = null

}