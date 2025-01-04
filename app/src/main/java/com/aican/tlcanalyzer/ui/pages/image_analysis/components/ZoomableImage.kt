package com.aican.tlcanalyzer.ui.pages.image_analysis.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import com.aican.tlcanalyzer.R
import java.io.File

@Composable
fun ZoomableImage(imagePath: String, description: String, visibility: Boolean = true) {
    if (visibility) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp) // Adjust height for better zooming experience
                .background(Color.Gray)
        ) {
            val imageFile = File(imagePath)
            if (imageFile.exists()) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageFile)
                        .build(),
                    contentDescription = description,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
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
