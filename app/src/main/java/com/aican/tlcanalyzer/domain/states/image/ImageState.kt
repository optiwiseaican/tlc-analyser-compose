package com.aican.tlcanalyzer.domain.states.image

import android.graphics.Bitmap

data class ImageState(
    val imagePath: String = "",
    val description: String = "Main Image",
    val imageBitmap: Bitmap? = null,
    var changeTrigger: Boolean = false
)
