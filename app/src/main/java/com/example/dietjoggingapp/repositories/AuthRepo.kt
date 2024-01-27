package com.example.dietjoggingapp.repositories

import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.other.UiState
import dagger.Provides

interface AuthRepo {

    fun registerUser(email: String, password: String, user: User, result: (UiState<String>) -> Unit)


    fun updateUserinfo(user: User, result: (UiState<String>) -> Unit)


    fun loginUser(email: String, password: String, result: (UiState<String>) -> Unit)


    fun forgotPassword(user: User, result: (UiState<String>) -> Unit)

    fun logout(result: () -> Unit)
}