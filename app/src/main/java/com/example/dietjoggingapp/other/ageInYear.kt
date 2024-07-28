package com.example.dietjoggingapp.other

import android.util.Log
import java.time.LocalDate
import java.time.Period

class registerUtils {
    companion object {
        fun ageInYear(year: Int, month: Int, dayOfMonth: Int): Int {
            Log.d("TAG", "ageInYear: ${
                Period.between(
                    LocalDate.of(year, month, dayOfMonth),
                    LocalDate.now()
                ).years
            }")
        return Period.between(
            LocalDate.of(year, month, dayOfMonth),
            LocalDate.now()
        ).years
        }
    }
}