package com.example.dietjoggingapp.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietjoggingapp.database.Jogging
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.repositories.JoggingRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


class ImageViewModel ( val repo: JoggingRepo): ViewModel() {

    private val _getJogging = MutableLiveData<UiState<List<Jogging>>>()
    val getJogging: LiveData<UiState<List<Jogging>>>
    get() = _getJogging

    fun getJoggings(userId: String) {
        _getJogging.value = UiState.Loading
        repo.getJoggings(userId) {
            _getJogging.value = it
        }
    }

    //Update Note
    private val _updateJogging = MutableLiveData<UiState<String>>()
    val updateJogging: LiveData<UiState<String>>
        get() = _updateJogging

    fun updateJogging(jogging: Jogging){
        _updateJogging.value = UiState.Loading
        repo.updateJogging(jogging){
            _updateJogging.value = it
        }
    }

    fun onUploadSingleFile(fileUris: Uri, onResult: (UiState<Uri>) -> Unit){
        onResult.invoke(UiState.Loading)
        viewModelScope.launch {
            repo
                .uploadSingleFile(fileUris, onResult)
        }
    }

    fun onUploadMultipleFiles(fileUris: List<Uri>, onResult: (UiState<List<Uri>>) -> Unit){
        onResult.invoke(UiState.Loading)
        viewModelScope.launch {
            repo.uploadMultipleFile(fileUris, onResult)
        }
    }
}