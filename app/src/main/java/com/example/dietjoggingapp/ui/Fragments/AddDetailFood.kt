package com.example.dietjoggingapp.ui.Fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.DailyCalories
import com.example.dietjoggingapp.database.Food
import com.example.dietjoggingapp.databinding.ActivityAddDetailFoodBinding
import com.example.dietjoggingapp.other.Constants
import com.example.dietjoggingapp.ui.MainActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AddDetailFood : AppCompatActivity() {
    val database = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityAddDetailFoodBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDetailFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
        binding.btnSave.setOnClickListener {
            sendFoodToFirebase()
        }
    }


    private fun sendFoodToFirebase() {
        val auth = FirebaseAuth.getInstance().currentUser
        val document = database.collection("FOODS").document()
        val documentDaillyCalorie = database.collection("DAILYCALORIE").document(auth!!.uid)
        val foodName = binding.etFoodName.text.toString()
        val foodDesc = binding.etFoodDesc.text.toString()

        var dailyCalorie = 0f

        val food = Food(foodName = foodName, foodDescription = foodDesc, userId = auth?.uid.toString())
        if (foodName.isNullOrEmpty()) {
            Toast.makeText(this, "Please fill uo Food name Field", Toast.LENGTH_SHORT)
        }else {
            var total_cal = 0f
            document.set(food!!)
                .addOnSuccessListener {
                    val dailyCalories = DailyCalories(user_id = auth!!.uid.toString(), calorie = food.foodCalorie)
//                    Update API untuk mendapatkan informasi jumlah calorie makanan
                    val client = OkHttpClient()
                    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                    StrictMode.setThreadPolicy(policy)
                    var query = ""
                    val request = Request.Builder()
                        .url("https://api.calorieninjas.com/v1/nutrition?query=${query}")
                        .addHeader("X-Api-Key", "SN4CZ3q5711ByjJ9PzgBRg==WbORRErHMV8pIY1v")
                        .get()
                        .build()
                    val response = client.newCall(request).execute()
                    client.newCall(request).enqueue(object: Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            TODO("Not yet implemented")
                        }

                        override fun onResponse(call: Call, response: Response) {
                            var jsonString = response.body()?.string().toString().trim()
                            var jsonArray = JSONObject(jsonString)
                            if(jsonArray != null) {
                                var items = jsonArray.getJSONArray("items")
                                for (i in 0..items.length() - 1) {
                                    var jsonObject = items.getJSONObject(i)
                                    var calories = jsonObject.get("calories").toString().toFloat()
                                    total_cal = total_cal + calories
                                    dailyCalories.calorie = total_cal
                                }
                            }
                        }

                    })
//                    Update Calorie Harian
                    documentDaillyCalorie.get().addOnCompleteListener {
                        if (it.isSuccessful) {
                            documentDaillyCalorie.update("calorie", dailyCalories.calorie).addOnCompleteListener {
                                Toast.makeText(this, "Data Calorie sudah diupdate", Toast.LENGTH_LONG)

                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            }.addOnFailureListener {
                                Toast.makeText(this, it.localizedMessage, Toast.LENGTH_SHORT)
                            }
                        }
                    }
                        .addOnFailureListener {
                            documentDaillyCalorie.set(dailyCalories)
                        }
                }.addOnFailureListener{
                    Toast.makeText(this, it.localizedMessage.toString(), Toast.LENGTH_LONG)
                }
        }
    }
    private fun initCloudStorage(uri: Uri): String{

        var ref = FirebaseStorage.getInstance()
        val auth = FirebaseAuth.getInstance().currentUser
        var storage = ref.reference
        val imageFolder = storage.child("photo/${Constants.FirestoreTable.FOOD}/${auth!!.uid}/")

        imageFolder.putFile(uri).addOnCompleteListener {
            if (it.isSuccessful) {

            }
            imageFolder.downloadUrl
            Log.d("TAG", "initCloudStorage:")
            Toast.makeText(this, "Photo has already uploadad", Toast.LENGTH_SHORT)

        }.addOnFailureListener{
            Toast.makeText(this, it.toString().trim(), Toast.LENGTH_SHORT)
        }.addOnSuccessListener {
            Log.d("TAG", "initCloudStorage:")
        }
        return imageFolder.downloadUrl.toString()
    }


}