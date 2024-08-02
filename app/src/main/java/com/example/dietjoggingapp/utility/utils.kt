package com.example.dietjoggingapp.utility

import com.google.firebase.auth.FirebaseAuth
import kotlin.math.roundToInt

class utils {
    val email = FirebaseAuth.getInstance().currentUser?.email.toString()
    companion object{
        fun countDietDays(CalDef: Float, maxWeight: Float, weight: Float, bmr: Float): Int {
            var DT = 0.0f;
            var ow = weight - maxWeight
            DT = (ow / 7700) / (bmr - CalDef)
            return DT.roundToInt()
        }
    }
}