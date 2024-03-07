package com.example.dietjoggingapp.ui.viewmodels

import androidx.lifecycle.*
import com.example.dietjoggingapp.database.Jogging
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.database.domains.ListFoodSuggest
import com.example.dietjoggingapp.other.SortType
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.repositories.JoggingRepo
import com.example.dietjoggingapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(var joggingRepo: JoggingRepo): ViewModel() {

    private val _getJogging = MutableLiveData<UiState<List<Jogging>>>()
    val getJoggings: LiveData<UiState<List<Jogging>>>
        get() = _getJogging
    fun getJogging(userId: String) {
        _getJogging.value = UiState.Loading
        joggingRepo.getJoggings(userId) {
            _getJogging.value = it
        }
    }

    private val _addJogging =MutableLiveData<UiState<Pair<Jogging, String>>>()
    val addJogging: LiveData<UiState<Pair<Jogging, String>>>
        get() = _addJogging
    fun addJogging(jogging: Jogging){
        _addJogging.value = UiState.Loading
        joggingRepo.addJogging(jogging){
            _addJogging.value = it
        }
    }




    fun sortJogging(sortType: SortType) {

    }

    fun insertJogging(jogging: Jogging) {

    }
}

