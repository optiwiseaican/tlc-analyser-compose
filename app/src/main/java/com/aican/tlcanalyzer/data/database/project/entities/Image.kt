package com.aican.tlcanalyzer.data.database.project.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "Image", foreignKeys = [ForeignKey(
        entity = ProjectDetails::class,
        parentColumns = arrayOf("id"),
        childColumns = arrayOf("projectId"),
        onDelete = ForeignKey.CASCADE
    )]
)
data class Image(
    @PrimaryKey val id: String,
    val name: String,
    val path: String,
    val timeStamp: String,
    val thresholdVal: String,
    val noOfSpots: String,
    val description: String,
    @ColumnInfo(name = "projectId") val projectId: String,
    val imageType: ImageType
)

enum class ImageType {
    MAIN,
    SPLIT
}
