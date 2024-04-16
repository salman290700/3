package com.example.dietjoggingapp.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class Food(
    var id: String = "",
    val foodName: String,
    val foodDescription: String?,
    var foodCalorie: Float = 0f,
    val userId: String,
    val date: Date = Date()
): Parcelable
