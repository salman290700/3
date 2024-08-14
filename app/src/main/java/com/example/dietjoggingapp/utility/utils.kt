package com.example.dietjoggingapp.utility

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate
import java.time.Period
import kotlin.math.roundToInt

class utils {
    val email = FirebaseAuth.getInstance().currentUser?.email.toString()
    companion object{
        fun countDietDays(bmi: Float, CalDef: Float, maxWeight: Float, weight: Float, calDefFood: Float, height: Float, bmr: Float): Int {
            var DT = 0.0f;
            var ow = weight - maxWeight
            val maxWeight = maxWeight(height)
            val minWeight = minWeight(height)
            if(bmi >= 18.5 && bmi < 25) {
                DT = 0F
                return DT.roundToInt()
            }else if(bmi >= 25) {
                if(calDefFood == 0.0f && CalDef == 0.0f) {
                    var bmrPlus = 7700 / 4 /7
                    DT = (ow * 7700) / bmrPlus
                    Log.d("TAG", "countDietDays: ${DT}")
                    return DT.roundToInt()
                }else {
                    var bmrPlus = 7700 / 4 /7
                    DT = (ow * 7700) / (CalDef + bmrPlus)
                    Log.d("TAG", "countDietDays: ${DT}")
                    return DT.roundToInt()
                }
            }else {
                if(calDefFood == 0.0f && CalDef == 0.0f) {
                    var bmrPlus = 7700 / 2 / 7
                    DT = ((minWeight - weight) * 7700) / bmrPlus
                    Log.d("TAG", "countDietDays: ${DT}")
                    return DT.roundToInt()
                }else {
                    var bmrPlus = 7700 / 2 /7
                    DT = ((minWeight - weight)* 7700) / (CalDef + bmrPlus)
                    Log.d("TAG", "countDietDays: ${DT}")
                    return DT.roundToInt()
                }
            }
//            if(weight >= minWeight) {
//                if (ow > 0) {
//                    if(calDefFood == 0.0f && CalDef == 0.0f) {
//                        var bmrPlus = 7700 / 4 /7
//                        DT = (ow * 7700) / bmrPlus
//                        return DT.roundToInt()
//                    }else {
//                        var bmrPlus = 7700 / 4 /7
//                        DT = (ow * 7700) / (CalDef + bmrPlus)
//                        return DT.roundToInt()
//                    }
//                }else {
//                    DT = 0.0f
//                    return DT.roundToInt()
//                }
//            } else {
//                var bmrPlus = 7700 / 2 /7
//                DT = ((minWeight - weight) * 7700) / bmrPlus
//                return DT.roundToInt()
//            }
        }

        fun calorieTarget (bmr: Float, calDefFood: Float, calDef: Float?): Int {
            var calorieTarget = 0.0f;
            if (calDefFood!! < bmr){
                calorieTarget = bmr - calDefFood
                return calorieTarget.roundToInt()
            }else {
                return 0;
            }
        }

        fun ow(height: Float, weight: Float): Float {
            val ow = weight - maxWeight(height)
            return ow
        }

        fun minWeight(heightInM: Float): Float {
            val minWeight = 18 * (heightInM * heightInM)
            Log.d("TAG", "minWeight: $minWeight")
            return minWeight
        }

        fun maxWeight(heightInM: Float): Float {
            val maxWeight = 25* (heightInM * heightInM)
            return maxWeight.toFloat()
        }

        fun bmr(gender: String, weight: Float, height: Float, age: Int, bmi: Float):Float {
            Log.d("TAG", "bmr: ${height}height")
            Log.d("TAG", "bmr: ${weight}weight")
            Log.d("TAG", "bmr: ${bmi}bmi")
            var bmr = 0.0f
            if(gender == "male") {
                if (bmi > 25) {
                    bmr = 66 + 13.7f * (weight) + 5.0f * (height) - 6.78f * age.toFloat()
                    var dietQuarterKg = 7700 / (7 * 4)
                    bmr = bmr - dietQuarterKg
                    Log.d("TAG", "bmr: ${bmr}")
                    Log.d("TAG", "bmr: ${bmr} bmi ${bmi}")
                    return bmr
                }else if (bmi >= 18 && bmr <= 25 ){
                    bmr = 66 + 13.7f * (weight) + 5.0f * (height) - 6.78f * age.toFloat()
                    Log.d("TAG", "bmr: ${bmr}")
                    Log.d("TAG", "bmr: ${bmr} bmi ${bmi}")
                    return bmr
                } else {
                    bmr = 66 + 13.7f * (weight) + 5.0f * (height) - 6.78f * age.toFloat()
                    Log.d("TAG", "bmr: ${bmr}")
                    var dietKg = 7700 / (7 * 2)
                    bmr = bmr + dietKg
                    Log.d("TAG", "bmr: ${bmr} bmi ${bmi}")
                    return bmr
                }
            } else {
                if (bmi > 25) {
                    bmr = 66 + 13.7f * (weight) + 1.8f * (height) - 6.78f * age.toFloat()
                    var dietQuarterKg = 7700 / (7 * 4)

                    bmr = bmr - dietQuarterKg
                    Log.d("TAG", "bmr: ${bmr}")
                    Log.d("TAG", "bmr: ${bmr} bmi ${bmi}")
                    return bmr
                }else if (bmi >= 18  && bmr <= 25){
                    bmr = 66 + 13.7f * (weight) + 1.8f * (height) - 6.78f * age.toFloat()
                    Log.d("TAG", "bmr: ${bmr} bmi ${bmi}")
                    Log.d("TAG", "bmr: ${bmr}")
                    return bmr
                } else {
                    bmr = 66 + 13.7f * (weight) + 1.8f * (height) - 6.78f * age.toFloat()
                    var dietKg = 7700 / (7 * 2)
                    bmr = bmr + dietKg
                    Log.d("TAG", "bmr: ${bmr} bmi ${bmi}")
                    Log.d("TAG", "bmr: ${bmr}")
                    return bmr
                }
            }
        }

        fun bmi(heightInM: Float, weight: Float): Float {
            val bmi = weight / (heightInM * heightInM).toFloat()
            return bmi
        }

        fun dailyCalorie(gender: String, weight: Float, height: Float, age: Int):Float {
            var dailyCalorie = 0.0f
            if(gender == "male") {
                dailyCalorie = 66 + 13.7f * (weight) + 5.0f * (height) - 6.78f * age.toFloat()
                return dailyCalorie
            } else {
                dailyCalorie = 66 + 9.6f * (weight) + 1.8f * (height) - 4.7f * age.toFloat()
                return dailyCalorie
            }
        }

        fun calDef(gender: String, weight: Float, height: Float, age: Int): Float {
            var caldef = dailyCalorie(gender, weight, height, age) - 500
            return caldef
        }

        fun maxCalPerServe(gender: String, weight: Float, height: Float, age: Int, bmi: Float): Float {
            return bmr(gender, weight, height, age, bmi)/3
        }

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

//        fun ow(bmr: Float, weight: Float, height: Float): Float {
//            val bmi = weight/(height*height)
//            Log.d("TAG", "ow: ${bmi}")
//            var ow = 0.0f
//
//            val maxweight: Float = maxWeight(height)
//            Log.d("TAG", "ow: ${maxweight}")
//            if (bmi > 25) {
//                Log.d("Tgas", "ow: ${maxweight.toString().trim()}")
//                ow = weight - maxweight
//                Log.d("Tgas", "ow: ${ow.toString().trim()}")
//                bmr = bmr - (10%bmr)
//                return ow
//            }else if (bmi < 18){
//                bmr = bmr + (10%bmr)
//                ow = 0.0f
//                return ow
//            }else {
//                bmr = bmr
//                ow = 0.0f
//                return ow
//            }
//        }
    }
}