package com.example.dietjoggingapp.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Toast
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener{
            btnLogin()
        }
    }

    private fun btnLogin() {
        var email = binding.etEmail.getText().toString()
        var password = binding.etPassword.getText().toString()

        signInEmail(email, password)
    }

    private fun signInEmail(email: String, password: String){
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        if(email.isNotEmpty() && password.isNotEmpty()){
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                if (it.isSuccessful){

                    Toast.makeText(this, "Anda berhasil Login", Toast.LENGTH_LONG)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }else {
                    Toast.makeText(this, it.exception.toString(), Toast.LENGTH_LONG)
                }
            }
        }else{
            Toast.makeText(this, "Tolong lengkapi email & password anda", Toast.LENGTH_LONG)
        }
    }
}