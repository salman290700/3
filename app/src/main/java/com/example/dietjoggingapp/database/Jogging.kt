package com.example.dietjoggingapp.database

import android.graphics.Bitmap
import android.os.Parcelable

import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.util.Date


@Parcelize
data class Jogging (
    var id: String = "",
    var userId: String = "",
    var img: Bitmap? = null,
    var timestamp: Long = 0L,
    var avgSpeedInKmh: Float = 0f,
    var distanceInMeters: Float = 0f,
    var timeInMillis: Long = 0L,
    var caloriesBurned: Float = 0f,
//    var date: Date = Date()
) : Parcelable