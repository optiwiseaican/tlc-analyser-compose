package com.aican.tlcanalyzer.ui.pages.image_analysis

import android.content.res.Configuration
import android.provider.CalendarContract.Colors
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.aican.tlcanalyzer.R
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.ActionButton
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.TopPanel
import com.aican.tlcanalyzer.ui.pages.image_analysis.components.ZoomableImage
import com.aican.tlcanalyzer.viewmodel.project.ProjectViewModel
import java.io.File

@Composable
fun AnalysisScreen(
    modifier: Modifier = Modifier,
    projectViewModel: ProjectViewModel,
    projectId: String
) {
    val project by projectViewModel.observerProjectDetails(projectId).collectAsState(initial = null)
    val imageDetails by projectViewModel.observerProjectImages(projectId)
        .collectAsState(initial = emptyList())

    if (project == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Loading...", style = MaterialTheme.typography.bodyMedium)
        }
    } else {
        Column {
            TopPanel(title = project?.projectName ?: "Unknown Project")


            if (imageDetails.isNotEmpty()) {
                ZoomableImage(
                    imagePath = imageDetails[0].contourImagePath,
                    description = "Main Image"
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No image available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red
                    )
                }
            }

            SpotDetectionUI()

        }
    }
}

@Preview(showBackground = true)
@Composable
fun SpotDetectionUI() {
    var threshold by remember { mutableFloatStateOf(100f) }
    var numberOfSpots by remember { mutableFloatStateOf(1f) }

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
            value = threshold,
            onValueChange = { threshold = it },
            max = 255f
        )

        SpotSlider(
            label = "No of Spots",
            value = numberOfSpots,
            onValueChange = { numberOfSpots = it },
            max = 100f
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ActionButton(text = "Generate Spots", onClick = { /* Handle Generate Spots */ })
            ActionButton(text = "Add Spot", onClick = { /* Handle Add Spot */ })
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


