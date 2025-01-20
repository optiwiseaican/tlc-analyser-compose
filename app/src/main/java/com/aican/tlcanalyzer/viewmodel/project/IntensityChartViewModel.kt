package com.aican.tlcanalyzer.viewmodel.project

import androidx.lifecycle.ViewModel
import com.aican.tlcanalyzer.domain.model.graphs.GraphPoint
import com.github.mikephil.charting.data.Entry
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.Collections
import javax.inject.Inject

@HiltViewModel
class IntensityChartViewModel @Inject constructor() : ViewModel() {

    private var _lineChartDataList: MutableStateFlow<List<Entry>> = MutableStateFlow(emptyList())
    val lineChartDataList: MutableStateFlow<List<Entry>> get() = _lineChartDataList

    fun prepareChartData(points: List<GraphPoint>) {
        val entries = points.map { point -> Entry(point.x, 255 - point.y) }
        _lineChartDataList.value = entries // Update the state
    }

}
