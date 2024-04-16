package com.example.dietjoggingapp.repositories

import com.example.dietjoggingapp.database.Food
import com.example.dietjoggingapp.other.UiState

interface FoodRepo {
    fun addFood(food: Food,result: (UiState<String>) -> Unit)

    fun getFood(userId: String, result: (UiState<MutableList<Food>>) -> Unit)

    fun editFood(FoodId: String, result: (UiState<String>) -> Unit)
}