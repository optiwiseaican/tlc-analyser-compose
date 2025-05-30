package com.aican.tlcanalyzer.ui.pages.split_image_section

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import com.aican.tlcanalyzer.data.database.project.entities.Image
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.domain.model.multiple_analysis.SelectedImage
import com.aican.tlcanalyzer.utils.AppUtils
import com.aican.tlcanalyzer.viewmodel.project.MultipleImageAnalysisViewModel
import kotlinx.coroutines.launch
import java.io.File


@Composable
fun SplitImageScreen(
    modifier: Modifier = Modifier,
    projectViewModel: ProjectViewModel = viewModel(),
    multipleImageAnalysisViewModel: MultipleImageAnalysisViewModel,
    projectId: String,
    projectName: String,
    navController: NavHostController,
    onBackClick: () -> Unit,
    onAddMainImageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMainImageClick: (Image) -> Unit,
    onSplitImageClick: (Image) -> Unit,
    onMultipleImageAnalysisClick: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var mainImages by remember { mutableStateOf<List<Image>>(emptyList()) }
    var splitImages by remember { mutableStateOf<List<Image>>(emptyList()) }
    val selectedImages by multipleImageAnalysisViewModel.selectedImages.collectAsState()
    var isSelectionMode by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(projectId) {
        coroutineScope.launch {
            mainImages = projectViewModel.getAllMainImageByProjectId(projectId)
            splitImages = projectViewModel.getAllSplitImageByProjectId(projectId)
        }
    }

    BackHandler(enabled = isSelectionMode) {
        if (selectedImages.isNotEmpty()) {
            multipleImageAnalysisViewModel.clearSelection()
            isSelectionMode = false
        } else {
            onBackClick()
        }
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = projectName,
                    fontSize = MaterialTheme.typography.titleLarge.fontSize,
                    fontWeight = MaterialTheme.typography.titleLarge.fontWeight
                )
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
            }
        },
        floatingActionButton = {
            if (isSelectionMode) {
                ExtendedFloatingActionButton(
                    containerColor = AppUtils.UiColor1,
                    text = { Text("Analyse", color = Color.White) },
                    icon = {
                        Icon(Icons.Filled.Check, contentDescription = "", tint = Color.White)
                    },
                    onClick = {
                        multipleImageAnalysisViewModel.viewModelScope.launch {
                            val validSelections = selectedImages.filter {
                                multipleImageAnalysisViewModel.doesIntensityPlotExist(it.imageId)
                            }

                            if (validSelections.isEmpty()) {
                                Toast.makeText(
                                    context,
                                    "No valid selections with existing intensity plots",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                multipleImageAnalysisViewModel.fetchImageAnalysisData(
                                    validSelections
                                )
                                onMultipleImageAnalysisClick.invoke()
                            }
                        }
                    }
                )
            }
        }
    ) { paddingValues ->
        ProjectDetailScreen(
            modifier = Modifier.padding(paddingValues),
            mainImages = mainImages,
            splitImages = splitImages,
            selectedImages = selectedImages,
            isSelectionMode = isSelectionMode,
            multipleImageAnalysisViewModel = multipleImageAnalysisViewModel,
            onImageSelected = { image, isSelected ->
                multipleImageAnalysisViewModel.selectImage(
                    SelectedImage(
                        image.imageId, image.name, image.hour, image.rm, image.finalSpot,
                        imageDetail = image
                    ),
                    isSelected
                )
                isSelectionMode = selectedImages.isNotEmpty()
            },
            onLongPress = { image ->
                multipleImageAnalysisViewModel.selectImage(
                    SelectedImage(
                        image.imageId, image.name, image.hour, image.rm, image.finalSpot,
                        imageDetail = image
                    ),
                    true
                )
                isSelectionMode = true
            },
            onClearSelection = {
                multipleImageAnalysisViewModel.clearSelection()
                isSelectionMode = false
            },
            onAddMainImageClick = onAddMainImageClick,
            onMainImageClick = onMainImageClick,
            projectViewModel = projectViewModel,
            onSplitImageClick = onSplitImageClick
        )
    }
}


@Composable
fun ProjectDetailScreen(
    modifier: Modifier = Modifier,
    multipleImageAnalysisViewModel: MultipleImageAnalysisViewModel,
    projectViewModel: ProjectViewModel,
    mainImages: List<Image>,
    splitImages: List<Image>,
    selectedImages: Set<SelectedImage>,
    isSelectionMode: Boolean,
    onImageSelected: (Image, Boolean) -> Unit,
    onLongPress: (Image) -> Unit,
    onClearSelection: () -> Unit,
    onAddMainImageClick: () -> Unit,
    onMainImageClick: (Image) -> Unit,
    onSplitImageClick: (Image) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (mainImages.isNotEmpty()) {
            Text(
                text = "Main Images",
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(mainImages) { image ->
                    MainImageItem(
                        image = image,
                        isSelected = selectedImages.contains(
                            SelectedImage(
                                imageId = image.imageId,
                                imageName = image.name,
                                hour = image.hour,
                                rm = image.rm,
                                final = image.finalSpot,
                                imageDetail = image
                            )
                        ),
                        multipleImageAnalysisViewModel = multipleImageAnalysisViewModel,
                        isSelectionMode = isSelectionMode,
                        onClick = { onMainImageClick(image) },
                        onLongClick = { onLongPress(image) },
                        onCheckboxClick = { isSelected -> onImageSelected(image, isSelected) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (splitImages.isNotEmpty()) {
            Text(
                text = "Split Images",
                fontSize = MaterialTheme.typography.titleMedium.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn {
                items(splitImages) { image ->
                    SplitImageItem(
                        image = image,
                        isSelected = selectedImages.contains(
                            SelectedImage(
                                imageId = image.imageId,
                                imageName = image.name,
                                hour = image.hour,
                                rm = image.rm,
                                final = image.finalSpot,
                                imageDetail = image

                            )
                        ),
                        multipleImageAnalysisViewModel = multipleImageAnalysisViewModel,
                        projectViewModel = projectViewModel,
                        isSelectionMode = isSelectionMode,
                        onClick = { onSplitImageClick(image) },
                        onLongClick = { onLongPress(image) },
                        onCheckboxClick = { isSelected -> onImageSelected(image, isSelected) }
                    )
                }
                item { Spacer(modifier = Modifier.height(40.dp)) }
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MainImageItem(
    image: Image,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    multipleImageAnalysisViewModel: MultipleImageAnalysisViewModel,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onCheckboxClick: (Boolean) -> Unit
) {
    val imageFile = File(image.croppedImagePath)
    val context = LocalContext.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(8.dp)
            .clickable { if (isSelectionMode) onCheckboxClick(!isSelected) else onClick() }
    ) {
        Box(
            modifier = Modifier
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) Color.Blue else Color.Gray,
                    shape = RoundedCornerShape(8.dp)
                )
                .combinedClickable(
                    onClick = {

                        if (isSelectionMode) {
                            multipleImageAnalysisViewModel.viewModelScope.launch {
                                if (multipleImageAnalysisViewModel.doesIntensityPlotExist(image.imageId)) {
                                    onCheckboxClick(!isSelected)
                                } else {
                                    Toast
                                        .makeText(
                                            context,
                                            "Intensity Plot Not Exist for ${image.name}",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
                                }
                            }
                        } else {
                            onClick()
                        }


                    },
                    onLongClick = {
                        multipleImageAnalysisViewModel.viewModelScope.launch {
                            if (multipleImageAnalysisViewModel.doesIntensityPlotExist(image.imageId)) {
                                onLongClick()
                            } else {
                                Toast
                                    .makeText(
                                        context,
                                        "Intensity Plot Not Exist for ${image.name}",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        }
                    }
                )
        ) {
            if (imageFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageFile)
                        .build(),
                    contentDescription = "Main Image",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp)
                )
            } else {
                Image(
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp),
                    painter = painterResource(id = R.drawable.baseline_warning_24),
                    contentDescription = "Missing Image"
                )
            }

            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onCheckboxClick(it) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                )
            }
        }

        Text(
            text = image.name,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SplitImageItem(
    image: Image,
    isSelected: Boolean,
    isSelectionMode: Boolean,
    multipleImageAnalysisViewModel: MultipleImageAnalysisViewModel,
    projectViewModel: ProjectViewModel,
    onClick: (Image) -> Unit,
    onLongClick: () -> Unit,
    onCheckboxClick: (Boolean) -> Unit
) {
    val imageFile = File(image.croppedImagePath)
    val context = LocalContext.current

    // ✅ Handle Null Case: If `image.hour` is null, show "Select Hour"
    var selectedHour by rememberSaveable {
        mutableIntStateOf(image.hour?.toIntOrNull() ?: -1) // -1 means "Select Hour"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) {
                        multipleImageAnalysisViewModel.viewModelScope.launch {
                            if (multipleImageAnalysisViewModel.doesIntensityPlotExist(image.imageId)) {
                                onCheckboxClick(!isSelected)
                            } else {
                                Toast
                                    .makeText(
                                        context,
                                        "Intensity Plot Not Exist for ${image.name}",
                                        Toast.LENGTH_SHORT
                                    )
                                    .show()
                            }
                        }
                    } else {
                        onClick(image)
                    }
                },
                onLongClick = {
                    multipleImageAnalysisViewModel.viewModelScope.launch {
                        if (multipleImageAnalysisViewModel.doesIntensityPlotExist(image.imageId)) {
                            onLongClick()
                        } else {
                            Toast
                                .makeText(
                                    context,
                                    "Intensity Plot Not Exist for ${image.name}",
                                    Toast.LENGTH_SHORT
                                )
                                .show()
                        }
                    }
                }
            ),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color.Blue.copy(alpha = 0.3f) else Color.White
        ),
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 🔹 Image Preview
                if (imageFile.exists()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(imageFile)
                            .build(),
                        contentDescription = "Split Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(50.dp)
                            .padding(8.dp)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.baseline_warning_24),
                        contentDescription = "Missing Image",
                        modifier = Modifier
                            .size(50.dp)
                            .padding(8.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(text = image.name, fontWeight = FontWeight.Bold)
                    Text(
                        text = AppUtils.decodeTimestamp(image.timeStamp),
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Show Checkbox **ONLY when selection mode is enabled**
                if (isSelectionMode) {
                    Checkbox(
                        checked = isSelected,
                        onCheckedChange = { onCheckboxClick(it) },
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            // 🔹 Custom Dropdown Below
            Spacer(modifier = Modifier.height(8.dp))

            HourDropdown(
                selectedHour = selectedHour,
                onHourSelected = { newHour ->
                    if (newHour != selectedHour) { // ✅ Prevent unnecessary database updates
                        selectedHour = newHour
                        projectViewModel.viewModelScope.launch {
                            projectViewModel.updateImageDetailByImageId(
                                image.copy(hour = newHour.toString())
                            )
                        }
                    }
                }
            )
        }
    }
}


@Composable
fun HourDropdown(
    selectedHour: Int,
    onHourSelected: (Int) -> Unit
) {
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val hoursList = (-1..24).map { // ✅ Add "-1" for "Select Hour"
        it to if (it == -1) "Select Hour" else "$it Hour${if (it > 1) "s" else ""}"
    }

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            // 🔹 TextField Styled as Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFDCD6FA), shape = RoundedCornerShape(8.dp))
                    .clickable { isDropdownExpanded = !isDropdownExpanded }
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = hoursList.find { it.first == selectedHour }?.second ?: "Select Hour",
                        color = Color.Black
                    )
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown Arrow"
                    )
                }
            }

            // 🔹 Custom Dropdown List
            AnimatedVisibility(visible = isDropdownExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White, shape = RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        .padding(4.dp)
                ) {
                    hoursList.forEach { (value, label) ->
                        Text(
                            text = label,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onHourSelected(value)
                                    isDropdownExpanded = false
                                }
                                .padding(12.dp),
                            color = Color.Black
                        )
                        Divider(color = Color.Gray.copy(alpha = 0.3f))
                    }
                }
            }
        }
    }
}
