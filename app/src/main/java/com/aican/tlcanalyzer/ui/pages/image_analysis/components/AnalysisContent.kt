package com.aican.tlcanalyzer.ui.pages.image_analysis.components

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.domain.model.graphs.MarkedRegion
import com.aican.tlcanalyzer.domain.states.graph.IntensityDataState
import com.aican.tlcanalyzer.domain.states.image.ImageState
import com.aican.tlcanalyzer.ui.components.topbar_navigation.CustomTopBar
import com.aican.tlcanalyzer.utils.AppUtils
import com.aican.tlcanalyzer.utils.AppUtils.buttonTextSize
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import com.github.mikephil.charting.data.Entry

@Composable
fun AnalysisContent(
    parts: Int,
    projectName: String,
    imagePath: String,
    imageState: ImageState,
    image: Image,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    detectionType: String,
    intensityDataState: IntensityDataState,
    numberOfIntensityParts: Int,
    lineChartData: List<Entry>,
    bandContourDataList: List<ContourData>,
    onNavigate: (String) -> Unit,
    thresholdVal: Int,
    projectViewModel: ProjectViewModel,
    selectUnselectBand: (List<ContourData>) -> Unit,
    numberOfSpots: Int,
    bandTempBitmap: Bitmap? = null,
    contourDataList: List<ContourData>,
    bandMarkedRegion: List<MarkedRegion>,
    onGenerateSpots: (Int, Int) -> Unit,
    addSpotClick: () -> Unit,
    removeOrEditSpotClick: () -> Unit,
    onClearAll: () -> Unit,
    onChangeROI: () -> Unit,
    onIntensityPlot: () -> Unit,
    onManageSpot: () -> Unit,
    startBandAnalysis: (Boolean, Float, Int, Float) -> Unit,
    saveTheseBands: (List<ContourData>) -> Unit,

    ) {
    var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) } // Track selected tab (0 for Detect, 1 for Report)
    val tabs = listOf("Detect", "Report")
    val isChartVisible by rememberSaveable { mutableStateOf(false) }
    val isTableVisible by rememberSaveable { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(detectionType) {
        selectedTab = if (detectionType == AppUtils.BAND_DETECTION_TYPE) 1 else 0
    }


    Scaffold(topBar = {
//            TopPanel(
//                title = projectName,
//                onBack = { /* Handle back navigation */ },
//                onSettings = { onNavigate("image_analysis_settings") },
//                onCropAgain = { onNavigate("crop_screen") }
//            )

        CustomTopBar(title = projectName, onBackClick = { /* Handle back navigation */ })
    }) { internalPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(internalPadding)
        ) {
            // Top Panel

//            if (imageState.imageBitmap != null){
//                println("Image State bitmap is not null")
//            }

            ImageSection(imageState)


            // Tab Row
            TabRow(selectedTabIndex = selectedTabIndex, modifier = Modifier.fillMaxWidth()) {
                tabs.forEachIndexed { index, title ->
                    Tab(selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(text = title) })
                }
            }

            // Tab Content
            when (selectedTabIndex) {
                0 -> {
                    // Detect Tab Content
                    Column(modifier = Modifier.fillMaxSize()) {


                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Top
                        ) {
                            Spacer(modifier = Modifier.height(4.dp))

                            DetectionTypeTab(selectedTab = selectedTab,
                                onTabSelected = { selectedTab = it })

                            Spacer(modifier = Modifier.height(4.dp))

                            when (selectedTab) {
                                0 -> SpotAnalyserLayout(
                                    modifier = Modifier.fillMaxSize(),
                                    parts = parts,
                                    projectName = projectName,
                                    imagePath = imagePath,
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
                                    onIntensityPlot = onIntensityPlot,
                                    onManageSpot = onManageSpot
                                )

                                1 -> BandAnalyserLayout(
                                    modifier = Modifier.fillMaxSize(),
                                    parts = parts,
                                    projectName = projectName,
                                    imagePath = imagePath,
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
                                    onIntensityPlot = onIntensityPlot,
                                    onManageSpot = onManageSpot,
                                    bandMarkedRegion = bandMarkedRegion,
                                    startBandAnalysis = startBandAnalysis,
                                    bandContourDataList = bandContourDataList,
                                    imageAnalysisViewModel = imageAnalysisViewModel,
                                    numberOfIntensityParts = numberOfIntensityParts,
                                    projectViewModel = projectViewModel,
                                    selectUnselectBand = selectUnselectBand
                                    , saveTheseBands = saveTheseBands
                                )
                            }
                        }

                        // Place AnalyserLayout below ImageSection

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
fun BandAnalyserLayout(
    modifier: Modifier = Modifier,
    parts: Int,
    projectName: String,
    imagePath: String,
    imageState: ImageState,
    image: Image,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    intensityDataState: IntensityDataState,
    lineChartData: List<Entry>,
    bandMarkedRegion: List<MarkedRegion>,
    onNavigate: (String) -> Unit,
    thresholdVal: Int,
    numberOfIntensityParts: Int,
    numberOfSpots: Int,
    contourDataList: List<ContourData>,
    onGenerateSpots: (Int, Int) -> Unit,
    bandContourDataList: List<ContourData>,
    startBandAnalysis: (Boolean, Float, Int, Float) -> Unit,
    selectUnselectBand: (List<ContourData>) -> Unit,
    addSpotClick: () -> Unit,
    removeOrEditSpotClick: () -> Unit,
    isChartVisibleR: Boolean,
    projectViewModel: ProjectViewModel,
    saveTheseBands: (List<ContourData>) -> Unit,
    isTableVisibleR: Boolean,
    onClearAll: () -> Unit,
    onChangeROI: () -> Unit,
    onManageSpot: () -> Unit,
    onIntensityPlot: () -> Unit,

    ) {


    val isChartVisible by rememberSaveable { mutableStateOf(isChartVisibleR) }
    val isTableVisible by rememberSaveable { mutableStateOf(isTableVisibleR) }

    LazyColumn(modifier = Modifier.fillMaxSize()) {


        item {

            ManageSpotsLayout(
                onClearAll = onClearAll,
                onManageSpot = onManageSpot,
                removeOrEditSpotClick = removeOrEditSpotClick
            )
        }


        if (isChartVisible) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                IntensityDataSection(
                    parts = parts,
                    intensityDataState = intensityDataState,
                    lineChartData = lineChartData,
                    contourDataList = contourDataList
                ) {

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


        // Band Detection Section
        item {
            BandDetectionUI(
                thresholdVal = thresholdVal,
                bandMarkedRegion = bandMarkedRegion,
                contourDataList = bandContourDataList,
                lineChartData = lineChartData,
                numberOfSpots = numberOfSpots,
                imageAnalysisViewModel = imageAnalysisViewModel,
                selectUnselectBand = selectUnselectBand,
                startBandAnalysis = startBandAnalysis,
                numberOfIntensityParts = numberOfIntensityParts,
                addSpotClick = addSpotClick,
                projectViewModel = projectViewModel,
                saveTheseBands = saveTheseBands
            )
        }

        item {
            RegionOfIntUI(
                onChangeROI = onChangeROI, onIntensityPlot = onIntensityPlot
            )
        }
    }


}

@Composable
fun ManageSpotsLayout(
    modifier: Modifier = Modifier,
    onClearAll: () -> Unit,
    onManageSpot: () -> Unit,
    removeOrEditSpotClick: () -> Unit,
) {
    Column {
        Spacer(modifier = Modifier.height(8.dp))
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
                    Text(text = "Clear All", fontSize = buttonTextSize)
                }

                // Manage Spots Button
                Button(modifier = Modifier.fillMaxWidth(), onClick = onManageSpot) {
                    Text(text = "Manage Spots", fontSize = buttonTextSize)
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
                    Text(text = "Remove/Edit Spots", fontSize = buttonTextSize)
                }

                // Revert to Main Image Button
                Button(modifier = Modifier.fillMaxWidth(), onClick = {
                    // TODO: Add logic to revert back to the main/original image $modifier
                    println("Revert to Main Image clicked")
                }) {
                    Text(text = "Revert to Main Image", fontSize = buttonTextSize)
                }
            }
        }
    }
}


@Composable
fun SpotAnalyserLayout(
    modifier: Modifier = Modifier,
    parts: Int,
    projectName: String,
    imagePath: String,
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
    onManageSpot: () -> Unit,
    onIntensityPlot: () -> Unit,
) {

    val isChartVisible by rememberSaveable { mutableStateOf(isChartVisibleR) }
    val isTableVisible by rememberSaveable { mutableStateOf(isTableVisibleR) }


    LazyColumn(modifier = Modifier.fillMaxSize()) {


        item {

            ManageSpotsLayout(
                onClearAll = onClearAll,
                onManageSpot = onManageSpot,
                removeOrEditSpotClick = removeOrEditSpotClick
            )

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
                    intensityDataState = intensityDataState,
                    lineChartData = lineChartData,
                    contourDataList = contourDataList
                ) {

                }
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
                onChangeROI = onChangeROI, onIntensityPlot = onIntensityPlot
            )
        }
    }

}


@Composable
fun DetectionTypeTab(
    selectedTab: Int, onTabSelected: (Int) -> Unit
) {
    val tabs = listOf("Spot Detection", "Band Detection")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .height(50.dp)
            .clip(RoundedCornerShape(25.dp))
            .background(Color(0xFFFFE0E0)) // Light pink background
            .padding(4.dp), horizontalArrangement = Arrangement.Center
    ) {
        tabs.forEachIndexed { index, title ->
            val isSelected = index == selectedTab

            Box(modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clip(RoundedCornerShape(25.dp))
                .background(if (isSelected) Color(0xFF1A144A) else Color.Transparent) // Dark purple for selected
                .clickable { onTabSelected(index) }
                .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center) {
                Text(
                    text = title,
                    color = if (isSelected) Color.White else Color(0xFF1A144A), // White text on selected, purple otherwise
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
