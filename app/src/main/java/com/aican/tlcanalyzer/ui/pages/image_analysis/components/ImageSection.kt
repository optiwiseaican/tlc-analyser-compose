package com.aican.tlcanalyzer.ui.pages.image_analysis.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.domain.states.image.ImageState
import java.io.File


@Composable
fun ImageSection(imageState: ImageState, zoomable: Boolean = false) {
    LaunchedEffect(imageState.changeTrigger) {
        println("ImageSection recomposed with path: ${imageState.imagePath}")
    }

    if (imageState.imagePath.isNotEmpty()) {
        val imageFile = File(imageState.imagePath)
        if (imageFile.exists()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {

                ZoomableImage(
                    imagePath = imageState.imagePath,
                    description = imageState.description,
                    imageBitmap = imageState.imageBitmap,
                    recomposeKey = imageState.changeTrigger.hashCode(), // Use trigger to recompose
                    zoomable = zoomable
                )
            }
        } else {
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
                    description = "Main Image", recomposeKey = recomposeTrigger.intValue,
                    zoomable = false
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