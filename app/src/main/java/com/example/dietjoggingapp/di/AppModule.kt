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
    @Singleton
    @Provides
    fun provideSharedPref(@ApplicationContext app: Context) =
        app.getSharedPreferences(SHARED_PREFEREMCES_NAME, MODE_PRIVATE)

    @Singleton
    @Provides
    fun provideName(sharedPref: SharedPreferences) = sharedPref.getString(KEY_NAME, "") ?: ""

    @Singleton
    @Provides
    fun provideWeight(sharedPref: SharedPreferences) = sharedPref.getFloat(KEY_WEIGHT, 80f)

    @Singleton
    @Provides
    fun provideFirstLaunch(sharedPref: SharedPreferences) = sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, true)


    @Provides
    @Singleton
    fun provideGson(): Gson {
        return Gson()
    }

}