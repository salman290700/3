package com.example.dietjoggingapp.database

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ingredients (
    var name: String = "",
    var servingSizeInGram: Float = 0f,
        ): Parcelable