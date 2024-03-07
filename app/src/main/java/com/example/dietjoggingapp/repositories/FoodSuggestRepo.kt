package com.example.dietjoggingapp.repositories

import android.content.Context
import com.example.dietjoggingapp.database.domains.FoodSuggest
import com.example.dietjoggingapp.database.domains.ListFoodSuggest
import com.example.dietjoggingapp.other.UiState

interface FoodSuggestRepo {
    fun getFoodSuggestion(context: Context, url: String, result: (UiState<List<FoodSuggest>>) -> Unit)
}