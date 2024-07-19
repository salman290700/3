package com.example.dietjoggingapp.ui

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
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

        binding.tvRegister.setOnClickListener {
            val intent: Intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    private fun signInEmail(email: String, password: String){
        val auth: FirebaseAuth = FirebaseAuth.getInstance()
        if(email.isNotEmpty() && password.isNotEmpty()){
            Log.d("TAG", "signInEmail:  ${password}")
            auth.signInWithEmailAndPassword(email, password).addOnCompleteListener{
                if (it.isSuccessful){
                    Toast.makeText(this, "Anda berhasil Login", Toast.LENGTH_LONG)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }else {
                    Toast.makeText(this, it.exception?.message.toString(), Toast.LENGTH_LONG)
                }
            }
                .addOnFailureListener {
                    Toast.makeText(this, "Error ${it.localizedMessage.toString()}", Toast.LENGTH_SHORT).show()
                    Log.d("TAG", "signInEmail: ${it.localizedMessage.toString()}")
                    Log.d("TAG", "signInEmail: ${it.message.toString()}")
                    Toast.makeText(this, "Error ${it.message.toString()}", Toast.LENGTH_SHORT).show()
                }
        }else{
            Toast.makeText(this, "Tolong lengkapi email & password anda", Toast.LENGTH_LONG)
        }
    }

    private fun btnLogin() {
        var email = binding.etEmail.text.toString()
        var password = binding.etPassword.text.toString()
        Log.d("TAG", "btnLogin: ${password.trim()}")
        signInEmail(email, password)
    }
}