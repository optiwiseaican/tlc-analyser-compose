package com.aican.tlcanalyzer.ui.pages.image_analysis

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Rect
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.ui.graphics.Color
import co.yml.charts.common.extensions.isNotNull
import com.aican.tlcanalyzer.domain.model.graphs.MarkedRegion
import com.aican.tlcanalyzer.ui.activities.EditRectangleContour
import com.aican.tlcanalyzer.ui.pages.image_analysis.peak_detection_section.calculateContourData
import com.aican.tlcanalyzer.ui.pages.image_analysis.peak_detection_section.drawHighlightedRegions
import com.aican.tlcanalyzer.utils.AppUtils
import com.aican.tlcanalyzer.utils.PeakDetectionAlgorithms
import com.aican.tlcanalyzer.utils.SharedData

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    projectViewModel: ProjectViewModel,
    imageAnalysisViewModel: ImageAnalysisViewModel,
    intensityChartViewModel: IntensityChartViewModel,
    projectId: String,
    isSplitProject: Boolean,
    imageId: String?,
    projectDetectionType: String? = null,
    onNavigate: (String) -> Unit = {},
    onIntensityPlot: () -> Unit = {},
    onBack: () -> Unit
) {

    val context = LocalContext.current

    var detectionType by remember {
        mutableStateOf<String?>(projectDetectionType)
    }

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

    var lag by remember { mutableIntStateOf(20) }
    var threshold by remember { mutableFloatStateOf(0.5f) }
    var influence by remember { mutableFloatStateOf(0.3f) }

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
    var showManageSpotDialog by remember { mutableStateOf(false) }

    val manualContourEditState by SharedStates.manualContourEditState.collectAsState()

    var selectedRMSpot by remember { mutableStateOf<String?>(null) }
    var selectedFinalSpot by remember { mutableStateOf<String?>(null) }

    var bandMarkedRegion by remember { mutableStateOf<List<MarkedRegion>>(emptyList()) }
    var bandContourDataList by remember { mutableStateOf<List<ContourData>>(emptyList()) }

    var bandTempBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }



    LaunchedEffect(manualContourEditState) {
        if (manualContourEditState!! && imageDetail != null) {

            // Fetch updated contour data from the database
            imageAnalysisViewModel.fetchAllSpotsFromDatabase(imageId = imageDetail!!.imageId)

            imageAnalysisViewModel.plotContourOnImage(
                imagePath = imageDetail!!.croppedImagePath,
                contourImagePath = imageDetail!!.contourImagePath ?: "",
                autoSpotModelList = autoGeneratedSpots,
                manualSpots = manualSpots
            )
//            imageState.value = imageState.value.copy(
//                imagePath = imageDetail!!.contourImagePath ?: "",
//                changeTrigger = !imageState.value.changeTrigger // Trigger recomposition
//            )
            imageDetail?.let {
                imageState.value = imageState.value.copy(
                    imagePath = it.contourImagePath ?: "",
                    changeTrigger = !imageState.value.changeTrigger // Trigger recomposition
                )
            }

            SharedStates.updateManualContourEditState(false)

        }
    }


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
    LaunchedEffect(imageDetail, detectionType) {
        imageDetail?.let {
            println("Threshold Value Updated: ${it.thresholdVal}")
            thresholdVal = it.thresholdVal
            numberOfSpots = it.noOfSpots
            imagePath = it.contourImagePath ?: ""
            selectedRMSpot = it.rm
            selectedFinalSpot = it.finalSpot

            detectionType = it.detectionType
            if (detectionType == null) {
                detectionType = AppUtils.SPOT_DETECTION_TYPE
            }
            imageState.value = imageState.value.copy(
                imagePath = it.contourImagePath ?: "",
                description = "Updated Image",
                changeTrigger = !imageState.value.changeTrigger // Trigger recomposition
            )

            originalBitmap = imageDetail?.croppedImagePath?.let { path ->
                BitmapFactory.decodeFile(path)
            }


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
        AlertDialog(
            onDismissRequest = { showDialog = false },
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
            bandMarkedRegion = bandMarkedRegion,
            onNavigate = onNavigate,
            thresholdVal = thresholdVal,
            numberOfSpots = numberOfSpots,
            imagePath = imagePath,
            image = imageDetail!!,
            detectionType = detectionType.toString(),
            imageState = imageState.value,
            contourDataList = contourDataList.value,
            bandTempBitmap = bandTempBitmap,
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
            onIntensityPlot = onIntensityPlot,
            onManageSpot = {
                showManageSpotDialog = true
            },
            numberOfIntensityParts = numberOfIntensityParts ?: 100,
            bandContourDataList = bandContourDataList,
            imageAnalysisViewModel = imageAnalysisViewModel,
            projectViewModel = projectViewModel,
            selectUnselectBand = { newContourDataList ->

                println("selectUnselectBand: $newContourDataList")

                bandMarkedRegion =
                    PeakDetectionAlgorithms.contourDataToMarkedRegion(
                        newContourDataList,
                        numberOfIntensityParts ?: 100
                    )

                println("selectUnselectBand bandMarkedRegion: $bandMarkedRegion")

                bandContourDataList = newContourDataList

                originalBitmap?.let { bitmap ->
                    val modifiedBitmap =
                        drawHighlightedRegions(
                            bitmap,
                            bandMarkedRegion,
                            numberOfIntensityParts ?: 100
                        )
                    if (modifiedBitmap != null) {
                        println("modifiedBitmap: $modifiedBitmap")
                    }
                    imageState.value = imageState.value.copy(
                        imageBitmap = modifiedBitmap,
                        changeTrigger = !imageState.value.changeTrigger // Trigger recomposition
                    )

//                    bandTempBitmap = modifiedBitmap

                }

            },
            saveTheseBands = { newContourDataList ->

                if (originalBitmap != null) {

                    imageAnalysisViewModel.viewModelScope.launch(Dispatchers.IO) {

                        val newManualRectContourList = mutableListOf<ManualContourResult>()


                        newContourDataList.forEach { newContourData ->
                            val top =
                                ((1 - newContourData.rfBottom.toFloat()) * originalBitmap!!.height).toInt()
                            val bottom =
                                ((1 - newContourData.rfTop.toFloat()) * originalBitmap!!.height).toInt()

                            val newRect = Rect(
                                0,
                                top,
                                originalBitmap!!.width,
                                bottom
                            )

                            newManualRectContourList.add(
                                ManualContourResult(
                                    type = ContourType.RECTANGULAR,
                                    contourData = newContourData,
                                    rect = newRect
                                )
                            )
                        }

                        if (newManualRectContourList.isNotEmpty() && imageDetail != null) {
                            imageAnalysisViewModel.saveManualContourListToDatabase(
                                type = ContourType.RECTANGULAR,
                                imageDetail!!.imageId,
                                newManualRectContourList
                            )
                            contourDataSavedToDatabase = true
//            contourDataSavedToDatabase = true
                        } else {
                            Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                } else {
                    Toast.makeText(context, "Image is not valid", Toast.LENGTH_SHORT).show()
                }
            },
            startBandAnalysis = { startBandAnalysis, newThreshold, newLag, newInfluence ->
                if (startBandAnalysis) {

//                println("lineChartData: $lineChartData")

//                println("lag: $newLag, threshold: $newThreshold, influence: $newInfluence")

                    val normalizedThreshold =
                        newThreshold / 255.0  // Convert threshold from 0-255 to 0.0-1.0

                    val newMarkedRegions = PeakDetectionAlgorithms.detectPeaksRefined(
                        lineChartData,
                        lag = newLag,
                        threshold = normalizedThreshold,  // Use the normalized threshold
                        influence = newInfluence.toDouble()
                    )

//                println("Updated bandMarkedRegion: $newMarkedRegions")

                    bandMarkedRegion = newMarkedRegions // This ensures recomposition

                    println("startBandAnalysis bandMarkedRegion: $bandMarkedRegion")


                    bandContourDataList = calculateContourData(
                        bandMarkedRegion,
                        lineChartData,
                        numberOfIntensityParts ?: 100
                    )

                    if (originalBitmap != null) {
                        println("originalBitmap not null: $originalBitmap")
                    }

                    originalBitmap?.let { bitmap ->
                        val modifiedBitmap =
                            drawHighlightedRegions(
                                bitmap,
                                bandMarkedRegion,
                                numberOfIntensityParts ?: 100
                            )
                        if (modifiedBitmap != null) {
                            println("modifiedBitmap: $modifiedBitmap")
                        }
                        imageState.value = imageState.value.copy(
                            imageBitmap = modifiedBitmap,
                            changeTrigger = !imageState.value.changeTrigger // Trigger recomposition
                        )

//                    bandTempBitmap = modifiedBitmap

                    }
                } else {
                    imageState.value = imageState.value.copy(
                        imageBitmap = null,
                        changeTrigger = !imageState.value.changeTrigger // Trigger recomposition
                    )
                }
            }

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
                onEditClick = { contourData ->
                    if (contourData.type == ContourType.RECTANGULAR) {
                        if (imageDetail != null) {
                            imageAnalysisViewModel.viewModelScope.launch {
                                val manualContourDetail =
                                    imageAnalysisViewModel.getManualContourDetailByContourId(
                                        contourData.contourId
                                    )
                                if (manualContourDetail != null) {
                                    SharedData.editRectangleContourRect = Rect(
                                        manualContourDetail.roiLeft!!.toInt(),
                                        manualContourDetail.roiTop!!.toInt(),
                                        manualContourDetail.roiRight!!.toInt(),
                                        manualContourDetail.roiBottom!!.toInt()
                                    )
                                    val intent = Intent(context, EditRectangleContour::class.java)
                                    intent.putExtra("contourId", manualContourDetail.contourId)
                                    intent.putExtra(
                                        "manualContourId",
                                        manualContourDetail.manualContourId
                                    )
                                    intent.putExtra("spotName", contourData.name)
                                    intent.putExtra("mainImagePath", imageDetail!!.croppedImagePath)
                                    context.startActivity(intent)
                                }
                            }
                        }
                    }
                },
                onDeleteClick = { contourData ->
                    imageAnalysisViewModel.viewModelScope.launch {
                        // Delete the selected contour
                        imageAnalysisViewModel.deleteContour(contourData.contourId)

                        // Fetch updated contour data from the database
                        imageAnalysisViewModel.fetchAllSpotsFromDatabase(imageId = contourData.imageId)

                        imageAnalysisViewModel.plotContourOnImage(
                            imagePath = imageDetail!!.croppedImagePath,
                            contourImagePath = imageDetail?.contourImagePath ?: "",
                            autoSpotModelList = autoGeneratedSpots,
                            manualSpots = manualSpots
                        )
//                        imageState.value = imageState.value.copy(
//                            imagePath = imageDetail!!.contourImagePath ?: "",
//                            changeTrigger = !imageState.value.changeTrigger // Trigger recomposition
//                        )
                        imageDetail?.let {
                            imageState.value = imageState.value.copy(
                                imagePath = it.contourImagePath ?: "",
                                changeTrigger = !imageState.value.changeTrigger // Trigger recomposition
                            )
                        }
                    }
                }
            )
        }
        if (showManageSpotDialog) {

            ManageSpotDialog(
                contourDataList = contourDataList.value,
                selectedRMSpotId = selectedRMSpot,
                selectedFinalSpotId = selectedFinalSpot,
                onDismissRequest = { showManageSpotDialog = false },
                onSaveClick = { rmSpotId, finalSpotId ->
                    projectViewModel.viewModelScope.launch {
                        selectedRMSpot = rmSpotId
                        selectedFinalSpot = finalSpotId

                        if (imageDetail != null && selectedRMSpot != null && selectedFinalSpot != null) {

                            projectViewModel.updateImageDetailByImageId(
                                imageDetail!!.copy(
                                    rm = selectedRMSpot!!,
                                    finalSpot = selectedFinalSpot!!
                                )
                            )
                            showManageSpotDialog = false

                            println("Selected RM Spot: $selectedRMSpot")
                            println("Selected Final Spot: $selectedFinalSpot")

                        } else {
                            showManageSpotDialog = false
                            Toast.makeText(
                                context,
                                "Please select RM and Final Spot",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSpotDialog(
    contourDataList: List<ContourData>,
    selectedRMSpotId: String?,
    selectedFinalSpotId: String?,
    onDismissRequest: () -> Unit,
    onSaveClick: (String?, String?) -> Unit
) {
    var rmSelected by remember { mutableStateOf(selectedRMSpotId) }
    var finalSelected by remember { mutableStateOf(selectedFinalSpotId) }

    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = { Text(text = "Add Tag", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(text = "RM", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))

                DropdownMenuItemContent(
                    selectedItem = rmSelected,
                    items = contourDataList.map { it.contourId to it.name },
                    onItemSelected = { rmSelected = it }
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(text = "Final Product", style = MaterialTheme.typography.bodyLarge)
                Spacer(modifier = Modifier.height(4.dp))

                DropdownMenuItemContent(
                    selectedItem = finalSelected,
                    items = contourDataList.map { it.contourId to it.name },
                    onItemSelected = { finalSelected = it }
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        onSaveClick(
                            rmSelected.takeIf { !it.isNullOrEmpty() },
                            finalSelected.takeIf { !it.isNullOrEmpty() })
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A1445))
                ) {
                    Text(text = "Save", color = Color.White)
                }
            }
        },
        confirmButton = {}
    )
}

@Composable
fun DropdownMenuItemContent(
    selectedItem: String?,
    items: List<Pair<String, String>>, // List of contourId -> contourName
    onItemSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedText = items.find { it.first == selectedItem }?.second ?: "Select Spot"

    Box(modifier = Modifier.fillMaxWidth()) {
        TextButton(
            onClick = { expanded = true },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFDCD6FA), shape = RoundedCornerShape(8.dp))
        ) {
            Text(text = selectedText)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item.second) },
                    onClick = {
                        onItemSelected(item.first)
                        expanded = false
                    }
                )
            }
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


