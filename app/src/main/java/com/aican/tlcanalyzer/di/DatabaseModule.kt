package com.aican.tlcanalyzer.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.aican.tlcanalyzer.data.database.AppDatabase
import com.aican.tlcanalyzer.data.database.project.entities.ProjectDetails
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executors
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration queries here if needed
                // e.g., database.execSQL("ALTER TABLE ContourData ADD COLUMN createdBy TEXT DEFAULT 'Unknown'")
            }
        }

        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tlc_app_database"
        )
            // Uncomment the below line if you want to enable migrations
            // .addMigrations(MIGRATION_1_2)
            .addCallback(
                object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Insert default data into the database
//                        Executors.newSingleThreadExecutor().execute {
//                            val defaultData = """
//                                INSERT INTO ProjectDetails (projectId, projectName, projectDescription, timeStamp, projectNumber)
//                                VALUES ('1', 'Test Project', 'Description', 'timestamp', '1');
//                            """
//                            db.execSQL(defaultData)
//                        }
                    }
                }
            )
            // .fallbackToDestructiveMigration() // Uncomment for development to recreate the DB when schema changes
            .build()
    }

    @Provides
    fun provideProjectDetailsDao(database: AppDatabase) = database.projectDetailsDao()

    @Provides
    fun provideImageDao(database: AppDatabase) = database.imageDao()

    @Provides
    fun provideContourDataDao(database: AppDatabase) = database.contourDataDao()

    @Provides
    fun provideContourPointDao(database: AppDatabase) = database.contourPointDao()

    @Provides
    fun provideManualContourDetailsDao(database: AppDatabase) = database.manualContourDetailsDao()

    @Provides
    fun provideContourSpecificDataDao(database: AppDatabase) = database.contourSpecificDataDao()

    @Provides
    fun provideIntensityPlotDataDao(database: AppDatabase) = database.intensityPlotDao()
}
