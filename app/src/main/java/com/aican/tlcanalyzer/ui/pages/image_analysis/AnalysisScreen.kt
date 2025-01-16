package com.aican.tlcanalyzer.ui.pages.image_analysis

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewModelScope
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.domain.model.graphs.IntensityDataState
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.ActionButton
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.TopPanel
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.ZoomableImage
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.ZoomableImageWithTrigger
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.IntensityChartViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    projectViewModel: ProjectViewModel,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    intensityChartViewModel: IntensityChartViewModel,
    projectId: String,
    onNavigate: (String) -> Unit = {}
) {

    DisposableEffect(Unit) {
        println("AnalysisScreen Created")
        onDispose {
            println("AnalysisScreen Disposed")
        }
    }


    // Observe state from ViewModels
    val project by projectViewModel.observerProjectDetails(projectId).collectAsState(initial = null)
    val imagesList by projectViewModel.cachedImagesList.collectAsState()


    val imageDetail by projectViewModel.selectedImageDetail.collectAsState()


    val numberOfIntensityParts by projectViewModel.cachedIntensityParts.collectAsState()
    val intensityDataState by imageAnalysisViewModel.intensityDataState.collectAsState()
    val lineChartData by intensityChartViewModel.lineChartDataList.collectAsState()

    val isContoursFetched by imageAnalysisViewModel.isContoursFetched.collectAsState()

    var imagePath by rememberSaveable {
        mutableStateOf("")
    }
//    var thresholdVal by remember { mutableIntStateOf(0) }
    var thresholdVal by remember {
        mutableIntStateOf(0) // Default to 0 if no image details
    }

    var numberOfSpots by remember {
        mutableIntStateOf(1)
    }


    LaunchedEffect(isContoursFetched) {
        if (isContoursFetched) {
            imageDetail?.let {
                imagePath = it.contourImagePath ?: ""
                imageAnalysisViewModel.resetContoursFetched() // Reset state after refresh
            }
        }
    }
    // Fetch initial data
    LaunchedEffect(projectId) {
        projectViewModel.cacheImageDetails(projectId)
        projectViewModel.cacheIntensityParts(projectId)
    }

    LaunchedEffect(imageDetail) {
        imageDetail?.let {
            println("Threshold Value Updated: ${it.thresholdVal}")
            thresholdVal = it.thresholdVal
            numberOfSpots = it.noOfSpots

            imagePath = it.contourImagePath ?: ""

            val spotsList = imageAnalysisViewModel.generateSpots(
                it.croppedImagePath, it.contourImagePath ?: "", it.thresholdVal, it.noOfSpots
            )
///storage/emulated/0/Android/media/com.aican.tlcanalyzer/TLC_Analyzer/TLC_IDN1736589157743GTUKJH3T/contour_image.jpg
//            /storage/emulated/0/Android/media/com.aican.tlcanalyzer/TLC_Analyzer/TLC_IDN1736589157743GTUKJH3T/cropped_image.jpg
            println("Spot List")
            println(spotsList.size)

        }
    }


    // Fetch intensity data when dependencies are ready
    LaunchedEffect(imagesList, numberOfIntensityParts) {
        fetchIntensityDataIfReady(
            imageDetails = imagesList,
            numberOfIntensityParts = numberOfIntensityParts,
            imageAnalysisViewModel = imageAnalysisViewModel,
            intensityChartViewModel = intensityChartViewModel
        )

    }
    LaunchedEffect(imagesList) {
        if (imagesList.isNotEmpty()) {
            projectViewModel.observeImageDetailByImageId(imagesList[0].imageId)
        }
    }
    // Main UI
    if (project == null || imageDetail == null) {
        LoadingScreen("Loading Project Details...")
    } else {
        AnalysisContent(projectName = project?.projectName ?: "Unknown Project",
            imageDetails = imagesList,
            intensityDataState = intensityDataState,
            lineChartData = lineChartData,
            onNavigate = onNavigate,
            thresholdVal = thresholdVal,
            numberOfSpots = numberOfSpots,
            imagePath = imagePath,
            image = imageDetail!!,
            onGenerateSpots = { thresholdVal, numberOfSpots ->
                imageDetail?.let { _ ->
                    projectViewModel.viewModelScope.launch {

                        projectViewModel.updateImageDetailByImageId(
                            imageDetail!!.copy(
                                thresholdVal = thresholdVal, noOfSpots = numberOfSpots
                            )
                        )

                    }
                }
            })
    }
}

@Composable
fun LoadingScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun AnalysisContent(
    projectName: String,
    imagePath: String,
    imageDetails: List<Image>,
    image: Image,
    intensityDataState: IntensityDataState,
    lineChartData: List<Entry>,
    onNavigate: (String) -> Unit,
    thresholdVal: Int,
    numberOfSpots: Int,
    onGenerateSpots: (Int, Int) -> Unit
) {

    println("AnalysisContent recomposed")
    var isChartVisible by rememberSaveable { mutableStateOf(false) }

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
                item {
                    ImageSection(
                        image = image
                    )
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
                            intensityDataState = intensityDataState, lineChartData = lineChartData
                        )
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


@Composable
fun SpotDetectionUI(thresholdVal: Int, numberOfSpots: Int, onGenerateSpots: (Int, Int) -> Unit) {
    var currentThreshold by remember { mutableIntStateOf(thresholdVal) }

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

        SpotSlider(
            label = "Threshold",
            value = currentThreshold.toFloat(),
            onValueChange = { newValue -> currentThreshold = newValue.toInt() },
            max = 255f
        )

        SpotSlider(
            label = "No of Spots",
            value = currentNumberOfSpots.toFloat(),
            onValueChange = { newValue -> currentNumberOfSpots = newValue.toInt() },
            max = 100f
        )

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(
                text = "Generate Spots",
                onClick = { onGenerateSpots(currentThreshold, currentNumberOfSpots) })
            ActionButton(text = "Add Spot", onClick = { /* Handle Add Spot */ })
        }
    }
}


@Composable
fun ImageSection(image: Image) {
    val recomposeTrigger = remember { mutableIntStateOf(0) }

    // Trigger recomposition when `image` updates
    LaunchedEffect(image) {
        println("image updated in ImageSection")
        recomposeTrigger.intValue++ // Increment the value to force recomposition
    }

    val imagePath = image.contourImagePath ?: ""

    if (imagePath.isNotEmpty()) {
        val imageFile = File(imagePath)
        if (imageFile.exists()) {
            // Show the image if the file exists
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                println("image section recomposed with trigger: ${recomposeTrigger.intValue}")
                ZoomableImage(
                    imagePath = imagePath, // Use the valid imagePath
                    description = "Main Image",
                    recomposeKey = recomposeTrigger.intValue
                )
            }
        } else {
            // Show a message if the file does not exist
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Image file does not exist",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Red
                )
            }
        }
    } else {
        // Show a message if imagePath is empty
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp), contentAlignment = Alignment.Center
        ) {
            Text(
                text = "No image path provided",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

//    if (imageDetails.isNotEmpty()) {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(200.dp)
//        ) {
//
//            ZoomableImage(
//                imagePath = imageDetails[0].contourImagePath ?: "", description = "Main Image"
//            )
//        }
//    } else {
//        Box(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(200.dp), contentAlignment = Alignment.Center
//        ) {
//            Text(
//                text = "No image available",
//                style = MaterialTheme.typography.bodyMedium,
//                color = Color.Red
//            )
//        }
//    }
//}

@Composable
fun IntensityDataSection(intensityDataState: IntensityDataState, lineChartData: List<Entry>) {
    when (intensityDataState) {
        is IntensityDataState.Loading -> {
            AnalysisLoaders(loading = true, loadingText = "Loading intensity data...")
        }

        is IntensityDataState.Success -> {
            if (lineChartData.isNotEmpty()) {
                LineGraph(entries = lineChartData)
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
fun LineGraph(entries: List<Entry>, dataLabel: String = "Intensity Data") {
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

private suspend fun fetchIntensityDataIfReady(
    imageDetails: List<Image>,
    numberOfIntensityParts: Int?,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    intensityChartViewModel: IntensityChartViewModel
) {
    if (imageDetails.isNotEmpty() && numberOfIntensityParts != null) {
        val croppedImagePath = imageDetails[0].croppedImagePath ?: return
        val result = imageAnalysisViewModel.fetchIntensityDataWithoutState(
            croppedImagePath, numberOfIntensityParts
        )
        if (result is IntensityDataState.Success) {
            intensityChartViewModel.prepareChartData(result.data)
        }
    }
}

@Composable
fun AnalysisLoaders(loading: Boolean, loadingText: String = "Loading") {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (loading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = loadingText)
                Spacer(modifier = Modifier.width(8.dp))
                CircularProgressIndicator(
                    modifier = Modifier.then(Modifier.size(32.dp))
                )

            }
        }
    }
}

@Composable
fun SpotSlider(label: String, value: Float, onValueChange: (Float) -> Unit, max: Float) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = "$label : ${value.toInt()}", style = TextStyle(fontSize = 16.sp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(onClick = { onValueChange((value + 1).coerceAtMost(max)) }) {
                    Icon(

                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "Increase $label"
                    )
                }

                IconButton(onClick = { onValueChange((value - 1).coerceAtLeast(0f)) }) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "Decrease $label"
                    )
                }
            }
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = 0f..max,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun TempAnalysisScreen(
    modifier: Modifier = Modifier, onNavigate: (String) -> Unit = {}
) {
    DisposableEffect(Unit) {
        println("AnalysisScreen Created")
        onDispose {
            println("AnalysisScreen Disposed")
        }
    }

    TopPanel(title = "projectName",
        onBack = { /* Handle back navigation */ },
        onSettings = { onNavigate("image_analysis_settings") },
        onCropAgain = { onNavigate("crop_screen") })
}


