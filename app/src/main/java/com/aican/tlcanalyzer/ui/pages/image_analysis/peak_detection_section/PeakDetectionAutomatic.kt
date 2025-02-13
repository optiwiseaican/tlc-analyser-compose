package com.aican.tlcanalyzer.ui.pages.image_analysis.peak_detection_section

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.domain.model.graphs.MarkedRegion
import com.aican.tlcanalyzer.ui.components.topbar_navigation.CustomTopBar
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.IntensityDataSection
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.TableScreen
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.IntensityChartViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import com.github.mikephil.charting.data.Entry
import org.opencv.core.Mat
import kotlin.math.pow

@Composable
fun PeakDetectionAutomatic(
    modifier: Modifier = Modifier,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    intensityChartViewModel: IntensityChartViewModel,
    projectViewModel: ProjectViewModel,
) {
    val intensityDataState by imageAnalysisViewModel.intensityDataState.collectAsState()
    val lineChartData by intensityChartViewModel.lineChartDataList.collectAsState()
    val numberOfIntensityParts by projectViewModel.cachedIntensityParts.collectAsState()
    val imageDetail by projectViewModel.selectedImageDetail.collectAsState()

    var markedRegions by remember { mutableStateOf<List<MarkedRegion>>(emptyList()) }
    var contourDataList by remember { mutableStateOf<List<ContourData>>(emptyList()) }
    var modifiedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    var lag by remember { mutableIntStateOf(20) }
    var threshold by remember { mutableFloatStateOf(0.5f) }
    var influence by remember { mutableFloatStateOf(0.3f) }

    // Load cropped image as bitmap
    val originalBitmap = remember {
        imageDetail?.croppedImagePath?.let { path ->
            BitmapFactory.decodeFile(path)
        }
    }

    // Perform automatic peak detection and update UI
    LaunchedEffect(lineChartData, lag, threshold, influence) {
        if (lineChartData.isNotEmpty()) {
            val detectedRegions = detectPeaksRefined(
                lineChartData, lag, threshold.toDouble(), influence.toDouble()
            )
            markedRegions = detectedRegions

            // Update contour table data
            contourDataList = calculateContourData(
                detectedRegions, lineChartData, numberOfIntensityParts ?: 100
            )

            // Update highlighted image
            originalBitmap?.let { bitmap ->
                modifiedBitmap =
                    drawHighlightedRegions(bitmap, detectedRegions, numberOfIntensityParts ?: 100)
            }
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Automatic Peak Detection",
                onBackClick = {

                }
            )
        },
    ) { internalPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(internalPadding)
        ) {
            // GRAPH SECTION
            item {
                Spacer(modifier = Modifier.height(18.dp))
                PeakMarkOnGraph(
                    parts = numberOfIntensityParts ?: 100,
                    intensityDataState = intensityDataState,
                    lineChartData = lineChartData,
                    contourDataList = contourDataList,
                    markedRegions = markedRegions
                ) {

                }
            }

            // IMAGE DISPLAY WITH REAL-TIME HIGHLIGHTING
            item {
                modifiedBitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Highlighted Image",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .padding(8.dp)
                    )
                }
            }

            // SLIDERS FOR PARAMETER CONTROL
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SliderControl(
                        label = "Lag",
                        value = lag.toFloat(),
                        range = 5f..100f,
                        onValueChange = { lag = it.toInt() }
                    )
                    SliderControl(
                        label = "Threshold",
                        value = threshold,
                        range = 0.1f..1.0f,
                        onValueChange = { threshold = it }
                    )
                    SliderControl(
                        label = "Influence",
                        value = influence,
                        range = 0.0f..1.0f,
                        onValueChange = { influence = it }
                    )
                }
            }

            // TABLE SECTION
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TableScreen(contourDataList)
            }
        }
    }
}

/**
 * 🔥 Slider UI Component
 */
@Composable
fun SliderControl(
    label: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "$label: ${"%.2f".format(value)}", style = MaterialTheme.typography.bodyMedium)
        Slider(
            value = value,
            valueRange = range,
            onValueChange = { onValueChange(it) },
            modifier = Modifier.padding(vertical = 8.dp)
        )
    }
}


fun detectPeaksAndConvertToRegions(
    lineChartData: List<Entry>,
    totalParts: Int,
    lag: Int,
    threshold: Double,
    influence: Double
): List<MarkedRegion> {
    return detectPeaksRefined(lineChartData, lag, threshold, influence)
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

    val signals = IntArray(intensityValues.size)
    val filteredData = intensityValues.copyOf()
    val avgFilter = DoubleArray(intensityValues.size)
    val stdFilter = DoubleArray(intensityValues.size)

    for (i in lag until intensityValues.size) {
        val window = filteredData.sliceArray(i - lag until i)
        avgFilter[i] = window.average()
        stdFilter[i] = kotlin.math.sqrt(window.sumOf { (it - avgFilter[i]).pow(2) } / window.size)

        if (kotlin.math.abs(intensityValues[i] - avgFilter[i]) > threshold * stdFilter[i]) {
            signals[i] = if (intensityValues[i] > avgFilter[i]) 1 else -1
            filteredData[i] = influence * intensityValues[i] + (1 - influence) * filteredData[i - 1]
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

