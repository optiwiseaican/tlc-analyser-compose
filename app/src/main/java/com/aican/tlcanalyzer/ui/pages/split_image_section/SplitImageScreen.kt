package com.aican.tlcanalyzer.ui.pages.split_image_section

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.utils.AppUtils
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SplitImageScreen(
    modifier: Modifier = Modifier,
    projectViewModel: ProjectViewModel = viewModel(),
    projectId: String,
    navController: NavHostController,
    onBackClick: () -> Unit,
    onAddMainImageClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onMainImageClick: (Image) -> Unit,
    onSplitImageClick: (Image) -> Unit

    ) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ✅ Load Main & Split Images from Database
    var mainImages by remember { mutableStateOf<List<Image>>(emptyList()) }
    var splitImages by remember { mutableStateOf<List<Image>>(emptyList()) }

    var projectName by remember { mutableStateOf("Not Defined") }

    LaunchedEffect(projectId) {
        coroutineScope.launch {
            mainImages = projectViewModel.getAllMainImageByProjectId(projectId)
            AppUtils.MAIN_IMAGE_COUNT = mainImages.size
            splitImages = projectViewModel.getAllSplitImageByProjectId(projectId)
        }
    }

    Column {
        // 🔹 Header Bar
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

        Spacer(modifier = Modifier.height(16.dp))

        ProjectDetailScreen(
            mainImages = mainImages,
            splitImages = splitImages,
            onMainImageClick = onMainImageClick,
            onSplitImageClick = onSplitImageClick,
            onAddMainImageClick = onAddMainImageClick
        )
    }
}

@Composable
fun ProjectDetailScreen(
    mainImages: List<Image>,
    splitImages: List<Image>,
    onAddMainImageClick: () -> Unit,
    onMainImageClick: (Image) -> Unit,
    onSplitImageClick: (Image) -> Unit
) {
    var selectedMainImage by remember { mutableStateOf<Image?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        // 🔹 Main Images Grid
        if (mainImages.isNotEmpty()) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(mainImages) { image ->
                    ImageItem(
                        image = image,
                        isSelected = selectedMainImage?.imageId == image.imageId,
                        onClick = {
                            selectedMainImage = image
                            onMainImageClick(image)
                        }
                    )
                }
            }
        } else {
            Text(text = "No Main Images Available", modifier = Modifier.padding(16.dp))
        }

        // 🔹 Add Main Image Button
        Button(
            onClick = onAddMainImageClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(text = "+ Add Main Images", fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // 🔹 Split Images List
        if (splitImages.isNotEmpty()) {
            LazyColumn {
                items(splitImages) { image ->
                    SplitImageItem(image, onSplitImageClick)
                }
            }
        } else {
            Text(text = "No Split Images Available", modifier = Modifier.padding(16.dp))
        }
    }
}

@Composable
fun ImageItem(image: Image, isSelected: Boolean, onClick: () -> Unit) {
    val imageFile = File(image.croppedImagePath)

    Box(
        modifier = Modifier
            .padding(8.dp)
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) Color.Green else Color.Gray,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
    ) {
        if (imageFile.exists()) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageFile)
                    .build(),
                contentDescription = "Main Image",
                contentScale = ContentScale.Inside,
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .padding(8.dp)
            )
        } else {
            Image(
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
                    .padding(8.dp),
                painter = painterResource(id = R.drawable.baseline_warning_24),
                contentDescription = "Missing Image"
            )
        }
        Text(
            text = image.name,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(Color.White.copy(alpha = 0.7f))
                .padding(4.dp),
            fontSize = 12.sp
        )
    }
}

@Composable
fun SplitImageItem(image: Image, onClick: (Image) -> Unit) {
    val imageFile = File(image.croppedImagePath)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { onClick(image) },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (imageFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageFile)
                        .build(),
                    contentDescription = "Split Image",
                    contentScale = ContentScale.Inside,
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
                Text(text = AppUtils.decodeTimestamp(image.timeStamp), fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}
