package com.aican.tlcanalyzer.ui.pages.image_analysis

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.ContourType
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.domain.model.spots.ManualContourResult
import com.aican.tlcanalyzer.domain.states.graph.IntensityDataState
import com.aican.tlcanalyzer.domain.states.image.ImageState
import com.aican.tlcanalyzer.libraries.cropper.CropImage
import com.aican.tlcanalyzer.libraries.cropper.CropImageView
import com.aican.tlcanalyzer.ui.activities.DrawRectangleCont
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.AddSpotDialog
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.AnalysisContent
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.LoadingScreen
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.TopPanel
import com.aican.tlcanalyzer.utils.RegionOfInterest
import com.aican.tlcanalyzer.utils.SharedStates
import com.aican.tlcanalyzer.viewmodel.project.ImageAnalysisViewModel
import com.aican.tlcanalyzer.viewmodel.project.IntensityChartViewModel
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.text.DecimalFormat
import kotlin.math.abs
import kotlin.math.sqrt
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import co.yml.charts.common.extensions.isNotNull

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    projectViewModel: ProjectViewModel,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    intensityChartViewModel: IntensityChartViewModel,
    projectId: String,
    isSplitProject: Boolean,
    imageId: String?,
    onNavigate: (String) -> Unit = {},
    onIntensityPlot: () -> Unit = {},
    onBack: () -> Unit
) {

    val context = LocalContext.current

    // Observe state from ViewModels
    val project by projectViewModel.observerProjectDetails(projectId).collectAsState(initial = null)
    if (!isSplitProject) {
        val imagesList by projectViewModel.cachedImagesList.collectAsState()
        // Observe the first image detail if available
        LaunchedEffect(imagesList) {
            if (imagesList.isNotEmpty()) {
                projectViewModel.observeImageDetailByImageId(imagesList[0].imageId)
            }
        }
    } else {
        if (imageId != null) {
            println("Here image id is $imageId")
            projectViewModel.observeImageDetailByImageId(imageId)

        } else {
            println("Here image id is null")
        }
    }
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
    val manualSpots by imageAnalysisViewModel.manualSpots.collectAsState()


    var contourDataSavedToDatabase by rememberSaveable {
        mutableStateOf(false)
    }

    val imageState = remember { mutableStateOf(ImageState()) } // State for the image section

    val contourDataList = imageAnalysisViewModel.allAutoGeneratedSpotsData.collectAsState()

    val manualRectContourListState by SharedStates.manualRectContourListState.collectAsState()

    var showAddSpotDialog by remember { mutableStateOf(false) }
    var showRemoveOrEditSpotDialog by remember { mutableStateOf(false) }

//    BackHandler {
//        println("🔙 System Back Pressed - Navigating Back First")
//
//        // Navigate back immediately
//        onBack()
//
//        // Run cleanup asynchronously to avoid UI delay
//        CoroutineScope(Dispatchers.IO).launch {
//            println("🧹 Clearing ViewModel Data in Background")
//            imageAnalysisViewModel.clearAllData()  // Clear ViewModel state
//            projectViewModel.clearSelectedImage()  // Reset selected image
//            intensityChartViewModel.clearAllData()
//        }
//    }


    LaunchedEffect(manualRectContourListState) {
        // React to changes in shared state
        println("Shared State Changed: $manualRectContourListState")

        if (manualRectContourListState.isNotEmpty() && imageDetail != null) {
            imageAnalysisViewModel.saveManualContourListToDatabase(
                type = ContourType.RECTANGULAR,
                imageDetail!!.imageId,
                manualRectContourListState
            )
            contourDataSavedToDatabase = true
//            contourDataSavedToDatabase = true
        }
    }

    LaunchedEffect(autoGeneratedSpots, manualSpots) {
        if ((autoGeneratedSpots.isNotEmpty() || manualSpots.isNotEmpty()) && imageDetail != null) {
            println("Auto Generated Spots: ${autoGeneratedSpots.size}")
            imageAnalysisViewModel.plotContourOnImage(
                imagePath = imageDetail!!.croppedImagePath,
                contourImagePath = imageDetail!!.contourImagePath ?: "",
                autoSpotModelList = autoGeneratedSpots,
                manualSpots = manualSpots
            )
            imageState.value = imageState.value.copy(
                imagePath = imageDetail!!.contourImagePath ?: "",
                changeTrigger = !imageState.value.changeTrigger // Trigger recomposition
            )
        }
    }


    LaunchedEffect(Unit) {

        contourDataSavedToDatabase = true
        println("contourDataSavedToDatabase == $contourDataSavedToDatabase")
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
            println("imageId: ${imageDetail!!.imageId}")
            imageAnalysisViewModel.fetchAllSpotsFromDatabase(imageId = imageDetail!!.imageId)
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
    LaunchedEffect(imageDetail, numberOfIntensityParts) {
        if (imageDetail != null) {
            fetchIntensityDataIfReady(
                imageDetail = imageDetail!!,
                numberOfIntensityParts = numberOfIntensityParts,
                imageAnalysisViewModel = imageAnalysisViewModel,
                intensityChartViewModel = intensityChartViewModel
            )
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

            },
            addSpotClick = {
                showAddSpotDialog = true

            },
            removeOrEditSpotClick = {
                showRemoveOrEditSpotDialog = true
            },
            onChangeROI = {

            },
            onIntensityPlot = onIntensityPlot
        )

        val cropImageLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data = result.data
                val cropResult = CropImage.getActivityResult(data)

                val rect: Rect? = cropResult?.cropRect

                if (cropResult != null && rect != null) {
                    val df = DecimalFormat("0.00E0")

                    val manualCircularContourList = ArrayList<ManualContourResult>()

                    val area = RegionOfInterest.calculateRectangleArea(rect.width(), rect.height())

//                    val contourBitmap = MediaStore.Images.Media.getBitmap(
//                        context.getContentResolver(),
//                        cropResult.uri
//                    );

                    val contourBitmap =
                        BitmapFactory.decodeFile(File(imageDetail!!.contourImagePath).absolutePath)


                    val imageHeight: Int = contourBitmap!!.getHeight()
                    val distanceFromTop: Double = (rect.top + rect.bottom) * 0.5

                    val maxDistance = imageHeight.toDouble()
                    val rfValue4: Double = 1.0 - (distanceFromTop / maxDistance)

                    val cv: Double = 1 / rfValue4

                    val rfValueTop: Double = rfValue4 + (rect.height() / 2) / imageHeight.toDouble()
                    val rfValueBottom: Double =
                        rfValue4 - (rect.height() / 2) / imageHeight.toDouble()
                    //                double area = pixelArea;

                    val x: Int = rect.left
                    val y: Int = rect.top
                    val w: Int = rect.width()
                    val h: Int = rect.height()

                    val solventFrontDistance: Double = w * 0.5 * (1.0 - 100.0 / 255.0)

                    val contourDistance = sqrt((x * x + y * y).toDouble())

                    val number: Double = area * abs(solventFrontDistance - contourDistance)
                    println(df.format(number))

                    val volume: Double = df.format(number).toDouble()


                    manualCircularContourList.add(
                        ManualContourResult(
                            type = ContourType.CIRCULAR,
                            contourData = ContourData(
                                contourId = "",
                                imageId = "",
                                name = "",
                                area = area.toString(),
                                volume = volume.toString(),
                                rf = rfValue4.toString(),
                                rfTop = rfValueTop.toString(),
                                rfBottom = rfValueBottom.toString(),
                                cv = cv.toString(),
                                chemicalName = "",
                                type = ContourType.CIRCULAR

                            ),
                            rect = rect

                        )
                    )

                    CoroutineScope(Dispatchers.IO).launch {
                        imageAnalysisViewModel.saveManualContourListToDatabase(
                            type = ContourType.CIRCULAR,
                            imageDetail!!.imageId,
                            manualCircularContourList
                        )
                        contourDataSavedToDatabase = true
                    }


                }

                if (cropResult != null) {
                    val croppedUri = cropResult.uri // The cropped image URI
                    println("Cropped image URI: $croppedUri")
                } else {
                    println("Cropping failed or was cancelled.")
                }
            } else {
                println("Cropping failed or was cancelled. result not ok")
            }
        }


        if (showAddSpotDialog) {
            AddSpotDialog(
                onDismissRequest = { showAddSpotDialog = false },
                onSaveClick = { isRectangle ->
                    showAddSpotDialog = false
                    if (isRectangle) {
                        val intent = Intent(context, DrawRectangleCont::class.java)
                        intent.putExtra("contourImagePath", imageDetail?.contourImagePath)
                        context.startActivity(intent)
                    } else {

                        val cropIntent =
                            CropImage.activity(Uri.fromFile(File(imageDetail!!.contourImagePath)))
                                .setGuidelines(CropImageView.Guidelines.ON)
                                .setCropShape(CropImageView.CropShape.OVAL)
                                .setInitialRotation(90)
                                .getIntent(context)
                        cropImageLauncher.launch(cropIntent)
                    }
                }
            )
        }

        if (showRemoveOrEditSpotDialog) {
            RemoveEditSpotDialog(
                contourDataList = contourDataList.value,
                onDismissRequest = { showRemoveOrEditSpotDialog = false },
                onEditClick = {

                },
                onDeleteClick = {

                }
            )
        }
    }
}

@Composable
fun RemoveEditSpotDialog(
    contourDataList: List<ContourData>,
    onDismissRequest: () -> Unit,
    onEditClick: (ContourData) -> Unit,
    onDeleteClick: (ContourData) -> Unit
) {
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = {
            Text(text = "All Spots", style = MaterialTheme.typography.headlineSmall)
        },
        text = {
            LazyColumn {
                items(contourDataList) { contourData ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Display contour name and type
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = contourData.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                text = "Type: ${contourData.type}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Edit button
                        if (contourData.type == ContourType.RECTANGULAR || contourData.type == ContourType.CIRCULAR) {
                            IconButton(onClick = { onEditClick(contourData) }) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        // Delete button
                        IconButton(onClick = { onDeleteClick(contourData) }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(text = "Close")
            }
        }
    )
}


private suspend fun fetchIntensityDataIfReady(
    imageDetail: Image,
    numberOfIntensityParts: Int?,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    intensityChartViewModel: IntensityChartViewModel
) {
    if (imageDetail.isNotNull() && numberOfIntensityParts != null) {
        val croppedImagePath = imageDetail.croppedImagePath ?: return
        val result = imageAnalysisViewModel.fetchIntensityDataWithoutState(
            croppedImagePath, numberOfIntensityParts
        )
        if (result is IntensityDataState.Success) {
            intensityChartViewModel.prepareChartData(result.data)
            imageAnalysisViewModel.saveIntensityData(imageDetail.imageId, result.data)
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

    TopPanel(title = "projectName ",
        onBack = { /* Handle back navigation */ },
        onSettings = { onNavigate("image_analysis_settings") },
        onCropAgain = { onNavigate("crop_screen") })
}


