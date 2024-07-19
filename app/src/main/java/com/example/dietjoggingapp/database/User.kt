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
    var dailyCalorie: Float = 0f,
    val weight: Float = 70.0f,
    val height: Float = 172.0f,
    val age: Float = 0.0f,
    var bmr: Float = 0.0f,
    var maxWeight: Float = 0.0f,
    var overweight: Float = 0.0f,
    var maxCalPerServe: Float = bmr / 3,
    var calDef: Float = 0.0f,
    val gender: String = "",
    val birthDate: Int = 0,
    val birthMonth: Int = 0,
    val birthYear: Int = 0
): Parcelable
