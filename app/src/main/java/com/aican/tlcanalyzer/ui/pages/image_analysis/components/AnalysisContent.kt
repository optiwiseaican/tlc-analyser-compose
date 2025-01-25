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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.domain.states.graph.IntensityDataState
import com.aican.tlcanalyzer.domain.states.image.ImageState
import com.aican.tlcanalyzer.ui.components.topbar_navigation.CustomTopBar
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
    addSpotClick: () -> Unit,
    removeOrEditSpotClick: () -> Unit,
    onClearAll: () -> Unit,
    onChangeROI: () -> Unit,
    onIntensityPlot: () -> Unit,
) {
    var selectedTabIndex by rememberSaveable { mutableStateOf(0) } // Track selected tab (0 for Detect, 1 for Report)
    val tabs = listOf("Detect", "Report")
    var isChartVisible by rememberSaveable { mutableStateOf(false) }
    var isTableVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
//            TopPanel(
//                title = projectName,
//                onBack = { /* Handle back navigation */ },
//                onSettings = { onNavigate("image_analysis_settings") },
//                onCropAgain = { onNavigate("crop_screen") }
//            )

            CustomTopBar(
                title = projectName,
                onBackClick = { /* Handle back navigation */ }
            )
        }
    ) { internalPadding ->
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(internalPadding)) {
            // Top Panel


            ImageSection(imageState)


            // Tab Row
            TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) }
                    )
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Detect Tab Content
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Display the ImageSection at the top

                        // Place AnalyserLayout below ImageSection
                        AnalyserLayout(
                            modifier = Modifier.fillMaxSize(),
                            parts = parts,
                            projectName = projectName,
                            imagePath = imagePath,
                            imageDetails = imageDetails,
                            imageState = imageState,
                            image = image,
                            intensityDataState = intensityDataState,
                            lineChartData = lineChartData,
                            onNavigate = onNavigate,
                            thresholdVal = thresholdVal,
                            numberOfSpots = numberOfSpots,
                            contourDataList = contourDataList,
                            onGenerateSpots = onGenerateSpots,
                            addSpotClick = addSpotClick,
                            removeOrEditSpotClick = removeOrEditSpotClick,
                            isChartVisibleR = isChartVisible,
                            isTableVisibleR = isTableVisible,
                            onClearAll = onClearAll,
                            onChangeROI = onChangeROI,
                            onIntensityPlot = onIntensityPlot
                        )
                    }
                }

                1 -> {
                    // Report Tab Content
                    ReportSection(
                        onNavigate = onNavigate,
//                    projectViewModel = projectViewModel
                    )
                }
            }
        }

    }

}


@Composable
fun AnalyserLayout(
    modifier: Modifier = Modifier,
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
    addSpotClick: () -> Unit,
    removeOrEditSpotClick: () -> Unit,
    isChartVisibleR: Boolean,
    isTableVisibleR: Boolean,
    onClearAll: () -> Unit,
    onChangeROI: () -> Unit,
    onIntensityPlot: () -> Unit,
) {

    var isChartVisible by rememberSaveable { mutableStateOf(isChartVisibleR) }
    var isTableVisible by rememberSaveable { mutableStateOf(isTableVisibleR) }


    LazyColumn(modifier = Modifier.fillMaxSize()) {


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
                        removeOrEditSpotClick.invoke()
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

//        item {
//            Spacer(modifier = Modifier.height(8.dp))
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center
//            ) {
//                Button(onClick = { isChartVisible = !isChartVisible }) {
//                    Text(text = if (isChartVisible) "Hide Chart" else "Show Chart")
//                }
//            }
//        }
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

//        item {
//            Spacer(modifier = Modifier.height(8.dp))
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center
//            ) {
//                Button(onClick = { isTableVisible = !isTableVisible }) {
//                    Text(text = if (isTableVisible) "Hide Detail Table" else "Show Detail Table")
//                }
//            }
//        }

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
                onGenerateSpots = onGenerateSpots,
                addSpotClick = addSpotClick,

                )
        }

        item {
            RegionOfIntUI(
                onChangeROI = onChangeROI,
                onIntensityPlot = onIntensityPlot
            )
        }
    }

}