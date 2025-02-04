package com.aican.tlcanalyzer.ui.pages.image_analysis.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.aican.tlcanalyzer.R
import kotlinx.coroutines.delay
import java.io.File

@Composable
fun ZoomableImage(
    imagePath: String,
    description: String,
    recomposeKey: Int, // Pass a unique key to force recomposition
    visibility: Boolean = true,
    zoomable: Boolean
) {
    if (visibility) {
        val scale = rememberSaveable { mutableFloatStateOf(1f) }
        val rotationState = rememberSaveable { mutableFloatStateOf(90f) }
        var isImageReady by rememberSaveable { mutableStateOf(false) }

        // Delay before loading the image
        LaunchedEffect(recomposeKey) {
            println("Delaying image loading for stabilization... : $isImageReady")
            isImageReady = false // Reset before applying the delay
            delay(500) // Add a delay of 1000ms (or adjust as needed)
            isImageReady = true // Set true after the delay
        }

        if (zoomable) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(modifier = Modifier
                    .height(500.dp)
                    .clip(RectangleShape)
                    .pointerInput(Unit) {
                        detectTransformGestures { _, _, zoom, rotation ->
                            scale.floatValue = maxOf(0.5f, minOf(3f, scale.floatValue * zoom))
                            rotationState.floatValue += rotation
                        }
                    }) {
                    if (isImageReady) {
                        val imageFile = File(imagePath)
                        val uniqueImagePath = "$imagePath?timestamp=${System.currentTimeMillis()}"
                        val bitmap = BitmapFactory.decodeFile(imageFile.path)

                        if (bitmap != null) {
                            println("Bitmap is not null for path: $uniqueImagePath")
                        }

                        if (imageFile.exists()) {
                            println("ZoomableImage recomposed with recomposeKey: $recomposeKey")
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uniqueImagePath.toUri()) // Use a unique key to bypass cache
                                    .memoryCacheKey(uniqueImagePath) // Optional custom cache key
                                    .diskCachePolicy(CachePolicy.DISABLED) // Disable disk cache
                                    .memoryCachePolicy(CachePolicy.DISABLED) // Disable memory cache
                                    .build(),
                                contentDescription = description,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale.floatValue,
                                        scaleY = scale.floatValue,
                                        rotationZ = rotationState.floatValue,
                                        transformOrigin = TransformOrigin.Center
                                    )
                            )
                        } else {
                            println("Image file does not exist: $imagePath")
                            Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = painterResource(id = R.drawable.baseline_warning_24),
                                contentDescription = "Image Not Found"
                            )
                        }
                    }
                    else {
                        // Placeholder while the image is being delayed
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
        else{


            Box(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                Box(modifier = Modifier
                    .height(500.dp)
                    .clip(RectangleShape)) {
                    if (isImageReady) {
                        val imageFile = File(imagePath)
                        val uniqueImagePath = "$imagePath?timestamp=${System.currentTimeMillis()}"
                        val bitmap = BitmapFactory.decodeFile(imageFile.path)

                        if (bitmap != null) {
                            println("Bitmap is not null for path: $uniqueImagePath")
                        }

                        if (imageFile.exists()) {
                            println("ZoomableImage recomposed with recomposeKey: $recomposeKey")
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(uniqueImagePath.toUri()) // Use a unique key to bypass cache
                                    .memoryCacheKey(uniqueImagePath) // Optional custom cache key
                                    .diskCachePolicy(CachePolicy.DISABLED) // Disable disk cache
                                    .memoryCachePolicy(CachePolicy.DISABLED) // Disable memory cache
                                    .build(),
                                contentDescription = description,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .graphicsLayer(
                                        scaleX = scale.floatValue,
                                        scaleY = scale.floatValue,
                                        rotationZ = rotationState.floatValue,
                                        transformOrigin = TransformOrigin.Center
                                    )

                            )
                        } else {
                            println("Image file does not exist: $imagePath")
                            Image(
                                modifier = Modifier.fillMaxSize(),
                                painter = painterResource(id = R.drawable.baseline_warning_24),
                                contentDescription = "Image Not Found"
                            )
                        }
                    }
                    else {
                        // Placeholder while the image is being delayed
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}


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
                    model = ImageRequest.Builder(LocalContext.current).data(imageFile).build(),
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
fun ZoomableImage2(imagePath: String, description: String, visibility: Boolean = true) {
    if (visibility) {

        val scale = remember { mutableFloatStateOf(1f) }
        val rotationState = remember { mutableFloatStateOf(90f) } // Rotation in degrees

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(500.dp) // Set desired box height
            .clip(RectangleShape) // Clip the box content
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoom, rotation ->
                    scale.floatValue = maxOf(
                        0.5f, minOf(3f, scale.floatValue * zoom)
                    ) // Restrict zoom between 50% and 300%
                    rotationState.floatValue += rotation
                }
            }) {
            val imageFile = File(imagePath)
            if (!imageFile.canRead()) {
                println("Image file is not readable: ${imageFile.path}")

            }

            if (imageFile.exists()) {
                println("File size: ${imageFile.length()} bytes")

                println("Image Path: " + imageFile.path)

                val bitmap1 = BitmapFactory.decodeFile(imageFile.path)
                if (bitmap1 == null) {
                    println("BitmapFactory failed to decode the file.")
                }
                Image(
                    rememberAsyncImagePainter(File(imageFile.path)),
                    contentDescription = "",
                    modifier = Modifier
                        .fillMaxSize() // Ensure the image fills the Box initially
                        .graphicsLayer(
                            scaleX = scale.floatValue, // Apply zoom scale
                            scaleY = scale.floatValue,
                            rotationZ = rotationState.floatValue, // Apply rotation
                            transformOrigin = TransformOrigin.Center // Rotate around the center
                        )
                )

                val bitmap = BitmapFactory.decodeFile(imageFile.path)?.asImageBitmap()

                if (bitmap != null) {
//                    Image(
//                        bitmap = bitmap,
//                        contentDescription = description,
//                        modifier = Modifier
//                            .fillMaxSize() // Ensure the image fills the Box initially
//                            .graphicsLayer(
//                                scaleX = scale.floatValue, // Apply zoom scale
//                                scaleY = scale.floatValue,
//                                rotationZ = rotationState.floatValue, // Apply rotation
//                                transformOrigin = TransformOrigin.Center // Rotate around the center
//                            )
//                    )


                } else {
                    Text(
                        text = "Image file not found",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red,
                        modifier = Modifier.fillMaxSize()
                    )
                }

//                AsyncImage(
//                    model = ImageRequest.Builder(LocalContext.current)
//                        .data(imageFile)
//                        .diskCachePolicy(coil3.request.CachePolicy.DISABLED) // Disable disk cache
//                        .memoryCachePolicy(coil3.request.CachePolicy.DISABLED) // Disable memory cache
//                        .build(),
//                    modifier = Modifier
//                        .fillMaxSize() // Ensure the image fills the Box initially
//                        .graphicsLayer(
//                            scaleX = scale.floatValue, // Apply zoom scale
//                            scaleY = scale.floatValue,
//                            rotationZ = rotationState.floatValue, // Apply rotation
//                            transformOrigin = TransformOrigin.Center // Rotate around the center
//                        ),
//                    contentDescription = description,
//                )
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
fun ZoomableImageWithTrigger(imagePath: String, recomposeTrigger: MutableState<Int>) {
    // Use the recomposition trigger as a key
    val key = "$imagePath-${recomposeTrigger.value}"

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current).data(File(imagePath))
            .diskCachePolicy(coil3.request.CachePolicy.DISABLED)
            .memoryCachePolicy(coil3.request.CachePolicy.DISABLED).build(),
        contentDescription = null,
        modifier = Modifier.fillMaxSize()
    )
}