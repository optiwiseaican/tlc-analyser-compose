package com.aican.tlcanalyzer.ui.pages.split_image_section.multiple_image_anal

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aican.tlcanalyzer.data.database.project.entities.IntensityPlotData
import com.aican.tlcanalyzer.domain.model.multiple_analysis.ImageAnalysisData
import com.aican.tlcanalyzer.utils.AppUtils.buttonTextSize
import com.aican.tlcanalyzer.utils.AppUtils.getColorByIndex
import com.aican.tlcanalyzer.viewmodel.project.MultipleImageAnalysisViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun MultipleImageAnalysisScreen(
    modifier: Modifier = Modifier,
    multipleImageAnalysisViewModel: MultipleImageAnalysisViewModel,
    onTimeVsAreaClick: () -> Unit,
    onGenerateClick: () -> Unit,
    onAnalyseIntensityGraphClick: () -> Unit,
) {
    val imageAnalysisData by multipleImageAnalysisViewModel.imageAnalysisDataList.collectAsState()



    if (imageAnalysisData.isNotEmpty()) {
        MultipleImageAnalysisContent(
            modifier = modifier,
            imageAnalysisData = imageAnalysisData,
            onGenerateClick = onGenerateClick,
            onAnalyseIntensityGraphClick = onAnalyseIntensityGraphClick,
            onTimeVsAreaClick = onTimeVsAreaClick
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
