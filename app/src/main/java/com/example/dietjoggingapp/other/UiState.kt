package com.example.dietjoggingapp.other

sealed class UiState<out T> {
//    State for Success, Loading, & Failure Data Transaction
    object Loading:UiState<Nothing>()
    data class Success<out T>(val data: T): UiState<T>()
    data class failure(val error: String): UiState<Nothing>()
}
