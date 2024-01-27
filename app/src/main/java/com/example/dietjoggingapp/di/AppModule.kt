package com.example.dietjoggingapp.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import com.example.dietjoggingapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.dietjoggingapp.other.Constants.KEY_NAME
import com.example.dietjoggingapp.other.Constants.KEY_WEIGHT
import com.example.dietjoggingapp.other.Constants.SHARED_PREFEREMCES_NAME
import com.example.dietjoggingapp.utility.Constants
import com.google.firebase.storage.StorageReference
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
object AppModule {
//    IsFirstLaunch
}