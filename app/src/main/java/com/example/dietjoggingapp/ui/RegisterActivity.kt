package com.example.dietjoggingapp.ui

import android.app.DatePickerDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.databinding.ActivityRegisterBinding
import com.example.dietjoggingapp.other.Constants
import com.example.dietjoggingapp.other.registerUtils
import com.example.dietjoggingapp.utility.utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.StorageReference
import java.math.RoundingMode
import java.util.Calendar
import kotlin.math.max

class RegisterActivity : AppCompatActivity() {
    private var auth = FirebaseAuth.getInstance()
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: FirebaseFirestore
    private lateinit var storageReference: StorageReference
    private lateinit var user: User
    private var ageInYear: Int = 0
    private var ageDay: Int = 0
    private var ageMonth: Int = 0
    private var ageYear: Int = 0
    private var bmr = 0.0f
    private var overWeight = 0.0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        Setup register button

        val c = Calendar.getInstance()

        val current_date = c.get(Calendar.DAY_OF_MONTH)
        val current_month = c.get(Calendar.MONTH)
        val current_year = c.get(Calendar.YEAR)
        val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            binding.birhdate.setText("" + dayOfMonth + " " + month + ", " + year)
            ageDay = dayOfMonth
            ageMonth = month
            ageYear = year
        }, current_year, current_month, current_date)

        binding.birhdate.setOnClickListener {
            datePickerDialog.show()
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.etPassword.text.toString()
            if (utils.ageInYear(ageYear, ageMonth, ageDay) < 18) {
                Toast.makeText(this, "Anda belum cukup Umur", Toast.LENGTH_SHORT).show()
            }else {
                registerUser(email, password, setUser(ageYear, ageMonth, ageDay))
            }
        }
//        Setup tvLogin
        binding.addPicture.setOnClickListener {
        }
    }

    private fun registerUser(email: String, password: String, user: User){
        //result: (UiState<String>) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener{
                Log.d("TAG", "registerUser: " + auth.currentUser?.uid.toString())
                database = FirebaseFirestore.getInstance()
                val document = database.collection(Constants.FirestoreTable.USERS).document(email)
                user.userId = it.result.user?.uid.toString()
                Log.d("TAG", "registerUser: ${user.age.toString().trim()}")
                if (user.age < 18) {
                    Toast.makeText(this, "Anda belum cukup umur", Toast.LENGTH_SHORT).show()
                }else {
                    document.set(user)
                        .addOnSuccessListener {
                            Log.d("TAG", "registerUser: " + user.userId)
                            val intent: Intent = Intent(this, LoginActivity::class.java)
                            startActivity(intent)
                        }
                        .addOnFailureListener{
                            Log.d("TAG", "addUser: " + it.localizedMessage.trim())
                            Log.d("TAG", "registerUser: ${it.message?.trim()}")
                        }
                        .addOnCompleteListener {
                            if (it.isSuccessful) {
                                Toast.makeText(this, "Register Successfull!!", Toast.LENGTH_SHORT).show()
                            }else {
                                Toast.makeText(this, "Error ${it.exception?.message?.toString()}", Toast.LENGTH_SHORT).show()
                            }
                        }
                        .addOnFailureListener {
                            Log.d("TAG", "addUser: " + it.localizedMessage.trim())
                            Log.d("TAG", "registerUser: ${it.message?.trim()}")
                        }
                }
            }
    }

    private fun setUser(year: Int, month: Int, day: Int): User {
        val email = binding.etEmail.text.toString()
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString().toFloat()
        val height  = (binding.etHeight.text.toString().toFloat() / 100.00).toBigDecimal().setScale(2, RoundingMode.DOWN).toFloat()
        val age = binding.etAge.text.toString().toInt()
        var gender = ""
        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->

        }

        if(binding.rbMale.isChecked) {
            gender = "male"
        } else if(binding.rbFemale.isChecked) {
            gender = "female"
        }
//        val datePickerDialog = DatePickerDialog(this, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
//            binding.etBirthDate.setText("" + dayOfMonth + " " + month + ", " + year)
//        }, current_year, current_month, current_date)
//        binding.birthDateLayout.setOnClickListener {
//            datePickerDialog.show()
//        }

        ageInYear = registerUtils.ageInYear(year, month, day)

        Log.d("TAG", "setUser: ${gender}")
        bmr = utils.bmr(gender, weight, height * 100, age, utils.bmi(height, weight))
        Log.d("TAG", "setUser: ${bmr}")
        overWeight = ow(weight, height)
        Log.d("TAG", "setUser: ${bmr}")
        Log.d("TAG", "setUser: ${registerUtils.ageInYear(ageYear, ageMonth, ageDay)}")
        val bmi = utils.bmi(height, weight)
        user = User(userId = "",
            fullName = name,
            email = email,
            dailyCalorie = utils.dailyCalorie(gender, weight, height, age),
            bmr = bmr,
            weight = weight,
            height = height,
            age = ageInYear,
            gender = gender,
            calDef = calDef(weight, height, age),
            overweight = overWeight,
            maxWeight = maxWeight(height),
            maxCalPerServe = utils.maxCalPerServe(gender, weight, height * 100, age, bmi),
            birthDate = ageDay,
            birthMonth = ageMonth,
            birthYear = ageYear,
            bmi = utils.bmi(height, weight)
        )
        Log.d("TAG", "setUser: ${user.toString().trim()}")
        return user
    }

    private fun dailyCalorie(weight: Float, height: Float, age: Int):Float {
        var gender: String = ""

        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->

        }

        if(binding.rbMale.isChecked) {
            gender = "male"
        } else {
            gender = "female"
        }
        if(gender == "male") {
            bmr = 66 + 13.7f * (weight) + 5.0f * (height) - 6.78f * age.toFloat()
            Log.d("TAG", "dailyCalorie: ${bmr}")
            return bmr
        } else {
            bmr = 66 + 9.6f * (weight) + 1.8f * (height) - 4.7f * age.toFloat()
            Log.d("TAG", "dailyCalorie: ${bmr}")
            return bmr
        }
    }

    private fun calDef(weight: Float, height: Float, age: Int): Float {
        var caldef = dailyCalorie(weight, height, age) - 500
        return caldef
    }
    private fun ow(weight: Float, height: Float): Float {
        val bmi = weight/(height*height)
        Log.d("TAG", "ow: ${bmi}")
        var ow = 0.0f
        val maxweight: Float = maxWeight(height)
        Log.d("TAG", "ow: ${maxweight}")
        Log.d("TAG", "ow: ${bmr}")
        if (bmi > 25) {
            Log.d("Tgas", "ow: ${maxweight.toString().trim()}")
            ow = weight - maxweight
            Log.d("Tgas", "ow: ${ow.toString().trim()}")
            bmr = bmr - (10%bmr)
            return ow
        }else if (bmi < 18){
            bmr = bmr + (20%bmr)
            ow = 0.0f
            return ow
        }else {
            bmr = bmr
            ow = 0.0f
            return ow
        }
    }

    private fun maxWeight(height: Float) : Float {
        var maxWeight = 25 * (height * height)
        return maxWeight
    }
    fun validation(): Boolean {
        var isValid = false
        var name = binding.etName.text.toString()
        var email = binding.etEmail.text.toString()
        var password = binding.etPassword.text.toString()
        var confPassword = binding.etConfPassword.text.toString()
        var gender = ""
        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->

        }

        if(binding.rbMale.isChecked) {
            gender = "Male"
        } else {
            gender = "female"
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
        if (password?.length!! < 8){
            isValid = false
            Toast.makeText(this, getString(R.string.longer_pass), Toast.LENGTH_SHORT)
        }
        return isValid
    }
}