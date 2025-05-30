package com.aican.tlcanalyzer.ui.pages.split_image_section.multiple_image_anal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aican.tlcanalyzer.domain.model.multiple_analysis.ImageAnalysisData
import com.aican.tlcanalyzer.utils.AppUtils
import com.aican.tlcanalyzer.utils.AppUtils.getColorByIndex
import com.aican.tlcanalyzer.viewmodel.project.MultipleImageAnalysisViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.github.mikephil.charting.utils.ColorTemplate

@Composable
fun AnalyseMultiIntensityGraphScreen(
    modifier: Modifier = Modifier,
    multipleImageAnalysisViewModel: MultipleImageAnalysisViewModel,
) {
    val imageAnalysisData by multipleImageAnalysisViewModel.imageAnalysisDataList.collectAsState()
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = "Intensity Analysis Graph",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        MultipleChartView(imageAnalysisData = imageAnalysisData)

        Spacer(modifier = Modifier.height(16.dp))
    }

}


@Composable
fun MultipleChartView(
    imageAnalysisData: List<ImageAnalysisData>

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
            val lineData = generateMultiImageLineData(imageAnalysisData, 100)
            chart.data = lineData
            chart.notifyDataSetChanged()
            chart.invalidate() // Refresh chart
        }
    )
}

fun generateMultiImageLineData(imageAnalysisData: List<ImageAnalysisData>, parts: Int): LineData {
    val lineDataSets = mutableListOf<ILineDataSet>()

    imageAnalysisData.forEachIndexed { index, imageData ->
        // 🔹 Create LineDataSet for Image Intensity Plot

        val intensityEntries = imageData.intensityData.map {
            Entry((it.rf.toFloat()), 255 - it.intensity.toFloat())
        }

        println("From Multiple Plot Screen: $intensityEntries")


        val lineDataSet = LineDataSet(intensityEntries, imageData.imageName).apply {
            color = ColorTemplate.COLORFUL_COLORS[index % ColorTemplate.COLORFUL_COLORS.size]
            valueTextSize = 10f
            setDrawCircles(false)
            setDrawValues(false)
        }

        lineDataSets.add(lineDataSet)

        // 🔹 Add Contour Shaded Regions
        imageData.contourData.forEachIndexed { i, contour ->
            var rfTop = contour.rfTop.toFloatOrNull()
            var rfBottom = contour.rfBottom.toFloatOrNull()
            val regionColor =  AppUtils.getLightColorByIndex(
                index + i
            )

            if (rfTop != null && rfBottom != null) {
                rfTop *= parts
                rfBottom *= parts

                val shadedRegion = intensityEntries.filter { entry ->
                    entry.x in rfBottom..rfTop
                }

                val shadedDataSet =
                    LineDataSet(shadedRegion, "${imageData.imageName} Region").apply {
                        setDrawCircles(false)
                        color = android.graphics.Color.parseColor(regionColor)
                        setDrawFilled(true)
                        fillColor = android.graphics.Color.parseColor(regionColor)
                        mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                        lineWidth = 0f
                    }

                lineDataSets.add(shadedDataSet)
            }
        }
    }

    return LineData(lineDataSets)
}

