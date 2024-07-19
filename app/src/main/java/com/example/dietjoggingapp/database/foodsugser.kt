package com.example.dietjoggingapp.database

import android.os.Parcelable
import com.example.dietjoggingapp.database.domains.FoodSuggest
import com.example.dietjoggingapp.database.domains.Ingredient
import com.example.dietjoggingapp.database.domains.Step
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.RawValue

@Parcelize
data class foodsugser(
    var id: String = "",
    var name: String = "",
    var desc: String = "",
    var tags: MutableList<String> = mutableListOf(),
    var steps: MutableList<Step> = mutableListOf(),
    var image: String = "",
    var ingredients: MutableList<Ingredients> = mutableListOf()
): Parcelable
