package com.aican.tlcanalyzer.data.database.project.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.aican.tlcanalyzer.data.database.project.converters.EnumConverter

@Entity(
    tableName = "Image",
    foreignKeys = [ForeignKey(
        entity = ProjectDetails::class,
        parentColumns = arrayOf("projectId"),
        childColumns = arrayOf("projectId"),
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["projectId"])]
)
data class Image(
    @PrimaryKey val imageId: String,
    val name: String,
    val originalImagePath: String,
    val croppedImagePath: String,
    val contourImagePath: String,
    val timeStamp: String,
    val thresholdVal: Int = 100,
    val noOfSpots: Int = 2,
    val description: String,
    @ColumnInfo(name = "projectId") val projectId: String,
    val imageType: ImageType,
    val parentImageId: String? = null, // Null for MAIN images
    val rm: String? = null,
    val finalSpot: String? = null,
    val hour: String? = null,
    val detectionType: String? = null
)


enum class ImageType {
    MAIN,
    SPLIT
}
