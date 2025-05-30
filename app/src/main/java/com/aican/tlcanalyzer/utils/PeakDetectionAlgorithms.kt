package com.aican.tlcanalyzer.utils

import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.domain.model.graphs.MarkedRegion
import com.github.mikephil.charting.data.Entry
import kotlin.math.pow

object PeakDetectionAlgorithms {

    fun contourDataToMarkedRegion(
        contourDataList: List<ContourData>,
        numberOfIntensityParts: Int
    ): List<MarkedRegion> {
        // bandMarkedRegion: [MarkedRegion(left=1.0, right=21.0), MarkedRegion(left=23.0, right=37.0), MarkedRegion(left=73.0, right=77.0)]
        // bandMarkedRegion: [MarkedRegion(left=0.37, right=0.23), MarkedRegion(left=0.77, right=0.73)]

        val markedRegions = mutableListOf<MarkedRegion>()

        contourDataList.forEach { contourData ->
            if (contourData.selected) {
                val markedRegion = MarkedRegion(
                    right = contourData.rfTop.toFloat() * numberOfIntensityParts,
                    left = contourData.rfBottom.toFloat() * numberOfIntensityParts,
                    name = contourData.name
                )
                markedRegions.add(markedRegion)
            }
        }

        return markedRegions
    }

    fun detectPeaksRefined(
        data: List<Entry>,
        lag: Int = 20,
        threshold: Double = 0.3,
        influence: Double = 0.5
    ): List<MarkedRegion> {
        val intensityValues = data.map { it.y.toDouble() }.toDoubleArray()

        // Ensure we have enough data points
        if (intensityValues.size < lag) return emptyList()

        val signals = IntArray(intensityValues.size);
        val filteredData = intensityValues.copyOf()
        val avgFilter = DoubleArray(intensityValues.size)
        val stdFilter = DoubleArray(intensityValues.size)

        for (i in lag until intensityValues.size) {
            val window = filteredData.sliceArray(i - lag until i)
            avgFilter[i] = window.average()
            stdFilter[i] =
                kotlin.math.sqrt(window.sumOf { (it - avgFilter[i]).pow(2) } / window.size)

            if (kotlin.math.abs(intensityValues[i] - avgFilter[i]) > threshold * stdFilter[i]) {
                signals[i] = if (intensityValues[i] > avgFilter[i]) 1 else -1
                filteredData[i] =
                    influence * intensityValues[i] + (1 - influence) * filteredData[i - 1]
            } else {
                signals[i] = 0
                filteredData[i] = intensityValues[i]
            }
        }

        // Detect peak start and end points
        val peakStart = mutableListOf<Int>()
        val peakEnd = mutableListOf<Int>()

        var prevSignal = -1
        for ((i, signal) in signals.withIndex()) {
            if ((signal == 0 || signal == 1) && prevSignal == -1) {
                peakStart.add(i)
            }
            if ((prevSignal == 0 || prevSignal == 1) && signal == -1) {
                peakEnd.add(i)
            }
            prevSignal = signal
        }

        // Convert peak indices to MarkedRegion
        return peakStart.zip(peakEnd).map { (start, end) ->
            MarkedRegion(
                left = data[start].x,
                right = data[end].x
            )
        }
    }

    /**
     * 🔹 Simple Moving Average Smoothing
     */
    fun smoothData(data: DoubleArray, windowSize: Int): DoubleArray {
        val smoothed = DoubleArray(data.size)
        for (i in data.indices) {
            val start = (i - windowSize / 2).coerceAtLeast(0)
            val end = (i + windowSize / 2).coerceAtMost(data.lastIndex)
            smoothed[i] = data.slice(start..end).average()
        }
        return smoothed
    }


}