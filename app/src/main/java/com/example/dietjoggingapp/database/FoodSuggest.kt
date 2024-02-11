package com.example.dietjoggingapp.database

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.util.*
@Parcelize
data class FoodSuggest(
    var id: String = "",
    var name: String = "",
    var Ingredients: List<Ingredients> = arrayListOf(),
    var timestamp: Long = 0L,
    var avgSpeedInKmh: Float = 0f,
    var distanceInMeters: Float = 0f,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Float = 0f,
    var date: Date = Date()
): Parcelable
