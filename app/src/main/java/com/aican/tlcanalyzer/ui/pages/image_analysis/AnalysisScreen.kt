package com.aican.tlcanalyzer.ui.pages.image_analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.aican.tlcanalyzer.domain.model.graphs.GraphPoint
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.ActionButton
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.TopPanel
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.ZoomableImage
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.IntensityChartViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.core.entry.ChartEntry
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryModelOf
import com.patrykandpatrick.vico.core.extension.orZero
import com.patrykandpatrick.vico.core.extension.orZeroInt


@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    projectViewModel: ProjectViewModel,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    intensityChartViewModel: IntensityChartViewModel,
    projectId: String,
    onNavigate: (String) -> Unit = {}
) {
    val project by projectViewModel.observerProjectDetails(projectId).collectAsState(initial = null)
    val imageDetails by projectViewModel.observerProjectImages(projectId)
        .collectAsState(initial = emptyList())

    val loadingIntensities by remember { mutableStateOf(false) }

    val numberOfIntensityParts by projectViewModel.observeNumberOfRfCountsByProjectId(projectId = projectId)
        .collectAsState(initial = null)

    val lineChartData by intensityChartViewModel.lineChartDataList.collectAsState()

    val intensityPointData by produceState(
        initialValue = emptyList<GraphPoint>(),
        key1 = imageDetails, key2 = numberOfIntensityParts
    ) {
        if (!imageDetails.isNullOrEmpty() && !imageDetails[0].croppedImagePath.isNullOrEmpty() && numberOfIntensityParts != null) {
            println("Calling this 1")
            value = imageAnalysisViewModel.fetchIntensityDataIntoGraphPointDSet(
                imageDetails[0].croppedImagePath,
                numberOfIntensityParts!!
            )
            intensityChartViewModel.prepareChartData(value)
        }
    }





    if (project == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Loading...", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        Column {
            TopPanel(title = project?.projectName ?: "Unknown Project", onBack = {

            }, onSettings = {
                onNavigate.invoke("image_analysis_settings")
            }, onCropAgain = {

            })

            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    item {
                        // Sticky image at the top
                        if (imageDetails.isNotEmpty()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                ZoomableImage(
                                    imagePath = imageDetails[0].contourImagePath,
                                    description = "Main Image"
                                )
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No image available",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Red
                                )
                            }
                        }
                    }

                    // Graph Section
                    item {
                        Spacer(modifier = Modifier.height(8.dp))

                        if (intensityPointData.isEmpty()) {
                            Text("No intensity data available", color = Color.Red)
                        } else {


                            if (lineChartData.isNotEmpty()) {
                                LineGraph(entries = lineChartData)
                            }
                        }


                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Analysis Loader Section
                    item {
                        AnalysisLoaders(loadingIntensities)
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Spot Detection Section
                    item {
                        SpotDetectionUI()
                    }
                }
            }
        }
    }
}


@Composable
fun LineGraph(
    entries: List<Entry>,
    dataLabel: String = "Intensity Data",
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp), // Adjust the height as needed
        factory = { context ->
            val chart = LineChart(context)

            // Prepare the LineDataSet
            val dataSet = LineDataSet(entries, dataLabel).apply {
                color = android.graphics.Color.BLUE
                valueTextColor = android.graphics.Color.BLACK
                lineWidth = 2f
                circleRadius = 0f
                setCircleColor(android.graphics.Color.RED)
                setDrawValues(false)
                setDrawCircles(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER // Smooth curve
            }

            // Set up the LineData
            chart.data = LineData(dataSet)

            // Chart customization
            chart.apply {
                description.isEnabled = false
                legend.isEnabled = true
                axisRight.isEnabled = false
                xAxis.apply {
                    isGranularityEnabled = true
                    granularity = 1f // Show 1 unit steps on X-axis
                    setDrawGridLines(false)
                    textColor = android.graphics.Color.DKGRAY
                }
                axisLeft.apply {
                    textColor = android.graphics.Color.DKGRAY
                }
                setPinchZoom(true) // Enable zooming and panning
                setTouchEnabled(true)
                setScaleEnabled(true)
                isDragEnabled = true
//                setVisibleXRangeMaximum(10f) // Optional: control the number of visible points

                animateX(400) // Optional animation
            }

            // Refresh chart
            chart.invalidate()
            chart
        },
        update = { chart ->
            // Update the chart if the entries change
            val dataSet = LineDataSet(entries, dataLabel).apply {
                color = android.graphics.Color.BLUE
                valueTextColor = android.graphics.Color.BLACK
                lineWidth = 2f
                circleRadius = 0f
                setCircleColor(android.graphics.Color.RED)
                setDrawValues(false)
                setDrawCircles(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chart.data = LineData(dataSet)
            chart.notifyDataSetChanged()
            chart.invalidate()
        }
    )
}


@Composable
fun AnalysisLoaders(loading: Boolean, loadingText: String = "Loading") {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (loading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = loadingText)
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.then(Modifier.size(32.dp))
                )

            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpotDetectionUI() {
    var threshold by remember { mutableFloatStateOf(100f) }
    var numberOfSpots by remember { mutableFloatStateOf(1f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Spot Detection",
            style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold),
            modifier = Modifier.align(Alignment.Start)
        )

        SpotSlider(
            label = "Threshold",
            value = threshold,
            onValueChange = { threshold = it },
            max = 255f
        )

        SpotSlider(
            label = "No of Spots",
            value = numberOfSpots,
            onValueChange = { numberOfSpots = it },
            max = 100f
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(text = "Generate Spots", onClick = { /* Handle Generate Spots */ })
            ActionButton(text = "Add Spot", onClick = { /* Handle Add Spot */ })
        }
    }
}

@Composable
fun SpotSlider(label: String, value: Float, onValueChange: (Float) -> Unit, max: Float) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$label : ${value.toInt()}", style = TextStyle(fontSize = 16.sp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = { onValueChange((value + 1).coerceAtMost(max)) }) {
                    Icon(

                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Increase $label"
                    )
                }

                IconButton(onClick = { onValueChange((value - 1).coerceAtLeast(0f)) }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Decrease $label"
                    )
                }
            }
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..max,
            modifier = Modifier.fillMaxWidth()
        )
    }
}


