package com.aican.tlcanalyzer.domain.states.image

data class ImageState(
    val imagePath: String = "",
    val description: String = "Main Image",
    var changeTrigger: Boolean = false
)
