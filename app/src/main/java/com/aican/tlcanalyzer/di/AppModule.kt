package com.aican.tlcanalyzer.di

import com.aican.tlcanalyzer.data.repository.auth.AuthRepository
import com.aican.tlcanalyzer.data.repository.auth.AuthRepositoryImplementation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()

    @Singleton
    @Provides
    fun provideFirebaseDatabaseReference(): DatabaseReference =
        FirebaseDatabase.getInstance().reference

    @Singleton
    @Provides
    fun provideAuthRepository(
        firebaseAuth: FirebaseAuth,
        databaseReference: DatabaseReference
    ): AuthRepository =
        AuthRepositoryImplementation(firebaseAuth, databaseReference)


}