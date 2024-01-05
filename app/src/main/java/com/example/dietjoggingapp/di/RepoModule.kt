package com.example.dietjoggingapp.di

import android.content.SharedPreferences
import com.example.dietjoggingapp.repositories.AuthRepo
import com.example.dietjoggingapp.repositories.AuthRepoImp
import com.example.dietjoggingapp.repositories.JoggingRepo
import com.example.dietjoggingapp.repositories.JoggingRepoImp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object RepoModule {

    @Provides
    @Singleton
    fun provideJoggingRepo(database: FirebaseFirestore, storageReference: StorageReference): JoggingRepo = JoggingRepoImp(database, storageReference)

    @Provides
    @Singleton
    fun provideAuthRepo(
        auth: FirebaseAuth,
        database: FirebaseFirestore,
        sharedPreferences: SharedPreferences,
        gson: Gson
    ): AuthRepo {
        return AuthRepoImp(auth, database, sharedPreferences, gson)
    }
}