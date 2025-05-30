package com.aican.tlcanalyzer.ui.pages.split_image_section.multiple_image_anal

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aican.tlcanalyzer.data.database.project.entities.IntensityPlotData
import com.aican.tlcanalyzer.domain.model.multiple_analysis.HrVsAreaPer
import com.aican.tlcanalyzer.domain.model.multiple_analysis.ImageAnalysisData
import com.aican.tlcanalyzer.ui.activities.TimeVsAreaGraph
import com.aican.tlcanalyzer.utils.AppUtils.buttonTextSize
import com.aican.tlcanalyzer.utils.AppUtils.getColorByIndex
import com.aican.tlcanalyzer.utils.SharedData
import com.aican.tlcanalyzer.viewmodel.project.MultipleImageAnalysisViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

@Composable
fun MultipleImageAnalysisScreen(
    modifier: Modifier = Modifier,
    multipleImageAnalysisViewModel: MultipleImageAnalysisViewModel,
    onTimeVsAreaClick: () -> Unit,
    onGenerateClick: () -> Unit,
    onAnalyseIntensityGraphClick: () -> Unit,
) {
    val imageAnalysisData by multipleImageAnalysisViewModel.imageAnalysisDataList.collectAsState()

    fun timeVsArea() {
        // Clear previous data
        SharedData.hrVsAreaPerArrayListRM = emptyList()
        SharedData.hrVsAreaPerArrayListFinal = emptyList()

        val hrVsAreaPerArrayRM = mutableListOf<HrVsAreaPer>()
        val hrVsAreaPerArrayFinal = mutableListOf<HrVsAreaPer>()

        if (imageAnalysisData.isNotEmpty()) {
            for (imageData in imageAnalysisData) {
                val contours = imageData.contourData // List of contour data for this image
                val hr = imageData.hour?.toFloatOrNull() ?: continue // Get Hour

                if (contours.isNotEmpty()) {
                    var totalArea = 0f
                    var rmArea = 0f
                    var finalArea = 0f

                    // Calculate total area
                    for (contour in contours) {
                        totalArea += contour.area.toFloat()
                    }

                    // Find RM and Final Spot Area
                    for (contour in contours) {
                        if (contour.contourId == imageData.rm) {
                            rmArea = contour.area.toFloat()
                        }
                        if (contour.contourId == imageData.final) {
                            finalArea = contour.area.toFloat()
                        }
                    }

                    // Calculate % area
                    val rmAreaPercent = if (totalArea > 0) (rmArea / totalArea) * 100 else 0f
                    val finalPercent = if (totalArea > 0) (finalArea / totalArea) * 100 else 0f

                    // Store the calculated values
                    hrVsAreaPerArrayRM.add(HrVsAreaPer(hr, rmAreaPercent))
                    hrVsAreaPerArrayFinal.add(HrVsAreaPer(hr, finalPercent))
                }
            }

            // Assign calculated values to shared data for graph plotting
            SharedData.hrVsAreaPerArrayListRM = hrVsAreaPerArrayRM
            SharedData.hrVsAreaPerArrayListFinal = hrVsAreaPerArrayFinal
        }
    }

    fun generateReport() {
    }

    val context = LocalContext.current

    if (imageAnalysisData.isNotEmpty()) {
        MultipleImageAnalysisContent(
            modifier = modifier,
            imageAnalysisData = imageAnalysisData,
            onGenerateClick = onGenerateClick,
            onAnalyseIntensityGraphClick = onAnalyseIntensityGraphClick,
            onTimeVsAreaClick = {
                timeVsArea()
                val intent = Intent(context, TimeVsAreaGraph::class.java)
                context.startActivity(intent)
            }
        )
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator() // Show a loader if data is not yet loaded
        }
    }
}



@Composable
fun MultipleImageAnalysisContent(
    modifier: Modifier = Modifier,
    imageAnalysisData: List<ImageAnalysisData>,
    onTimeVsAreaClick: () -> Unit,
    onGenerateClick: () -> Unit,
    onAnalyseIntensityGraphClick: () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Multiple Image Analysis",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))


            LazyColumn {
                items(imageAnalysisData) { imageData ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.LightGray)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = imageData.imageName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Image ID: ${imageData.imageId}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }

        // 🔹 Buttons Section - Properly aligned with equal weight distribution
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    onClick = onTimeVsAreaClick
                ) {
                    Text(text = "Time vs % Area", fontSize = buttonTextSize)
                }

                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    onClick = onGenerateClick
                ) {
                    Text(text = "Generate Report", fontSize = buttonTextSize)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    onClick = onAnalyseIntensityGraphClick
                ) {
                    Text(text = "Analyse Intensity Graph", fontSize = buttonTextSize)
                }
            }
        }
    }

}
