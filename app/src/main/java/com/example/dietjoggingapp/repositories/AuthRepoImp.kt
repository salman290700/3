package com.example.dietjoggingapp.repositories

import android.content.SharedPreferences
import android.util.Log
import android.widget.Toast
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.other.Constants
import com.example.dietjoggingapp.other.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson

class AuthRepoImp(
    val auth: FirebaseAuth,
    val database: FirebaseFirestore,
): AuthRepo {
    override fun registerUser(email: String, password: String, user: User, result: (UiState<String>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if(it.isSuccessful) {
                    user.userId = it.result?.user?.uid ?: ""
                    updateUserinfo(user) {state ->
                        when(state) {
                            is UiState.Success -> {
                                UiState.Success("User Registration Success..!")
                            }
                            is UiState.Loading -> {

                            }

                            is UiState.failure -> {
                                UiState.failure(state.error)
                            }
                        }
                    }
                }
                else {
                    try {
                        throw it.exception ?:java.lang.Exception("Invalid Credential")
                    }catch (e: FirebaseAuthWeakPasswordException) {
                        result.invoke(
                            UiState.failure("Create a password more than 8 Chars")
                        )
                    }catch (e: FirebaseAuthInvalidCredentialsException) {
                        result.invoke(
                            UiState.failure("Invalid Credential")
                        )
                    }catch (e: java.lang.Exception) {
                        result.invoke(
                            UiState.failure(e.message.toString())
                        )
                    }
                }
            }
    }

    override fun updateUserinfo(user: User, result: (UiState<String>) -> Unit) {
        val document = database.collection(Constants.FirestoreTable.USERS).document(user.userId)

        document.set(user)
            .addOnSuccessListener {
                result.invoke(
                    UiState.Success("User has been updated Succesfully")
                )
            }
            .addOnFailureListener{
                result.invoke(
                    UiState.failure(it.localizedMessage.toString())
                )
            }
    }

    override fun loginUser(email: String, password: String, result: (UiState<String>) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {task ->
                if(task.isSuccessful) {
                    result.invoke(
                        UiState.Success("Authentication Success")
                    )
                }
            }
            .addOnFailureListener { task ->
                result.invoke(
                    UiState.failure("Authentication Failed, Please enter the right email & password")
                )
                Log.d("TAG", "loginUser: Authentication Failed")
            }
    }

    override fun forgotPassword(user: User, result: (UiState<String>) -> Unit) {
        TODO("Not yet implemented")
    }

    override fun logout(result: () -> Unit) {
        auth.signOut()
        result.invoke()
    }
}