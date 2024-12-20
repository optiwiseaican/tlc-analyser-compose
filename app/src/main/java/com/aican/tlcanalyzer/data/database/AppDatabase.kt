package com.aican.tlcanalyzer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.aican.tlcanalyzer.data.database.project.dao.ImageDao
import com.aican.tlcanalyzer.data.database.project.dao.ProjectDetailsDao
import com.aican.tlcanalyzer.data.database.project.entities.Image
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails

@Database(entities = [ProjectDetails::class, Image::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun projectDetailsDao(): ProjectDetailsDao
    abstract fun imageDao(): ImageDao
}
