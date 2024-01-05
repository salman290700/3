package com.example.dietjoggingapp.repositories

import com.example.dietjoggingapp.database.Jogging
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class MainRepository
(
//    val joggingDAO: JoggingDAO
)
{
    suspend fun insertJogging(jogging: Jogging) {
//        joggingDAO.insertJogging(jogging)
    }

    suspend fun deleteJogging(jogging: Jogging) {
//        joggingDAO.deleteJogging(jogging)
    }

    fun getAllJoggingSortedByDate() = null
//        joggingDAO.getAllJoggingSortedByDate()

    fun getAllJoggingSortedByDistance() = null
//        joggingDAO.getAllJoggingSortedByDistance()

    fun getAllJoggingSortedByTimeInMillis() = null
//        joggingDAO.getAllJoggingSortedByTimeInMillis()

    fun getAllJoggingSortedByAvgSpeed() = null
//        joggingDAO.getAllJoggingSortedByAvgSpeed()

    fun getAllJoggingSortedByCaloriesBurned() = null
//        joggingDAO.getAllJoggingSortedByCaloriesBurned()

    fun getTotalAvgSpeed() {
//        joggingDAO.getTotalAvgSpeedInKmh()
    }

    fun getTotalDistance() {
//        joggingDAO.getTotalDistance()
    }

    fun getTotalCaloriesBurned() {
//        joggingDAO.getTotalCaloriesBurned()
    }

    fun getTotalTimeInMillis() {
//        joggingDAO.getTotalTimeInMillis()
    }
}