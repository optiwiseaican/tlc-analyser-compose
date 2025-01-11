package com.aican.tlcanalyzer.ui.pages.image_analysis.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.aican.tlcanalyzer.R
import java.io.File

@Composable
fun ZoomableImage1(imagePath: String, description: String, visibility: Boolean = true) {
    if (visibility) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp) // Adjust height for better zooming experience
                .background(Color.Gray)
        ) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageFile)
                        .build(),
                    contentDescription = description,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .rotate(90f)

                )
            } else {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = R.drawable.baseline_warning_24),
                    contentDescription = "Image Not Found"
                )
            }
        }
    }
}


@Composable
fun ZoomableImage(imagePath: String, description: String, visibility: Boolean = true) {
    if (visibility) {

        val scale = remember { mutableFloatStateOf(1f) }
        val rotationState = remember { mutableFloatStateOf(90f) } // Rotation in degrees

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp) // Set desired box height
                .background(Color.Black)
                .clip(RectangleShape) // Clip the box content
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, rotation ->
                        scale.value = maxOf(
                            0.5f,
                            minOf(3f, scale.value * zoom)
                        ) // Restrict zoom between 50% and 300%
                        rotationState.value += rotation
                    }
                }
        ) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageFile)
                        .build(),
                    modifier = Modifier
                        .fillMaxSize() // Ensure the image fills the Box initially
                        .graphicsLayer(
                            scaleX = scale.value, // Apply zoom scale
                            scaleY = scale.value,
                            rotationZ = rotationState.value, // Apply rotation
                            transformOrigin = TransformOrigin.Center // Rotate around the center
                        ),
                    contentDescription = description,
                )
            } else {
                Image(
                    modifier = Modifier.fillMaxSize(),
                    painter = painterResource(id = R.drawable.baseline_warning_24),
                    contentDescription = "Image Not Found"
                )
            }
        }
    }
}
