package com.aican.tlcanalyzer.ui.pages.split_image_section.multiple_image_anal

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.data.database.project.entities.IntensityPlotData
import com.aican.tlcanalyzer.domain.model.graphs.GraphPoint
import com.aican.tlcanalyzer.domain.states.graph.IntensityDataState
import com.aican.tlcanalyzer.domain.states.image.ImageState
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.ImageSection
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.IntensityDataSection
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.TableScreen
import com.aican.tlcanalyzer.ui.pages.image_analysis.report_section.BarGraph
import com.aican.tlcanalyzer.ui.pages.image_analysis.report_section.generatePDF
import com.aican.tlcanalyzer.viewmodel.project.MultipleImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun MultipleGenerateReport(
    modifier: Modifier = Modifier,
    multipleImageAnalysisViewModel: MultipleImageAnalysisViewModel,
    projectViewModel: ProjectViewModel
) {
    val imageAnalysisData by multipleImageAnalysisViewModel.imageAnalysisDataList.collectAsState()
    val numberOfIntensityParts by projectViewModel.cachedIntensityParts.collectAsState()

    if (imageAnalysisData.isNotEmpty()) {
        // ✅ Wrapping in a Box with max height to prevent infinite constraints
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                items(imageAnalysisData) { imageData ->
                    BatchReportComposable(
                        modifier = Modifier.fillMaxWidth(),
                        numberOfIntensityParts = numberOfIntensityParts ?: 100,
                        intensityData = imageData.intensityData,
                        contourDataList = imageData.contourData,
                        imageDetail = imageData.imageDetail
                    )
                }
            }
        }
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun BatchReportComposable(
    modifier: Modifier = Modifier,
    numberOfIntensityParts: Int,
    intensityData: List<IntensityPlotData>,
    contourDataList: List<ContourData>,
    imageDetail: Image
) {
    val intensityChartBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val volumeChartBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val contourImageState = remember { mutableStateOf(ImageState()) }
    val originalImageState = remember { mutableStateOf(ImageState()) }
    var intensityDataState by remember { mutableStateOf<IntensityDataState>(IntensityDataState.Loading) }

    val graphData = intensityData.map { data ->
        val rf = data.rf.toFloat()
        val intensity = data.intensity.toFloat()
        Pair(Entry(rf, 255 - intensity), GraphPoint(rf, 255 - intensity, ""))
    }

// Extracting the individual lists
    val lineChartData = graphData.map { it.first }  // Extracts List<Entry>
    val graphPoints = graphData.map { it.second }   // Extracts List<GraphPoint>

    intensityDataState = IntensityDataState.Success(graphPoints)


    LaunchedEffect(imageDetail) {
        imageDetail?.let {
            contourImageState.value = contourImageState.value.copy(
                imagePath = it.contourImagePath ?: "",
                description = "Updated Image",
                changeTrigger = !contourImageState.value.changeTrigger
            )
            originalImageState.value = originalImageState.value.copy(
                imagePath = it.croppedImagePath ?: "",
                description = "Updated Image",
                changeTrigger = !originalImageState.value.changeTrigger
            )
        }
    }

    // ✅ Ensuring height constraint for LazyColumn
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)  // 🔹 Restrict height to avoid infinite constraints
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            item {
                IntensityDataSection(
                    parts = numberOfIntensityParts,
                    intensityDataState = intensityDataState,
                    lineChartData = lineChartData,
                    contourDataList = contourDataList
                ) { chartBitmap ->
                    intensityChartBitmap.value = chartBitmap
                }

                BarGraph(contourDataList = contourDataList) { chartBitmap ->
                    volumeChartBitmap.value = chartBitmap
                }

                ImageSection(imageState = originalImageState.value, zoomable = false)
                ImageSection(imageState = contourImageState.value, zoomable = false)
                TableScreen(contourDataList)
            }
        }
    }
}


//// generate report
//fun generateReport(): Uri? {
//    return withContext(Dispatchers.IO) {
//        delay(1000)
//        try {
//
//        } catch (e: Exception) {
//            Log.e("PDF_ERROR", "Error generating PDF: ${e.message}")
//            return@withContext null
//        }
//    }
//}