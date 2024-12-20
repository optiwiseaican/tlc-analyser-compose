package com.aican.tlcanalyzer.di

import android.content.Context
import androidx.room.Room
import com.aican.tlcanalyzer.data.database.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class DatabaseModule {


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "tlc_app_database"
        ).build()
    }

    @Provides
    fun provideProjectDetailsDao(database: AppDatabase) = database.projectDetailsDao()

    @Provides
    fun provideImageDao(database: AppDatabase) = database.imageDao()
}