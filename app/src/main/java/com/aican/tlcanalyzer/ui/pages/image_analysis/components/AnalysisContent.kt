package com.aican.tlcanalyzer.ui.pages.image_analysis.components

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
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.domain.states.graph.IntensityDataState
import com.aican.tlcanalyzer.domain.states.image.ImageState
import com.github.mikephil.charting.data.Entry

@Composable
fun AnalysisContent(
    parts: Int,
    projectName: String,
    imagePath: String,
    imageDetails: List<Image>,
    imageState: ImageState,
    image: Image,
    intensityDataState: IntensityDataState,
    lineChartData: List<Entry>,
    onNavigate: (String) -> Unit,
    thresholdVal: Int,
    numberOfSpots: Int,
    contourDataList: List<ContourData>,
    onGenerateSpots: (Int, Int) -> Unit,
    onClearAll: () -> Unit
) {

    println("AnalysisContent recomposed")
    var isChartVisible by rememberSaveable { mutableStateOf(false) }
    var isTableVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(image) {
        println("image updated changed")
    }

    Column {
        // Top Panel
        TopPanel(title = projectName,
            onBack = { /* Handle back navigation */ },
            onSettings = { onNavigate("image_analysis_settings") },
            onCropAgain = { onNavigate("crop_screen") })

        // Main Content
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                // Image Section
//                item {
//                    ImageSection(
//                        image = image
//                    )
//                }

                item {
                    ImageSection(imageState)
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    val textSize = 12.sp
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            // Clear All Button
                            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                onClearAll.invoke()
                            }) {
                                Text(text = "Clear All", fontSize = textSize)
                            }

                            // Manage Spots Button
                            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                // TODO: Add logic to open a spot management dialog or screen
                                println("Manage Spots clicked")
                            }) {
                                Text(text = "Manage Spots", fontSize = textSize)
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 4.dp)
                        ) {
                            // Remove/Edit Spots Button
                            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                // TODO: Add logic to enable editing/removing specific spots
                                println("Remove/Edit Spots clicked")
                            }) {
                                Text(text = "Remove/Edit Spots", fontSize = textSize)
                            }

                            // Revert to Main Image Button
                            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                                // TODO: Add logic to revert back to the main/original image
                                println("Revert to Main Image clicked")
                            }) {
                                Text(text = "Revert to Main Image", fontSize = textSize)
                            }
                        }
                    }

                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = { isChartVisible = !isChartVisible }) {
                            Text(text = if (isChartVisible) "Hide Chart" else "Show Chart")
                        }
                    }
                }
                // Intensity Data Section (conditionally visible)
                if (isChartVisible) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        IntensityDataSection(
                            parts = parts,
                            intensityDataState = intensityDataState, lineChartData = lineChartData,
                            contourDataList = contourDataList
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(onClick = { isTableVisible = !isTableVisible }) {
                            Text(text = if (isTableVisible) "Hide Detail Table" else "Show Detail Table")
                        }
                    }
                }

                // Intensity Data Section (conditionally visible)
                if (isTableVisible) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        TableScreen(contourDataList)
                    }
                }


                // Spot Detection Section
                item {
                    SpotDetectionUI(
                        thresholdVal = thresholdVal,
                        numberOfSpots = numberOfSpots,
                        onGenerateSpots = onGenerateSpots
                    )
                }
            }
        }
    }
}