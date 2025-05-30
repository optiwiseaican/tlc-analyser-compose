package com.aican.tlcanalyzer.ui.pages.image_analysis.components

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.domain.states.graph.IntensityDataState
import com.aican.tlcanalyzer.utils.AppUtils
import com.aican.tlcanalyzer.utils.AppUtils.getColorByIndex
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import java.util.Collections

@Composable
fun IntensityDataSection(
    parts: Int,
    intensityDataState: IntensityDataState,
    lineChartData: List<Entry>,
    contourDataList: List<ContourData>,
    onBitmapCaptured: (Bitmap?) -> Unit

) {
    when (intensityDataState) {
        is IntensityDataState.Loading -> {
            AnalysisLoaders(loading = true, loadingText = "Loading intensity data...")
        }

        is IntensityDataState.Success -> {
            if (lineChartData.isNotEmpty()) {
                LineGraph(
                    parts = parts,
                    entries = lineChartData,
                    contourDataList = contourDataList,
                    onBitmapCaptured = onBitmapCaptured
                )
            } else {
                Text(
                    text = "No intensity data available for graph",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            }
        }

        is IntensityDataState.Empty -> {
            Text(
                text = "No intensity data available",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
        }

        is IntensityDataState.Error -> {
            Text(
                text = "Error: ${(intensityDataState as IntensityDataState.Error).message}",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Red
            )
        }

        null -> {
            Text(
                text = "Intensity data is not yet available.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }

}

@Composable
fun LineGraph(
    parts: Int,
    entries: List<Entry>,
    dataLabel: String = "Intensity Data",
    contourDataList: List<ContourData>,
    onBitmapCaptured: (Bitmap?) -> Unit
) {
    var chartBitmap: Bitmap? = null
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
//                animateX(400)
            }
        },
        update = { chart ->
            println("From Intensity Plot Screen: $entries")
            val mainDataSet = LineDataSet(entries, dataLabel).apply {
                color = android.graphics.Color.BLUE
                valueTextColor = android.graphics.Color.BLACK
                lineWidth = 2f
                setDrawCircles(false)
                setDrawValues(false)
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            // List for shaded datasets
            val shadedDataSets = mutableListOf<LineDataSet>()

            // Generate shaded regions for rfTop and rfBottom
            contourDataList.forEachIndexed { index, contour ->
                var rfTop = contour.rfTop.toFloatOrNull()
                var rfBottom = contour.rfBottom.toFloatOrNull()
                val regionColor = AppUtils.getLightColorByIndex(
                    index
                )
                if (rfTop != null && rfBottom != null) {
                    rfTop *= parts
                    rfBottom *= parts
//                    rfTop = 100 - rfTop
//                    rfBottom = 100 - rfBottom

                    println("rfTop: $rfTop, rfBottom: $rfBottom")

                    val shadedRegion = entries.filter { entry ->
                        entry.x in rfBottom..rfTop
                    }

                    val shadedDataSet = LineDataSet(shadedRegion, "Shaded Region").apply {
                        setDrawCircles(false)
                        color =
                            android.graphics.Color.parseColor(regionColor)
                        setDrawFilled(true)
                        fillColor = android.graphics.Color.parseColor(regionColor)
                        mode = LineDataSet.Mode.HORIZONTAL_BEZIER
                        lineWidth = 0f
                    }

                    shadedDataSets.add(shadedDataSet)
                }
            }

            // Combine all datasets (main line + shaded regions)
            val combinedDataSets: MutableList<ILineDataSet> = mutableListOf()
            combinedDataSets.add(mainDataSet)
            combinedDataSets.addAll(shadedDataSets)

            chart.data = LineData(combinedDataSets)
            chart.notifyDataSetChanged()
            chart.invalidate()

            chart.post {
                try {
                    onBitmapCaptured.invoke(chart.chartBitmap)

                } catch (e: Exception) {
                    onBitmapCaptured.invoke(null)
                }
            }
        }
    )

}


@Composable
fun LineGraph(
    entries: List<Entry>,
    dataLabel: String = "Intensity Data",
//    contourDataList: List<ContourData>
) {


    AndroidView(modifier = Modifier
        .fillMaxWidth()
        .height(300.dp), factory = { context ->
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
            animateX(400)
        }
    }, update = { chart ->
        val dataSet = LineDataSet(entries, dataLabel).apply {
            color = android.graphics.Color.BLUE
            valueTextColor = android.graphics.Color.BLACK
            lineWidth = 2f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        chart.data = LineData(dataSet)
        chart.notifyDataSetChanged()
        chart.invalidate()
    })
}
