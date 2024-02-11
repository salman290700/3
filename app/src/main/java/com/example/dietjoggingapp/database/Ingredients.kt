package com.example.dietjoggingapp.database

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ingredients (
    var position: Int = 0,
    var servingSizeInGram: Float = 0f,
        ): Parcelable