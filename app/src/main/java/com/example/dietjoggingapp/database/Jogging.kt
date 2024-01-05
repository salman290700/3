package com.example.dietjoggingapp.database

import android.graphics.Bitmap
import android.os.Parcelable

import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.util.Date


@Parcelize
data class Jogging (
    var id: String = "",
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeedInKmh: Float = 0f,
    var distanceInMeters: Int = 0,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Int = 0,
//    var date: Date = Date()
) : Parcelable