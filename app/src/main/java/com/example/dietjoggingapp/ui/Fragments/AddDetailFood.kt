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
    private lateinit var calory: DailyCalories
    val database = FirebaseFirestore.getInstance()
    private lateinit var binding: ActivityAddDetailFoodBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddDetailFoodBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnSave.setOnClickListener {
            sendFoodToFirebase()
        }
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        return super.onCreateView(name, context, attrs)
        binding.btnSave.setOnClickListener {
            sendFoodToFirebase()
        }
    }

    private fun sendFoodToNinjaCalorieAPI(): Float {
        val auth = FirebaseAuth.getInstance().currentUser
        val document = database.collection("FOODS").document()
        val documentDailyCalorie2 = database.collection("DAILYCALORIE").document(auth!!.uid)
        val foodName = binding.etFoodName.text.toString()
        val foodDesc = binding.etFoodDesc.text.toString()

        val client = OkHttpClient()
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var query = ""
        query = foodName

        val request = Request.Builder()
            .url("https://api.calorieninjas.com/v1/nutrition?query=${query}")
            .addHeader("X-Api-Key", "SN4CZ3q5711ByjJ9PzgBRg==WbORRErHMV8pIY1v")
            .get()
            .build()
        val response = client.newCall(request).execute()
        var jsonString = response.body()?.string().toString().trim()
        var jsonArray = JSONObject(jsonString)

        if (jsonArray != null) {
            var items = jsonArray.getJSONArray("items")
            for (i in 0..items.length() - 1) {
                var jsonObject = items.getJSONObject(i)
                var calories = jsonObject.get("calories").toString().toFloat()
                Log.d("TAG", "sendFoodToFirebase calory before: ${calory.calorie} ")
                Log.d("TAG", "sendFoodToFirebase calories: ${calories}")
                calory.calorie = calory.calorie + calories
                Log.d("TAG", "sendFoodToFirebase calory after: ${calory.calorie}")
            }
            Log.d("TAG", "sendFoodToFirebase: ${calory.calorie}")
            documentDailyCalorie2.update("calorie", calory.calorie).addOnSuccessListener {
                Log.d("TAG", "sendFoodToFirebase: ${it}")
            }
                .addOnFailureListener {
                    Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                }
            Toast.makeText(this, "Data Calorie sudah diupdate", Toast.LENGTH_LONG)

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        Toast.makeText(this, "${calory.calorie}", Toast.LENGTH_LONG)
        return calory.calorie
    }

    private fun sendFoodToFirebase() {
        Log.d("TAG", "sendFoodToFirebase: SendToFirebase")
        val auth = FirebaseAuth.getInstance().currentUser
        val document = database.collection("FOODS").document()
        val documentDailyCalorie2 = database.collection("DAILYCALORIE").document(auth!!.uid)
//        val documentDaillyCalorie = database.collection("DAILYCALORIE").document(auth!!.uid).get().result.toObject(DailyCalories::class.java)

        val foodName = binding.etFoodName.text.toString()
        val foodDesc = binding.etFoodDesc.text.toString()


        var total_cal =0f

        database.collection("DAILYCALORIE").document(auth!!.uid).get()
            .addOnSuccessListener {
                if(it.data!!.isEmpty()) {
                    documentDailyCalorie2.set(DailyCalories(user_id = auth.uid, calorie = 0f))
                }
                calory = it.toObject(DailyCalories::class.java)!!
                val client = OkHttpClient()
                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                var query = ""
                query = foodName
                val request = Request.Builder()
                    .url("https://api.calorieninjas.com/v1/nutrition?query=${query}")
                    .addHeader("X-Api-Key", "SN4CZ3q5711ByjJ9PzgBRg==WbORRErHMV8pIY1v")
                    .get()
                    .build()
                val response = client.newCall(request).execute()
                var jsonString = response.body()?.string().toString().trim()
                var jsonArray = JSONObject(jsonString)
//            Log.d("TAG", "sendFoodToFirebase: ${calory.calorie}")
                if(jsonArray != null) {
                    var items = jsonArray.getJSONArray("items")
                    for (i in 0..items.length() - 1) {
                        var jsonObject = items.getJSONObject(i)
                        var calories = jsonObject.get("calories").toString().toFloat()
                        Log.d("TAG", "sendFoodToFirebase calory before: ${calory.calorie} ")
                        Log.d("TAG", "sendFoodToFirebase calories: ${calories}")
                        calory.calorie = calory.calorie + calories
                        Log.d("TAG", "sendFoodToFirebase calory after: ${calory.calorie}")
                    }
                    Log.d("TAG", "sendFoodToFirebase: ${calory.calorie}")
                    documentDailyCalorie2.update("calorie", calory.calorie).addOnSuccessListener {
                        Log.d("TAG", "sendFoodToFirebase: ${it}")
                    }
                        .addOnFailureListener {
                            Toast.makeText(this, it.message.toString(), Toast.LENGTH_SHORT).show()
                        }
                    Toast.makeText(this, "Data Calorie sudah diupdate", Toast.LENGTH_LONG)

                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                }
            }.addOnFailureListener {
                Toast.makeText(this, it.message.toString().trim(), Toast.LENGTH_SHORT).show()
            }

            val food = Food(foodName = foodName, foodDescription = foodDesc, userId = auth?.uid.toString())

//                if (documentDaillyCalorie.result != null){
//                    calory = documentDaillyCalorie.result.toObject(DailyCalories::class.java)!!
//                    Log.d("TAG", "sendFoodToFirebase: ${calory.calorie}")
//                    if(jsonArray != null) {
//                        var items = jsonArray.getJSONArray("items")
//                        for (i in 0..items.length() - 1) {
//                            var jsonObject = items.getJSONObject(i)
//                            var calories = jsonObject.get("calories").toString().toFloat()
//                            total_cal = total_cal + calories
//                            Log.d("TAG", "sendFoodToFirebase: ${calories}")
//                            Log.d("TAG", "sendFoodToirebase: ${total_cal}")
//                            total_cal = calory!!.calorie + total_cal
//                            Log.d("TAG", "sendFoodToFirebase: ${total_cal}")
//                            documentDailyCalorie2.update("calorie", total_cal)
//                            Log.d("TAG", "sendFoodToFirebase: ${total_cal.toString()}")
//                        }
//                    }
//                } else {
//                    documentDailyCalorie2.set(DailyCalories(user_id = auth.uid, calorie = 0f))
//                        .addOnSuccessListener {
//                            Log.d("TAG", "sendFoodToFirebase: ${it.toString()}")
//                        }.addOnFailureListener {
//                            Log.d("TAG", "sendFoodToFirebase: ${it.localizedMessage}")
//                        }
//                }


//            Log.d("TAG", "sendFoodToFirebase: ${response.body().toString()}")
//            document.set(food!!)
//                .addOnSuccessListener {
//                    val dailyCalories = DailyCalories(user_id = auth!!.uid.toString(), calorie = food.foodCalorie)
////                    Update API untuk mendapatkan informasi jumlah calorie makanan
//                    val client = OkHttpClient()
//                    val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
//                    StrictMode.setThreadPolicy(policy)
//                    var query = ""
//                    val request = Request.Builder()
//                        .url("https://api.calorieninjas.com/v1/nutrition?query=${query}")
//                        .addHeader("X-Api-Key", "SN4CZ3q5711ByjJ9PzgBRg==WbORRErHMV8pIY1v")
//                        .get()
//                        .build()
//                    val response = client.newCall(request).execute()
//                    Log.d("TAG", "sendFoodToFirebase: ${response.body().toString()}")
//
//
////                    client.newCall(request).enqueue(object: Callback {
////                        override fun onFailure(call: Call, e: IOException) {
////                            TODO("Not yet implemented")
////                        }
////
////                        override fun onResponse(call: Call, response: Response) {
////                            var jsonString = response.body()?.string().toString().trim()
////                            var jsonArray = JSONObject(jsonString)
////                            if(jsonArray != null) {
////                                var items = jsonArray.getJSONArray("items")
////                                for (i in 0..items.length() - 1) {
////                                    var jsonObject = items.getJSONObject(i)
////                                    var calories = jsonObject.get("calories").toString().toFloat()
////                                    total_cal = total_cal + calories
////                                    dailyCalories.calorie = total_cal
////                                    Log.d("TAG", "onResponse: ${dailyCalories.calorie.toString()}")
////                                    documentDaillyCalorie.update("calorie", total_cal)
////                                    Log.d("TAG", "onResponse: ${response}")
////
////                                }
////                            }
////                        }
////
////
////
////                    })
////                    Update Calorie Harian
//                }.addOnFailureListener{
//                    Toast.makeText(this, it.localizedMessage.toString(), Toast.LENGTH_LONG)
//                }
        }
}