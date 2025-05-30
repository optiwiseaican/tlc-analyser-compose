package com.aican.tlcanalyzer.domain.model.multiple_analysis

import kotlinx.parcelize.Parcelize
import android.os.Parcelable
import com.aican.tlcanalyzer.data.database.project.entities.Image

data class SelectedImage(
    val imageId: String,
    val imageName: String,
    val hour: String? = null,
    val rm: String? = null,
    val final: String? = null,
    val imageDetail: Image
)