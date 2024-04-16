package com.example.dietjoggingapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dietjoggingapp.database.Food
import com.example.dietjoggingapp.database.Jogging
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.repositories.FoodRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class FoodViewModel @Inject constructor(var foodRepo: FoodRepo): ViewModel()  {
    private val _getFood = MutableLiveData<UiState<MutableList<Food>>>()
    val getFoods: LiveData<UiState<MutableList<Food>>>
        get() = _getFood
    fun getFood(userId: String) {
        _getFood.value = UiState.Loading
        foodRepo.getFood(userId) {
            _getFood.value = it
        }
    }

    private val _addFood = MutableLiveData<UiState<String>>()
    val addFood: LiveData<UiState<String>>
        get() = _addFood
    fun addFood(food: Food){
        _addFood.value = UiState.Loading
        foodRepo.addFood(food){
            _addFood.value = it
        }
    }


}