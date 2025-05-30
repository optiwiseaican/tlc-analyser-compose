package com.aican.tlcanalyzer.ui.pages.image_analysis.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.domain.model.graphs.MarkedRegion
import com.aican.tlcanalyzer.ui.pages.image_analysis.peak_detection_section.PeakMarkOnGraph
import com.aican.tlcanalyzer.utils.AppUtils
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import com.github.mikephil.charting.data.Entry


@Composable
fun BandDetectionUI(
    thresholdVal: Int,
    numberOfSpots: Int,
    bandMarkedRegion: List<MarkedRegion>,
    lineChartData: List<Entry>,
    contourDataList: List<ContourData>,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    projectViewModel: ProjectViewModel,
    numberOfIntensityParts: Int,
    startBandAnalysis: (Boolean, Float, Int, Float) -> Unit,
    selectUnselectBand: (List<ContourData>) -> Unit,
    saveTheseBands: (List<ContourData>) -> Unit,
    addSpotClick: () -> Unit,
) {
    var currentThreshold by remember { mutableIntStateOf(thresholdVal) }
    var bandAnalysisStarted by rememberSaveable { mutableStateOf(false) }
    var showAdvanceOptions by rememberSaveable { mutableStateOf(false) }
    val intensityDataState by imageAnalysisViewModel.intensityDataState.collectAsState()
    val imageDetail by projectViewModel.selectedImageDetail.collectAsState()


    var modifiedBitmap by remember { mutableStateOf<Bitmap?>(null) }


    var currentNumberOfSpots by remember {
        mutableIntStateOf(numberOfSpots)
    }
    // Synchronize currentThreshold with thresholdVal whenever it changes
    LaunchedEffect(thresholdVal) {
        currentThreshold = thresholdVal

    }
    LaunchedEffect(numberOfSpots) {
        currentNumberOfSpots = numberOfSpots
    }

    val lag by remember { mutableIntStateOf(20) }
    var threshold by remember { mutableFloatStateOf(0.5f) }
    val influence by remember { mutableFloatStateOf(0.3f) }
    var showDialog by remember { mutableStateOf(false) }

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



        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                containerColor = if (bandAnalysisStarted) Color.Red else Color.Magenta,
                modifier = Modifier.weight(1.5f),
                text = if (bandAnalysisStarted) "Stop Band Analysis" else "Start Band Analysis",
                onClick = {
                    bandAnalysisStarted = !bandAnalysisStarted
                    startBandAnalysis(
                        bandAnalysisStarted,
                        currentThreshold.toFloat(),
                        lag,
                        influence
                    )

                })
            ActionButton(
                modifier = Modifier.weight(1f),
                text = "Add Band", onClick = addSpotClick
            )
        }
        if (bandAnalysisStarted) {
            SpotSlider(
                label = "Threshold",
                value = currentThreshold.toFloat(),
                onValueChange = { newValue ->
                    currentThreshold = newValue.toInt()
                    startBandAnalysis(
                        bandAnalysisStarted,
                        currentThreshold.toFloat(),
                        lag,
                        influence
                    )

                },
                max = 255f
            )

            ActionButton(
                text = if (showAdvanceOptions) "Hide Advance Options" else "Show Advance Options",
                onClick = {
                    showAdvanceOptions = !showAdvanceOptions

                }
            )

            if (showAdvanceOptions) {
                PeakMarkOnGraph(
                    parts = numberOfIntensityParts ?: 100,
                    intensityDataState = intensityDataState,
                    lineChartData = lineChartData,
                    contourDataList = contourDataList,
                    markedRegions = bandMarkedRegion
                ) {

                }

                SelectUnselectBands(
                    contourDataList = contourDataList,
                    selectUnselectBand = selectUnselectBand
                )
            }


            ActionButton(
                text = "Save These Bands",
                onClick = {
                    showDialog = true // Show the dialog when button is clicked
                }
            )

            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false }, // Dismiss when clicking outside
                    title = { Text("Warning") },
                    text = { Text("After saving these bands, your previously detected bands will be deleted. Are you sure?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showDialog = false // Close dialog
                                saveTheseBands.invoke(contourDataList)
                                // Perform save action here
                            }
                        ) {
                            Text("Confirm")
                        }
                    },
                    dismissButton = {
                        TextButton(
                            onClick = { showDialog = false } // Just close dialog
                        ) {
                            Text("Cancel")
                        }
                    }
                )
            }

        }

    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SelectUnselectBands(
    modifier: Modifier = Modifier,
    contourDataList: List<ContourData>,
    selectUnselectBand: (List<ContourData>) -> Unit,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {

        contourDataList.forEachIndexed { index, contour ->
            SelectUnselectBandsItems(
                index = index,
                id = contour.contourId,
                text = "Band ${contour.name}",
                selected = true,
                onCheckedChange = { id, isChecked ->

                    contourDataList.find { cont ->
                        if (cont.contourId == id) println("matched")

                        cont.contourId == id
                    }?.selected = isChecked

                    selectUnselectBand.invoke(contourDataList)
                })

        }
    }
}

@Composable
fun SelectUnselectBandsItems(
    index: Int,
    id: String,
    text: String,
    modifier: Modifier = Modifier,
    selected: Boolean,
    onCheckedChange: (String, Boolean) -> Unit
) {

    var itemSelected by remember { mutableStateOf(selected) }

    Surface(
        color = if (itemSelected) Color(
            android.graphics.Color.parseColor(
                AppUtils.getLightColorByIndex(
                    index
                )
            )
        ) else Color.Transparent,
        contentColor = if (itemSelected) Color.Black else Color.Black,
        shape = RoundedCornerShape(50.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (itemSelected) Color.Black else Color.Black
        ),
        onClick = {
            if (itemSelected) {
                itemSelected = false
                onCheckedChange(id, false)
            } else {
                itemSelected = true
                onCheckedChange(id, true)
            }
        },
        modifier = modifier
            .height(40.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(8.dp)

        ) {
            Checkbox(
                checked = itemSelected,
                onCheckedChange = {
                    itemSelected = it
                    onCheckedChange(id, it)
                },
                modifier = Modifier.padding(end = 8.dp)
            )
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(end = 8.dp)
            )
        }
    }
}
