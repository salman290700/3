package com.example.dietjoggingapp.ui.viewmodels

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.dietjoggingapp.database.Jogging
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.repositories.JoggingRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


class AddJoggingViewModel (private val joggingRepo: JoggingRepo): ViewModel() {
    private val _addJogging = MutableLiveData<UiState<Pair<Jogging, String>>>()
    val addJogging: LiveData<UiState<Pair<Jogging, String>>>
    get() = _addJogging

    fun addJogging(jogging: Jogging){
        _addJogging.value = UiState.Loading
        joggingRepo.addJogging(jogging){
            _addJogging.value = it
        }
    }

    private val _text = MutableLiveData<String>().apply {
        value = "This is notifications Fragment"
    }
    val text: LiveData<String> = _text

    fun uploadSingleFile(fileUri: Uri, onResult: (UiState<Uri>) -> Unit){
        onResult.invoke(UiState.Loading)
        viewModelScope.launch {
            joggingRepo.uploadSingleFile(fileUri, onResult)
        }
    }
}