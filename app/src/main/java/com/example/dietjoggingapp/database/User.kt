package com.example.dietjoggingapp.database

import android.os.Parcelable
import com.google.firebase.firestore.ServerTimestamp
import kotlinx.android.parcel.Parcelize
import java.util.Date

@Parcelize
data class User(
    var userId:String = "",
    var fullName: String = "",
    val email: String = "",
    @ServerTimestamp
    val date: Date = Date(),
    var dailyCalorie: Float = 0f,
    var weight: Float = 70.0f,
    var height: Float = 172.0f,
    var age: Int = 0,
    var bmr: Float = 0.0f,
    var maxWeight: Float = 0.0f,
    var overweight: Float = 0.0f,
    var maxCalPerServe: Float = bmr / 3,
    var calDef: Float = 0.0f,
    var gender: String = "",
    var birthDate: Int = 0,
    var birthMonth: Int = 0,
    var birthYear: Int = 0,
    var bmi: Float = 0.0f
): Parcelable
