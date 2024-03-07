package com.example.dietjoggingapp.ui.viewmodels

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dietjoggingapp.database.domains.FoodSuggest
import com.example.dietjoggingapp.database.domains.ListFoodSuggest
import com.example.dietjoggingapp.other.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FoodSuggestViewModel @Inject constructor(var foodSuggestRepo: com.example.dietjoggingapp.repositories.FoodSuggestRepo): ViewModel() {
    private val _getFoodSuggest = MutableLiveData<UiState<List<FoodSuggest>>>()
    val getFoodSuggest: LiveData<UiState<List<FoodSuggest>>>
        get() = _getFoodSuggest
    fun getFoodSuggest(context: Context, url: String, ) {
        _getFoodSuggest.value = UiState.Loading
        foodSuggestRepo.getFoodSuggestion(context, url) {
            _getFoodSuggest.value = it
        }
    }
}