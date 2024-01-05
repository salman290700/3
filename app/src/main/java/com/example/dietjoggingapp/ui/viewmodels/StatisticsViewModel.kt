package com.example.dietjoggingapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import com.example.dietjoggingapp.repositories.MainRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

class StatisticsViewModel (
    val mainRepository: MainRepository
):ViewModel() {
    
}