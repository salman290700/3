package com.example.dietjoggingapp.database.domains

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Step(
    var stepDesc: String = ""
): Parcelable
