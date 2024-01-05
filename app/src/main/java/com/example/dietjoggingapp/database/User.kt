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
    val date: Date = Date()
): Parcelable
