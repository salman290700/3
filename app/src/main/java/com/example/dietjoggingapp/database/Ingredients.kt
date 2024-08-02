package com.example.dietjoggingapp.database

import android.os.Parcelable
import com.example.dietjoggingapp.database.domains.ServingSize
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Ingredients (
    var name: String = "",
    var servingSizeInGram: Float? = 0f,
    var servingZiseInTbsp : String? = "",
        ): Parcelable