package com.example.dietjoggingapp.database

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.util.*

@Parcelize
data class CalDef(
    var email: String = "",
    var caldef: Float = 0.0f,
    var date: String = "${Date().year} ${Date().month} ${Date().date}"
): Parcelable
