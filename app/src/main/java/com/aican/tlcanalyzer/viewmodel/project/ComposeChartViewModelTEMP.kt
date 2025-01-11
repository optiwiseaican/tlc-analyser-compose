package com.aican.tlcanalyzer.viewmodel.project

import com.aican.tlcanalyzer.domain.model.graphs.GraphPoint
import com.github.mikephil.charting.data.Entry
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import kotlinx.coroutines.flow.MutableStateFlow

class ComposeChartViewModelTEMP {
    // ChartEntryModelProducer to manage the chart data
    val chartEntryModelProducer: ChartEntryModelProducer = ChartEntryModelProducer()


    // Function to set chart data from a list of GraphPoints
    fun setChartData(points: List<GraphPoint>) {
        val entries = points.map { point ->
            object : ChartEntry {
                override val x: Float =
                    String.format("%.2f", point.x)
                        .toFloat() // Ensure x is at most 2 decimal places
                override val y: Float = point.y

                override fun withY(newY: Float): ChartEntry {
                    return object : ChartEntry {
                        override val x: Float = String.format("%.2f", point.x)
                            .toFloat() // Ensure x is at most 2 decimal places
                        override val y: Float = newY    // Update the y value
                        override fun withY(y: Float): ChartEntry {
                            return this.withY(y)
                        }
                    }
                }
            }
        }
        chartEntryModelProducer.setEntries(entries)
    }

    fun updateChartData(points: List<GraphPoint>) {
        val updatedEntries = points.map { point ->
            object : ChartEntry {
                override val x: Float =
                    String.format("%.2f", point.x)
                        .toFloat() // Ensure x is at most 2 decimal places
                override val y: Float = point.y

                override fun withY(newY: Float): ChartEntry {
                    return object : ChartEntry {
                        override val x: Float = String.format("%.2f", point.x)
                            .toFloat() // Ensure x is at most 2 decimal places
                        override val y: Float = newY    // Update the y value
                        override fun withY(y: Float): ChartEntry {
                            return this.withY(y)
                        }
                    }
                }
            }
        }
        chartEntryModelProducer.setEntries(updatedEntries)
    }


    // Function to clear the chart data
    fun clearChartData() {
        chartEntryModelProducer.setEntries()
    }

    // Optional: Helper to format X-axis labels
    fun getFormattedXAxisLabel(value: Float): String {
        return "X-$value"
    }

    // Optional: Helper to format Y-axis labels
    fun getFormattedYAxisLabel(value: Float): String {
        return "${value.toInt()} units"
    }
}