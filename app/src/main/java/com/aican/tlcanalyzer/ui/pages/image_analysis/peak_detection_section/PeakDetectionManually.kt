package com.aican.tlcanalyzer.ui.pages.image_analysis.peak_detection_section

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.ContourType
import com.aican.tlcanalyzer.domain.model.graphs.MarkedRegion
import com.aican.tlcanalyzer.domain.model.spots.ManualContourResult
import com.aican.tlcanalyzer.domain.states.graph.IntensityDataState
import com.aican.tlcanalyzer.ui.components.topbar_navigation.CustomTopBar
import com.aican.tlcanalyzer.ui.components.topbar_navigation.MenuOption
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.AnalysisLoaders
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.IntensityDataSection
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.LineGraph
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.TableScreen
import com.aican.tlcanalyzer.utils.AppUtils.getColorByIndex
import com.aican.tlcanalyzer.utils.SharedStates
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.IntensityChartViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import org.opencv.android.Utils
import org.opencv.core.Mat
import org.opencv.core.Point
import org.opencv.core.Scalar
import org.opencv.imgproc.Imgproc
import kotlin.math.pow

@Composable
fun PeakDetectionManually(
    modifier: Modifier = Modifier,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    intensityChartViewModel: IntensityChartViewModel,
    projectViewModel: ProjectViewModel,
    peakDetectionManuallySaveClicked: () -> Unit
) {
    val intensityDataState by imageAnalysisViewModel.intensityDataState.collectAsState()
    val lineChartData by intensityChartViewModel.lineChartDataList.collectAsState()
    val numberOfIntensityParts by projectViewModel.cachedIntensityParts.collectAsState()
    val imageDetail by projectViewModel.selectedImageDetail.collectAsState()

    var markedRegions by remember { mutableStateOf<List<MarkedRegion>>(emptyList()) }
    var contourDataList by remember { mutableStateOf<List<ContourData>>(emptyList()) }
    var tempLeftBoundary by remember { mutableStateOf<Float?>(null) }
    var modifiedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Load cropped image as bitmap
    val originalBitmap = remember {
        imageDetail?.croppedImagePath?.let { path ->
            BitmapFactory.decodeFile(path)
        }
    }

    // Update bitmap whenever a new region is marked
    LaunchedEffect(markedRegions) {
        originalBitmap?.let { bitmap ->
            modifiedBitmap =
                drawHighlightedRegions(bitmap, markedRegions, numberOfIntensityParts ?: 100)
        }
    }

    Scaffold(
        topBar = {
            CustomTopBar(
                title = "Plot on Graph",
                onBackClick = { /* Handle back navigation */ },
                menuOptions = listOf(
                    MenuOption(
                        icon = Icons.Default.CheckCircle,
                        description = "Save",
                        onClick = {
                            if (contourDataList.isNotEmpty()) {
                                val manualContourResultList = ArrayList<ManualContourResult>()

                                // 🔹 Loop through marked regions and map to ContourData
                                markedRegions.forEachIndexed { index, region ->
                                    val rfTop = (1 - region.right / (numberOfIntensityParts
                                        ?: 100)) * (originalBitmap?.height ?: 0)
                                    val rfBottom = (1 - region.left / (numberOfIntensityParts
                                        ?: 100)) * (originalBitmap?.height ?: 0)

                                    val rect = Rect(
                                        0, // Full width
                                        rfTop.toInt(),
                                        (originalBitmap?.width ?: 0),
                                        rfBottom.toInt()
                                    )

                                    val contourData =
                                        contourDataList.getOrNull(index) ?: return@forEachIndexed

                                    manualContourResultList.add(
                                        ManualContourResult(
                                            ContourType.RECTANGULAR,
                                            contourData,
                                            rect
                                        )
                                    )
                                }

                                // 🔥 Update manual contour state
                                updateManualRectContourListState(manualContourResultList)
                                peakDetectionManuallySaveClicked.invoke()

                            }
                        }
                    )
                )
            )
        },
    ) { internalPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(internalPadding)
        ) {

            // 🔹 GRAPH SECTION
            item {
                Spacer(modifier = Modifier.height(18.dp))
                PeakMarkOnGraph(
                    parts = numberOfIntensityParts ?: 100,
                    intensityDataState = intensityDataState,
                    lineChartData = lineChartData,
                    contourDataList = contourDataList,
                    markedRegions = markedRegions,
                    onTap = { xValue ->
                        if (tempLeftBoundary == null) {
                            tempLeftBoundary = xValue
                        } else {
                            val newRegion = MarkedRegion(tempLeftBoundary!!, xValue)
                            markedRegions = markedRegions + newRegion
                            tempLeftBoundary = null

                            // 🔥 Update Table Data when region is marked
                            contourDataList = calculateContourData(
                                markedRegions,
                                lineChartData,
                                numberOfIntensityParts ?: 100
                            )
                        }
                    }
                )
            }

            // 🔹 IMAGE DISPLAY WITH REAL-TIME HIGHLIGHTING
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


            // ✅ BUTTONS: UNDO & CLEAR ALL
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 🔄 UNDO LAST REGION BUTTON
                    Button(
                        onClick = {
                            if (markedRegions.isNotEmpty()) {
                                markedRegions = markedRegions.dropLast(1)
                                contourDataList = calculateContourData(
                                    markedRegions,
                                    lineChartData,
                                    numberOfIntensityParts ?: 100
                                )
                            } else {
                                tempLeftBoundary = null
                            }
                        },
                        enabled = markedRegions.isNotEmpty() || tempLeftBoundary != null,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (markedRegions.isNotEmpty() || tempLeftBoundary != null) Color.Gray else Color.LightGray
                        )
                    ) {
                        Text("Undo Last Region")
                    }

                    // 🗑 CLEAR ALL BUTTON
                    Button(
                        onClick = {
                            markedRegions = emptyList()
                            contourDataList = emptyList()
                            tempLeftBoundary = null
                        },
                        enabled = markedRegions.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (markedRegions.isNotEmpty()) Color.Red else Color.LightGray
                        )
                    ) {
                        Text("Clear All")
                    }
                }
            }

            // 🔹 MARKED REGIONS LIST
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Marked Regions:", style = MaterialTheme.typography.bodyMedium)
                markedRegions.forEach { region ->
                    Text(text = "Start: ${"%.2f".format(region.left)}, End: ${"%.2f".format(region.right)}")
                }
            }

            // 🔹 TABLE SECTION
            item {
                Spacer(modifier = Modifier.height(8.dp))
                TableScreen(contourDataList)
            }
        }
    }
}

private fun updateManualRectContourListState(rectArrayList: List<ManualContourResult>) {
    SharedStates.updateManualRectContourList(rectArrayList)

}

/**
 * 🔥 Function to Draw Highlighted Regions on Image
 */
fun drawHighlightedRegions(
    inputBitmap: Bitmap,
    markedRegions: List<MarkedRegion>,
    totalParts: Int
): Bitmap {
    val inputMat = Mat()
    Utils.bitmapToMat(inputBitmap, inputMat)

    val outputMat = inputMat.clone()
    val fullWidth = inputBitmap.width

    // 🔹 Iterate through marked regions and draw rectangles
    markedRegions.forEach { region ->
        val rfTop = (1 - region.right / totalParts) * inputBitmap.height
        val rfBottom = (1 - region.left / totalParts) * inputBitmap.height

        val p1 = Point(0.0, rfBottom.toDouble()) // Full width, bottom
        val p2 = Point(fullWidth.toDouble(), rfTop.toDouble()) // Full width, top

        val color = Scalar(255.0, 0.0, 255.0) // Magenta color
        val thickness = 2

        Imgproc.rectangle(outputMat, p1, p2, color, thickness)
    }

    // Convert back to Bitmap
    val outputBitmap =
        Bitmap.createBitmap(outputMat.cols(), outputMat.rows(), Bitmap.Config.ARGB_8888)
    Utils.matToBitmap(outputMat, outputBitmap)

    return outputBitmap
}


/**
 * 🔥 Function to Calculate Contour Data based on Marked Regions
 */
fun calculateContourData(
    markedRegions: List<MarkedRegion>,
    lineChartData: List<Entry>,
    totalParts: Int
): List<ContourData> {
    return markedRegions.mapIndexed { index, region ->
        val left = region.left
        val right = region.right

        // ✅ Calculate Rf values
        val rfTop = right / totalParts
        val rfBottom = left / totalParts
        val rf = ((left + right) / 2) / totalParts

        // ✅ Calculate Area (Sum of Y-values within region)
        val area = lineChartData.filter { it.x in left..right }.sumOf { it.y.toDouble() }

        // ✅ Calculate Volume
        val volume = area * rf

        // ✅ Calculate CV (Coefficient of Variation)
        val intensityValues = lineChartData.filter { it.x in left..right }.map { it.y.toDouble() }
        val mean = intensityValues.average()
        val standardDeviation =
            kotlin.math.sqrt(intensityValues.sumOf { (it - mean).pow(2) } / intensityValues.size)
        val cv = if (mean != 0.0) (standardDeviation / mean) * 100 else 0.0

        ContourData(
            contourId = "C_${index + 1}",
            imageId = "Image_1",  // Dummy value, you can replace with actual image ID
            name = "C_${index + 1}",
            area = "%.2f".format(area),
            volume = "%.2f".format(volume),
            rf = "%.2f".format(rf),
            rfTop = "%.2f".format(rfTop),
            rfBottom = "%.2f".format(rfBottom),
            cv = "%.2f".format(cv),
            chemicalName = "Unknown",
            type = ContourType.RECTANGULAR
        )
    }
}

@Composable
fun PeakMarkOnGraph(
    modifier: Modifier = Modifier,
    parts: Int,
    intensityDataState: IntensityDataState,
    lineChartData: List<Entry>,
    contourDataList: List<ContourData>,
    markedRegions: List<MarkedRegion>,
    onTap: (Float) -> Unit
) {
    when (intensityDataState) {
        is IntensityDataState.Loading -> {
            AnalysisLoaders(loading = true, loadingText = "Loading intensity data...")
        }

        is IntensityDataState.Success -> {
            if (lineChartData.isNotEmpty()) {
                LineGraphWithTap(
                    intensityData = lineChartData,
                    markedRegions = markedRegions,
                    onTap = onTap
                )
            } else {
                Text(
                    text = "No intensity data available for graph",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            }
        }

        else -> {
            Text(
                text = "No intensity data available",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
        }
    }
}

@Composable
fun LineGraphWithTap(
    intensityData: List<Entry>,
    markedRegions: List<MarkedRegion>,
    onTap: (Float) -> Unit
) {
    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp),
        factory = { context ->
            LineChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = true
                axisRight.isEnabled = false
                xAxis.apply {
                    isGranularityEnabled = true
                    granularity = 1f
                    setDrawGridLines(false)
                }
                axisLeft.apply {
                    textColor = android.graphics.Color.DKGRAY
                }
                setPinchZoom(true)
                setTouchEnabled(true)
                setScaleEnabled(true)
                isDragEnabled = true
            }
        },
        update = { chart ->
            val mainDataSet = LineDataSet(intensityData, "Intensity Data").apply {
                color = android.graphics.Color.BLUE
                valueTextColor = android.graphics.Color.BLACK
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            // ✅ Shaded regions using `MarkedRegion`
            val shadedDataSets = mutableListOf<LineDataSet>()
            markedRegions.forEach { region ->
                val shadedRegion = intensityData.filter { it.x in region.left..region.right }
                val shadedDataSet = LineDataSet(shadedRegion, "Shaded Region").apply {
                    setDrawCircles(false)
                    color = android.graphics.Color.MAGENTA
                    setDrawFilled(true)
                    fillColor = android.graphics.Color.MAGENTA
                    mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                    lineWidth = 0f
                }
                shadedDataSets.add(shadedDataSet)
            }

            val combinedDataSets: MutableList<ILineDataSet> = mutableListOf()
            combinedDataSets.add(mainDataSet)
            combinedDataSets.addAll(shadedDataSets)

            chart.data = LineData(combinedDataSets)
            chart.notifyDataSetChanged()
            chart.invalidate()

            // Detect Tap
            chart.onChartGestureListener = object :
                com.github.mikephil.charting.listener.OnChartGestureListener {
                override fun onChartSingleTapped(me: android.view.MotionEvent?) {
                    me?.let {
                        val highlight = chart.getHighlightByTouchPoint(it.x, it.y)
                        val xValue = highlight?.x
                        if (xValue != null) {
                            onTap(xValue)
                        }
                    }
                }

                override fun onChartGestureStart(
                    me: android.view.MotionEvent?,
                    lastPerformedGesture: com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture?
                ) {
                }

                override fun onChartGestureEnd(
                    me: android.view.MotionEvent?,
                    lastPerformedGesture: com.github.mikephil.charting.listener.ChartTouchListener.ChartGesture?
                ) {
                }

                override fun onChartLongPressed(me: android.view.MotionEvent?) {}
                override fun onChartDoubleTapped(me: android.view.MotionEvent?) {}
                override fun onChartFling(
                    me1: android.view.MotionEvent?,
                    me2: android.view.MotionEvent?,
                    velocityX: Float,
                    velocityY: Float
                ) {
                }

                override fun onChartScale(
                    me: android.view.MotionEvent?,
                    scaleX: Float,
                    scaleY: Float
                ) {
                }

                override fun onChartTranslate(
                    me: android.view.MotionEvent?,
                    dX: Float,
                    dY: Float
                ) {
                }
            }
        }
    )
}



