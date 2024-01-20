package com.example.dietjoggingapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.databinding.ActivityRegisterBinding
import com.example.dietjoggingapp.other.Constants
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.utility.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference

class RegisterActivity : AppCompatActivity() {
    private var auth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        Setup register button
        val email = binding.etEmail.text.toString()
        val password = binding.etEmail.text.toString()

        binding.btnRegister.setOnClickListener {
            registerUser(email, password, setUser(), )
        }

//        Setup tvLogin
        binding
    }

    private fun registerUser(email: String, password: String, user: User){ //result: (UiState<String>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                Log.d("TAG", "registerUser: " + auth.currentUser?.uid.toString())
                val document = database.collection(Constants.FirestoreTable.USERS).document()
                user.userId = document.id
                document.set(user)
                    .addOnSuccessListener {
                        Log.d("TAG", "registerUser: " + user.userId)
                        val intent: Intent = Intent(this, LoginActivity::class.java)
                        startActivity(intent)
                    }

                    .addOnFailureListener{
                        Log.d("TAG", "addUser: " + it.localizedMessage)
                    }
            }
    }

    private fun setUser(): User {
        val email = binding.etEmail.text.toString()
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString().toFloat()
        val height  = binding.etHeight.text.toString().toFloat()
        val age = binding.etAge.text.toString().toFloat()
        var gender = ""
        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == R.id.rbMale) {
                gender = "Male"
            } else {
                gender = "female"
            }
        }
        return User(
            userId = "",
            fullName = name,
            email = email,
            weight = weight,
            height = height,
            age = age,
            gender = gender
        )
    }

    private fun dailyCalorie(): Float {
        val weight = binding.etWeight.text.toString().toFloat()
        val height = binding.etHeight.text.toString().toFloat()
        val age = binding.etAge.text.toString().toFloat()
        var gender: String = ""

        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == R.id.rbMale) {
                gender = "Male"
            } else {
                gender = "female"
            }
        }

        var bmr = 0.0f

        if(gender == "Male") {
            bmr = (10.0f * weight + 6.25f * height - 5.0f * age + 5.0f).toFloat()
        } else {
            bmr = (10.0f * weight + 6.25f * height - 5.0f * age - 161.0f).toFloat()
        }

        return bmr
    }

    fun validation(): Boolean {
        var isValid = true
        var name = binding.etName.text.toString()
        var email = binding.etEmail.text.toString()
        var password = binding.etPassword.text.toString()
        var confPassword = binding.etConfPassword.text.toString()
        var gender = ""
        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == R.id.rbMale) {
                gender = "Male"
            } else {
                gender = "female"
            }
        }

        if (gender.isNullOrEmpty()){
            isValid = false
        }
        if (email.isNullOrEmpty()){
            isValid = false
            Toast.makeText(this, "Enter a valid email", Toast.LENGTH_SHORT)
        }
        if (name.isNullOrEmpty()){
            isValid = false
            Toast.makeText(this, getString(R.string.enter_fullname), Toast.LENGTH_SHORT)
        }
        if (password.isNullOrEmpty()){
            isValid = false
            Toast.makeText(this, getString(R.string.enter_pass), Toast.LENGTH_SHORT)
        }
        if (confPassword.isNullOrEmpty()){
            isValid = false
            Toast.makeText(this, "Please enter Confirm Password", Toast.LENGTH_SHORT)
        }
        if (confPassword != password ) {
            isValid = true
            Toast.makeText(this, "Password & Confirm Password not identic", Toast.LENGTH_SHORT)
        }
        if (password?.length!! < 6){
            isValid = false
            Toast.makeText(this, getString(R.string.longer_pass), Toast.LENGTH_SHORT)
        }
        return isValid
    }
}