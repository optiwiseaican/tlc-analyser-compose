package com.aican.tlcanalyzer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.aican.tlcanalyzer.data.database.project.converters.EnumConverter
import com.aican.tlcanalyzer.data.database.project.dao.ContourDataDao
import com.aican.tlcanalyzer.data.database.project.dao.ContourPointDao
import com.aican.tlcanalyzer.data.database.project.dao.ContourSpecificDataDao
import com.aican.tlcanalyzer.data.database.project.dao.ImageDao
import com.aican.tlcanalyzer.data.database.project.dao.IntensityPlotDao
import com.aican.tlcanalyzer.data.database.project.dao.ManualContourDetailsDao
import com.aican.tlcanalyzer.data.database.project.dao.ProjectDetailsDao
import com.aican.tlcanalyzer.data.database.project.entities.ContourData
import com.aican.tlcanalyzer.data.database.project.entities.ContourPoint
import com.aican.tlcanalyzer.data.database.project.entities.ContourSpecificData
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.data.database.project.entities.IntensityPlotData
import com.aican.tlcanalyzer.data.database.project.entities.ManualContourDetails
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails

@Database(
    entities = [ProjectDetails::class,
        Image::class,
        ContourData::class,
        ContourPoint::class,
        ManualContourDetails::class,
        ContourSpecificData::class,
        IntensityPlotData::class
    ], version = 1, exportSchema = true
)
@TypeConverters(EnumConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDetailsDao(): ProjectDetailsDao
    abstract fun imageDao(): ImageDao
    abstract fun contourDataDao(): ContourDataDao
    abstract fun contourPointDao(): ContourPointDao
    abstract fun manualContourDetailsDao(): ManualContourDetailsDao
    abstract fun contourSpecificDataDao(): ContourSpecificDataDao
    abstract fun intensityPlotDao(): IntensityPlotDao

}
