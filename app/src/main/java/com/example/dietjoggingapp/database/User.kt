package com.example.dietjoggingapp.database

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class User(
    var userId:String = "",
    val fullName: String = "",
    val email: String = "",
    @ServerTimestamp
    val date: Date = Date(),
    val dailyCalorie: Double = 0.0,
    val weight: Float = 70.0f,
    val height: Float = 172.0f,
    val age: Float = 0.0f,
    val bmr: Float = 0.0f,
    val gender: String = ""
): Parcelable
