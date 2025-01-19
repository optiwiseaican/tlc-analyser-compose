package com.aican.tlcanalyzer.data.database.project.entities

import androidx.room.*

/**
 * Table for storing contour data, linked to an image.
 */
@Entity(
    tableName = "ContourData",
    foreignKeys = [
        ForeignKey(
            entity = Image::class,
            parentColumns = ["imageId"],
            childColumns = ["imageId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["imageId"])]
)
data class ContourData(
    @PrimaryKey val contourId: String, // Unique ID for the contour
    @ColumnInfo(name = "imageId") val imageId: String, // Foreign Key linking to Image
    val name: String,
    val area: String,
    val volume: String,
    val rf: String,
    val rfTop: String,
    val rfBottom: String,
    val cv: String,
    val chemicalName: String,
    val type: ContourType // Type of the contour (AUTO, RECTANGULAR, CIRCULAR)
)

/**
 * Enum to define types of contours.
 */
enum class ContourType {
    AUTO,
    RECTANGULAR,
    CIRCULAR
}

/**
 * Table for storing individual points of a contour.
 * Applies when the contour type is AUTO.
 */
@Entity(
    tableName = "ContourPoints",
    foreignKeys = [
        ForeignKey(
            entity = ContourData::class,
            parentColumns = ["contourId"],
            childColumns = ["contourId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["contourId"])]
)
data class ContourPoint(
    @PrimaryKey val contourPointId: String, // Unique ID for each point
    @ColumnInfo(name = "contourId") val contourId: String, // Links to the Contour
    val x: Float, // X-coordinate of the point
    val y: Float  // Y-coordinate of the point
)

/**
 * Table for manual contour details (RECTANGULAR or CIRCULAR).
 */
@Entity(
    tableName = "ManualContourDetails",
    foreignKeys = [
        ForeignKey(
            entity = ContourData::class,
            parentColumns = ["contourId"],
            childColumns = ["contourId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["contourId"])]
)
data class ManualContourDetails(
    @PrimaryKey val manualContourId: String, // Unique ID for manual contour details
    @ColumnInfo(name = "contourId") val contourId: String, // Links to the Contour
    val roiTop: Float? = null, // Top coordinate for the region of interest
    val roiBottom: Float? = null, // Bottom coordinate for the region of interest
    val roiLeft: Float? = null, // Left coordinate for the region of interest
    val roiRight: Float? = null // Right coordinate for the region of interest
)
