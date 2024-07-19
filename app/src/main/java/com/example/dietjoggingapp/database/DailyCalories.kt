package com.example.dietjoggingapp.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class DailyCalories(
    var id: String = "",
    var calorie: Float = 0f,
    var user_id: String = "",
    var date: Int = Date().date
):Parcelable
