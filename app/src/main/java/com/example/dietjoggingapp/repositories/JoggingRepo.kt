package com.example.dietjoggingapp.repositories

import android.net.Uri
import com.example.dietjoggingapp.database.Jogging
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.other.UiState
import dagger.Module


interface JoggingRepo {
    fun getJoggings(user: User?, result: (UiState<List<Jogging>>) -> Unit )
    fun addJogging(jogging: Jogging, result: (UiState<Pair<Jogging, String>>) -> Unit)
    fun updateJogging(jogging: Jogging, result: (UiState<String>) -> Unit)
    suspend fun uploadSingleFile(fileUri: Uri, onResult: (UiState<Uri>) -> Unit)
    suspend fun uploadMultipleFile(fileUri: List<Uri>, onResult: (UiState<List<Uri>>) -> Unit)
}