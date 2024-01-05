package com.example.dietjoggingapp.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.repositories.AuthRepo
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(val auth: AuthRepo): ViewModel() {
    private val _register = MutableLiveData<UiState<String>>()
    val register: LiveData<UiState<String>>
    get() =_register

    fun registerUser(
        email: String,
        password: String,
        user: User
    ) {
        _register.value = UiState.Loading
        auth.registerUser(
            email = email,
            password = password,
            user = user
        ){
            _register.value = it
        }
    }

    private val _login = MutableLiveData<UiState<String>>()
    val login: LiveData<UiState<String>>
    get() = _login

    fun login(email: String, password: String) {
        _login.value = UiState.Loading
        auth.loginUser(
            email = email,
            password = password
        ) {
            _register.value = it
        }
    }

    fun getSession(result: (User?) -> Unit) {
        auth.getSession(result = result)
    }

}