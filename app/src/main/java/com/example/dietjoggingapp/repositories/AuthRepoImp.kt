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
    val sharedPreferences: SharedPreferences,
    val gson: Gson
): AuthRepo {
    override fun registerUser(email: String, password: String, user: User, result: (UiState<String>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                if(it.isSuccessful) {
                    user.userId = it.result?.user?.uid ?: ""
                    updateUserinfo(user) {state ->
                        when(state) {
                            is UiState.Success -> {
                                storeSession(
                                    id = it.result.user?.uid ?: ""
                                ) {state ->
                                    if(state == null) {
                                        result.invoke(
                                            UiState.failure("Session Store Failed")
                                        )
                                    }
                                    else {
                                        result.invoke(
                                            UiState.Success("Session Stored Succesfully")
                                        )
                                    }
                                }
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
                    storeSession(id = task.result.user?.uid ?: "") {
                        if(it == null) {
                            result.invoke(
                                UiState.failure("StoreSession Failed..!")
                            )
                            Log.d("TAG", "loginUser: Store Session Failed")

                        } else {
                            result.invoke(UiState.Success("StoreSession Success..!"))
                            Log.d("TAG", "loginUser: StoreSessionSuccess")
                        }
                    }
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

    override fun storeSession(id: String, result: (User?) -> Unit) {
        database.collection(Constants.FirestoreTable.USERS).document(id).get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    val user = it.result.toObject(User::class.java)
                    sharedPreferences.edit().putString(Constants.sharedPreferences.USER_SESSION, gson.toJson((user))).apply()
                    result.invoke(user)
                } else {
                    result.invoke(null)
                }
            }
            .addOnFailureListener{
                result.invoke(null)
            }
    }

    override fun getSession(result: (User?) -> Unit) {
        val usesStr = sharedPreferences.getString(Constants.sharedPreferences.USER_SESSION, null)

        if(usesStr == null) {
            result.invoke(null)
        } else {
            val user = gson.fromJson(usesStr, User::class.java)
            result.invoke(user)
            Log.d("TAG", "getSession: ${user.userId}")
        }
    }

    override fun logout(result: () -> Unit) {
        auth.signOut()
        sharedPreferences.edit().putString(Constants.sharedPreferences.USER_SESSION, null).apply()
        result.invoke()
    }

}