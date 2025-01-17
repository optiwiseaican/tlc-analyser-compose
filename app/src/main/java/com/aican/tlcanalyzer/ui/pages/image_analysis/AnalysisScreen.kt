package com.aican.tlcanalyzer.ui.pages.image_analysis

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewModelScope
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.domain.states.graph.IntensityDataState
import com.aican.tlcanalyzer.domain.states.image.ImageState
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.ActionButton
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.AnalysisContent
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.LoadingScreen
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.TopPanel
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.ZoomableImage
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.IntensityChartViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import  java.util.ArrayList

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

    var spotContourButtonClicked by remember { mutableStateOf(false) }
    var imagePath by rememberSaveable { mutableStateOf("") }
    var thresholdVal by remember { mutableIntStateOf(0) }
    var numberOfSpots by remember { mutableIntStateOf(1) }

    val autoGeneratedSpots by imageAnalysisViewModel.autoGeneratedSpots.collectAsState()


    var contourDataSavedToDatabase by rememberSaveable {
        mutableStateOf(false)
    }

    val imageState = remember { mutableStateOf(ImageState()) } // State for the image section

    val contourDataList = imageAnalysisViewModel.allContourData.collectAsState()


    LaunchedEffect(autoGeneratedSpots) {
        if (autoGeneratedSpots.isNotEmpty() && imageDetail != null) {
            println("Auto Generated Spots: ${autoGeneratedSpots.size}")
            imageAnalysisViewModel.plotContourOnImage(
                imagePath = imageDetail!!.croppedImagePath,
                contourImagePath = imageDetail!!.contourImagePath ?: "",
                autoSpotModelList = autoGeneratedSpots
            )
        }
    }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        contourDataSavedToDatabase = true
    }


    // Triggered only when the button is clicked
    LaunchedEffect(spotContourButtonClicked) {
        if (spotContourButtonClicked) {
            imageDetail?.let { detail ->
                // Use ViewModel's function to generate spots and handle result
                val generatedSpotList = imageAnalysisViewModel.generateSpots(
                    imagePath = detail.croppedImagePath,
                    contourImagePath = detail.contourImagePath ?: "",
                    threshold = detail.thresholdVal,
                    numberOfSpots = detail.noOfSpots
                ) { message ->
                    // Show a toast message on the main thread
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    }
                }

                // Save spots data to the database
                imageAnalysisViewModel.saveAutoContourDataToDatabase(
                    detail.imageId,
                    contourDataList = generatedSpotList
                )

                println("Spot List generated and saved")
                println("Total spots: ${generatedSpotList.size}")

                contourDataSavedToDatabase = true // Mark data as saved
            }

            spotContourButtonClicked = false // Reset the state to avoid re-triggering
        }
    }

    // Update imagePath when contours are fetched
    LaunchedEffect(isContoursFetched) {
        if (isContoursFetched) {
            imageDetail?.let {
                imagePath = it.contourImagePath ?: ""
                imageAnalysisViewModel.resetContoursFetched()

                imageState.value = imageState.value.copy(
                    imagePath = it.contourImagePath ?: "",
                    description = "Updated Image",
                    changeTrigger = !imageState.value.changeTrigger // Trigger recomposition
                )

            }
        }
    }

    // Fetch initial data
    LaunchedEffect(projectId) {
        projectViewModel.cacheImageDetails(projectId)
        projectViewModel.cacheIntensityParts(projectId)
    }

    LaunchedEffect(imageDetail, contourDataSavedToDatabase) {
        if (imageDetail != null && contourDataSavedToDatabase) {
            println("fetchAutoGeneratedSpotsFromDatabase invoked")
            imageAnalysisViewModel.fetchAutoGeneratedSpotsFromDatabase(imageId = imageDetail!!.imageId)
            contourDataSavedToDatabase = false
        }
    }
    // Update threshold and numberOfSpots when imageDetail changes
    LaunchedEffect(imageDetail) {
        imageDetail?.let {
            println("Threshold Value Updated: ${it.thresholdVal}")
            thresholdVal = it.thresholdVal
            numberOfSpots = it.noOfSpots
            imagePath = it.contourImagePath ?: ""

            imageState.value = imageState.value.copy(
                imagePath = it.contourImagePath ?: "",
                description = "Updated Image",
                changeTrigger = !imageState.value.changeTrigger // Trigger recomposition
            )

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

    // Observe the first image detail if available
    LaunchedEffect(imagesList) {
        if (imagesList.isNotEmpty()) {
            projectViewModel.observeImageDetailByImageId(imagesList[0].imageId)
        }
    }

    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        AlertDialog(onDismissRequest = { showDialog = false },
            title = { Text(text = "Confirm Clear All") },
            text = { Text(text = "Are you sure you want to clear all data? This action cannot be undone.") },
            confirmButton = {
                Button(onClick = {
                    showDialog = false
                    imageDetail?.let { image ->
                        CoroutineScope(Dispatchers.IO).launch {
                            imageAnalysisViewModel.clearAllContours(
                                image.imageId, image.croppedImagePath, image.contourImagePath
                            )
                            println("Clear All triggered for imageId: ${image.imageId}")
                            imageState.value = imageState.value.copy(
                                imagePath = image.croppedImagePath,
                                changeTrigger = !imageState.value.changeTrigger // Force recomposition
                            )
                        }
                    }
                }) {
                    Text("Clear")
                }
            },
            dismissButton = {
                Button(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            })
    }

    // Main UI
    if (project == null || imageDetail == null) {
        LoadingScreen("Loading Project Details...")
    } else {
        AnalysisContent(
            parts = numberOfIntensityParts ?: 100,
            projectName = project?.projectName ?: "Unknown Project",
            imageDetails = imagesList,
            intensityDataState = intensityDataState,
            lineChartData = lineChartData,
            onNavigate = onNavigate,
            thresholdVal = thresholdVal,
            numberOfSpots = numberOfSpots,
            imagePath = imagePath,
            image = imageDetail!!,
            imageState = imageState.value,
            contourDataList = contourDataList.value,
            onGenerateSpots = { newThresholdVal, newNumberOfSpots ->
                imageDetail?.let {
                    projectViewModel.viewModelScope.launch {
                        projectViewModel.updateImageDetailByImageId(
                            it.copy(thresholdVal = newThresholdVal, noOfSpots = newNumberOfSpots)
                        )
                        thresholdVal = newThresholdVal
                        numberOfSpots = newNumberOfSpots
                        spotContourButtonClicked = true // Trigger spot generation
                    }
                }
            },
            onClearAll = {
                showDialog = true

            })
    }
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


