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

        binding.tvRegister.setOnClickListener {
            val intent: Intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {


        return super.onCreateView(name, context, attrs)
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

    private fun btnLogin() {
        var email = binding.etEmail.text.toString()
        var password = binding.etPassword.text.toString()
        signInEmail(email, password)
    }
}