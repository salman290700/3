package com.example.dietjoggingapp.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Jogging_Suggestion(
    var id: String = "",
    var user_id: String = "",
    var kalori: Float = 0f
): Parcelable