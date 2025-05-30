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
import com.aican.tlcanalyzer.utils.PeakDetectionAlgorithms
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
            println("Peak Detection Auto lineChartData: $lineChartData")

            println("Peak Detection Auto lag: $lag, threshold: $threshold, influence: $influence")

            val detectedRegions = PeakDetectionAlgorithms.detectPeaksRefined(
                lineChartData, lag, threshold.toDouble(), influence.toDouble()
            )
            markedRegions = detectedRegions

            // Update contour table data
            contourDataList = calculateContourData(
                detectedRegions, lineChartData, numberOfIntensityParts ?: 100
            )

            // Peak Detection Auto lineChartData: [Entry, x: 1.0 y: 64.17647, Entry, x: 2.0 y: 63.5, Entry, x: 3.0 y: 63.852936, Entry, x: 4.0 y: 63.823532, Entry, x: 5.0 y: 69.647064, Entry, x: 6.0 y: 66.73529, Entry, x: 7.0 y: 67.382355, Entry, x: 8.0 y: 65.23529, Entry, x: 9.0 y: 65.82353, Entry, x: 10.0 y: 66.147064, Entry, x: 11.0 y: 62.441177, Entry, x: 12.0 y: 61.852936, Entry, x: 13.0 y: 61.823532, Entry, x: 14.0 y: 62.058823, Entry, x: 15.0 y: 61.823532, Entry, x: 16.0 y: 61.852936, Entry, x: 17.0 y: 61.882355, Entry, x: 18.0 y: 61.91176, Entry, x: 19.0 y: 62.323532, Entry, x: 20.0 y: 62.294113, Entry, x: 21.0 y: 62.294113, Entry, x: 22.0 y: 62.294113, Entry, x: 23.0 y: 62.47058, Entry, x: 24.0 y: 63.705887, Entry, x: 25.0 y: 64.73529, Entry, x: 26.0 y: 65.82353, Entry, x: 27.0 y: 69.41176, Entry, x: 28.0 y: 73.91176, Entry, x: 29.0 y: 84.47058, Entry, x: 30.0 y: 97.41176, Entry, x: 31.0 y: 116.97058, Entry, x: 32.0 y: 134.05882, Entry, x: 33.0 y: 143.08823, Entry, x: 34.0 y: 147.73529, Entry, x: 35.0 y: 144.67647, Entry, x: 36.0 y: 136.64706, Entry, x: 37.0 y: 123.70589, Entry, x: 38.0 y: 104.73529, Entry, x: 39.0 y: 77.79411, Entry, x: 39.999996 y: 73.5, Entry, x: 41.000004 y: 69.52942, Entry, x: 42.0 y: 68.23529, Entry, x: 43.0 y: 66.55882, Entry, x: 44.0 y: 65.94118, Entry, x: 45.0 y: 65.882355, Entry, x: 45.999996 y: 65.79411, Entry, x: 47.000004 y: 65.617645, Entry, x: 48.0 y: 64.79411, Entry, x: 49.0 y: 64.352936, Entry, x: 50.0 y: 64.41176, Entry, x: 51.0 y: 64.382355, Entry, x: 52.0 y: 64.20589, Entry, x: 53.0 y: 64.05882, Entry, x: 54.0 y: 64.147064, Entry, x: 55.0 y: 64.17647, Entry, x: 56.0 y: 63.91176, Entry, x: 57.0 y: 63.823532, Entry, x: 58.0 y: 63.882355, Entry, x: 59.0 y: 64.02942, Entry, x: 60.0 y: 63.941177, Entry, x: 61.0 y: 63.794113, Entry, x: 62.0 y: 63.941177, Entry, x: 63.0 y: 63.852936, Entry, x: 64.0 y: 64.26471, Entry, x: 65.0 y: 63.73529, Entry, x: 66.0 y: 63.382355, Entry, x: 67.0 y: 63.5, Entry, x: 68.0 y: 63.41176, Entry, x: 69.0 y: 63.26471, Entry, x: 70.0 y: 63.058823, Entry, x: 71.0 y: 63.0, Entry, x: 72.0 y: 62.852936, Entry, x: 73.0 y: 63.02942, Entry, x: 74.0 y: 63.617645, Entry, x: 75.0 y: 63.73529, Entry, x: 76.0 y: 65.117645, Entry, x: 77.0 y: 80.76471, Entry, x: 78.0 y: 64.82353, Entry, x: 79.0 y: 60.676468, Entry, x: 80.0 y: 61.794113, Entry, x: 81.0 y: 62.52942, Entry, x: 82.0 y: 62.558823, Entry, x: 83.0 y: 62.676468, Entry, x: 84.0 y: 63.0, Entry, x: 85.0 y: 63.23529, Entry, x: 86.0 y: 63.23529, Entry, x: 87.0 y: 63.441177, Entry, x: 88.0 y: 64.23529, Entry, x: 89.0 y: 64.352936, Entry, x: 90.0 y: 64.5, Entry, x: 91.0 y: 65.20589, Entry, x: 92.0 y: 65.79411, Entry, x: 93.0 y: 66.29411, Entry, x: 94.0 y: 66.58824, Entry, x: 95.0 y: 66.82353, Entry, x: 96.0 y: 67.08824, Entry, x: 97.0 y: 67.352936, Entry, x: 98.0 y: 68.117645, Entry, x: 99.0 y: 69.55882, Entry, x: 100.0 y: 72.08824]
            // Peak Detection Auto lag: 20, threshold: 0.5, influence: 0.3

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
    return PeakDetectionAlgorithms.detectPeaksRefined(lineChartData, lag, threshold, influence)
}
