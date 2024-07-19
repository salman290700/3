package com.example.dietjoggingapp.repositories

import android.content.Context
import android.os.StrictMode
import android.util.Log
import android.widget.Toast
import com.example.dietjoggingapp.adapters.FoodSuggestAdapter
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.database.domains.*
import com.example.dietjoggingapp.other.Constants
import com.example.dietjoggingapp.other.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import com.google.gson.Gson
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions

import okhttp3.*
import org.json.JSONArray

import java.io.IOException
import javax.inject.Singleton
import kotlin.math.roundToInt

class FoodSuggestImpl: FoodSuggestRepo {
    private lateinit var volleySingleton: Singleton
    private lateinit var listFoodSuggest: MutableList<FoodSuggest>
    private val gson = Gson()
    private lateinit var adapter: FoodSuggestAdapter
    private var database = FirebaseFirestore.getInstance()
    private lateinit var user: User
    override fun getFoodSuggestion(context: Context, url: String, result: (UiState<List<FoodSuggest>>) -> Unit) {
        var auth = FirebaseAuth.getInstance().currentUser?.email.toString()
        var document = database.collection(Constants.FirestoreTable.USERS).document(auth)

        document.get()
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    user = it.result.toObject(User::class.java)!!
                    listFoodSuggest = arrayListOf()
                    val client = OkHttpClient()
                    val policy= StrictMode.ThreadPolicy.Builder().permitAll().build();
                    StrictMode.setThreadPolicy(policy)
                    var listFoodSuggest = arrayListOf<FoodSuggest>()
                    val x = 10.35f

                    val y: Int = x.roundToInt()
                    Log.d("TAG", "getFoodSuggestion: ${y.toString().trim()}")
                    val request = Request.Builder()
                        .url("https://low-carb-recipes.p.rapidapi.com/search?tags=low-carb&maxAddedSugar=0&limit=5&maxCalories=${user.maxCalPerServe.roundToInt()}")
                        .get()
                        .addHeader("X-RapidAPI-Key", "af01d0da1cmshf3bec2d6210b1d8p1a6d54jsn7407d83c4f15")
                        .addHeader("X-RapidAPI-Host", "low-carb-recipes.p.rapidapi.com")
                        .build()
                    val response = client.newCall(request).execute()
                    client.newCall(request).enqueue(object: Callback{
                        override fun onFailure(call: Call, e: IOException) {

                        }

                        override fun onResponse(call: Call, response: Response) {

//                            var gson = Gson()
//                            var jsonString = response.body()?.string().toString().trim()
////                            Tidak menggunakan class
////                            var jsonArray = gson.fromJson(jsonString, FoodSuggest::class.java)
//                            var jsonArray2 = JSONArray(jsonString)
//                        if(jsonArray2.getJSONObject(0).has("message")){
//                            result.invoke(UiState.failure(jsonArray2.getJSONObject(0).get("message").toString().trim()))
//
//                        }else {
//                            Log.d("TAG", "onResponse json array2: ${jsonArray2}")
//                            if(jsonArray2 != null) {
//                                for (i in 0..jsonArray2.length()-1) {
//                                    var jsonObject = jsonArray2.getJSONObject(i)
//                                    var id = jsonObject.get("id")
//                                    var name = jsonObject.get("name")
//                                    var desc = jsonObject.get("description")
//                                    var ingredients: JSONArray = jsonObject.getJSONArray("ingredients")
//                                    var steps: JSONArray = jsonObject.getJSONArray(    "steps")
//                                    var image = jsonObject.get("image").toString().trim()
//                                    Log.d("TAG", "onResponse: iamge ${image}")
//                                    var foodSuggest = FoodSuggest()
//                                    foodSuggest.id = id.toString()
//                                    foodSuggest.name = name.toString()
//                                    foodSuggest.description = desc.toString()
//                                    var listIngredient: MutableList<Ingredient> = arrayListOf()
//                                    for (i in 0..ingredients.length()-1) {
//                                        Log.d("TAG", "onResponse: ${i.toString().trim()}")
//                                        var ingredientJsonObject = ingredients.getJSONObject(i)
//                                        var ingredient = Ingredient()
//                                        var servingSizeObj = ingredientJsonObject.getJSONObject("servingSize")
//                                        Log.d("TAG", "onResponse servingSize: ${servingSizeObj.toString().trim()}")
//                                        var servingSize = ServingSize()
//                                        var desc = servingSizeObj.get("desc")?.toString()
//                                        var grams: Float? = null;
//                                        if(servingSizeObj.has("grams")) {
//                                            grams = servingSizeObj.get("grams")?.toString()?.toFloat()
//                                        }else {
//                                            grams = null
//                                        }
//                                        var qty = servingSizeObj.get("qty")?.toString()?.toFloat()
//                                        var scale = servingSizeObj.get("scale")?.toString()?.toFloat()
////                                        var name = servingSizeObj.get("name").toString()
//                                        var units = servingSizeObj.get("units")?.toString()
//                                        if(desc?.isNotEmpty() == true) {
//                                            servingSize.desc = desc
//                                        }
//                                        if (grams != null) {
//                                            servingSize.grams = grams
//                                        }
//                                        if(qty?.isNaN() == true){
//                                            servingSize.qty = qty
//                                        }
//                                        if (scale?.isNaN() == true) {
//                                            servingSize.scale = scale
//                                        }
//                                        if(units?.isNotEmpty() == true) {
//                                            servingSize.units = units
//                                            ingredient.servingSize = servingSize
//                                            listIngredient.add(ingredient)
//                                        }
//
//
//
//                                    }
//                                    foodSuggest.ingredients = listIngredient
//
////                    Steps how to make the food
//                                    var listOfStep: MutableList<Step> = arrayListOf()
//                                    for (i in 0..steps.length()-1) {
//                                        Log.d("TAG", "onResponse: ${steps.get(i).toString().trim()}")
//                                        var stepsJsonObject = steps.get(i).toString()
//                                        var step = Step()
//                                        step.stepDesc = stepsJsonObject
//                                        listOfStep.add(step)
//                                    }
//                                    foodSuggest.steps = listOfStep
//                                    foodSuggest.image = image
//                                    listFoodSuggest.add(foodSuggest)
//
//                                    Log.d("TAG", "onResponse: ${listFoodSuggest.toString().trim()}")
//                                }
//
////            listFoodSuggest = gson.fromJson(jsonArray.get(), ListFoodSuggest::class.java)
//
//                            }
//                        }
                        }
                    })

                    // Create an English-German translator:
                    val options = TranslatorOptions.Builder()
                        .setSourceLanguage(TranslateLanguage.ENGLISH)
                        .setTargetLanguage(TranslateLanguage.INDONESIAN)
                        .build()
                    val englishIndonesianTranslator = Translation.getClient(options)
                    var conditions = DownloadConditions.Builder()
                        .build()
                    var gson = Gson()
                    var jsonString = response.body()?.string().toString().trim()
//                            Tidak menggunakan class
//                            var jsonArray = gson.fromJson(jsonString, FoodSuggest::class.java)
                    var jsonArray2 = JSONArray(jsonString)
                    if(jsonArray2.getJSONObject(0).has("message")){
                        result.invoke(UiState.failure(jsonArray2.getJSONObject(0).get("message").toString().trim()))

                    }else {
                        Log.d("TAG", "onResponse json array2: ${jsonArray2}")
                        if(jsonArray2 != null) {
                            for (i in 0..jsonArray2.length()-1) {
                                var jsonObject = jsonArray2.getJSONObject(i)

                                var id = jsonObject.get("id")
                                var name = jsonObject.get("name")
                                var desc = jsonObject.get("description")
                                var ingredients: JSONArray = jsonObject.getJSONArray("ingredients")
                                var steps: JSONArray = jsonObject.getJSONArray(    "steps")
                                var image = jsonObject.get("image").toString().trim()
                                Log.d("TAG", "onResponse: iamge ${image}")
                                var foodSuggest = FoodSuggest()
                                foodSuggest.id = id.toString()
                                foodSuggest.name = name.toString()
                                foodSuggest.description = desc.toString()
                                var listIngredient: MutableList<Ingredient> = arrayListOf()
                                for (i in 0..ingredients.length()-1) {
                                    Log.d("TAG", "onResponse: ${i.toString().trim()}")
                                    var ingredientJsonObject = ingredients.getJSONObject(i)
                                    var ingredient = Ingredient()
                                    var servingSizeObj = ingredientJsonObject.getJSONObject("servingSize")
                                    Log.d("TAG", "onResponse servingSize: ${servingSizeObj.toString().trim()}")
                                    var servingSize = ServingSize()
                                    var desc = servingSizeObj.get("desc")?.toString()
                                    var grams: Float? = null;
                                    if(servingSizeObj.has("grams")) {
                                        grams = servingSizeObj.get("grams")?.toString()?.toFloat()
                                    }else {
                                        grams = null
                                    }
                                    var qty = servingSizeObj.get("qty")?.toString()?.toFloat()
                                    var scale = servingSizeObj.get("scale")?.toString()?.toFloat()
//                                        var name = servingSizeObj.get("name").toString()
                                    var units = servingSizeObj.get("units")?.toString()
//                                    Gunakan name dan desc untuk bahan baku
                                    if(desc?.isNotEmpty() == true) {
                                        servingSize.desc = desc
                                    }
                                    if (grams != null) {
                                        servingSize.grams = grams
                                    }
                                    if(qty?.isNaN() == true){
                                        servingSize.qty = qty
                                    }
                                    if (scale?.isNaN() == true) {
                                        servingSize.scale = scale
                                    }
                                    if(units?.isNotEmpty() == true) {
                                        servingSize.units = units
                                        ingredient.servingSize = servingSize
                                        listIngredient.add(ingredient)
                                    }
                                }
                                foodSuggest.ingredients = listIngredient

//                    Steps how to make the food
                                var listOfStep: MutableList<Step> = arrayListOf()
                                for (i in 0..steps.length()-1) {
                                    Log.d("TAG", "onResponse: ${steps.get(i).toString().trim()}")
                                    var stepsJsonObject = steps.get(i).toString()
                                    var step = Step()
                                    step.stepDesc = stepsJsonObject
                                    listOfStep.add(step)
                                }
                                foodSuggest.steps = listOfStep
                                foodSuggest.image = image
                                listFoodSuggest.add(foodSuggest)

                                Log.d("TAG", "onResponse: ${listFoodSuggest.toString().trim()}")
                            }

//            listFoodSuggest = gson.fromJson(jsonArray.get(), ListFoodSuggest::class.java)

                        }
                    }

                    if(listFoodSuggest.isNotEmpty()) {
                        result.invoke(
                            UiState.Success(listFoodSuggest)
                        )
                    }else {
                        result.invoke(
                            UiState.failure("List is Empty")
                        )
                    }
                }else {
                    Log.d("TAG", "getFoodSuggestion: ${it.exception?.message?.trim()}")
                    Log.d("TAG", "getFoodSuggestion: ${it.exception?.localizedMessage?.trim()}")
                }
            }
            .addOnFailureListener {
                Log.d("TAG", "getFoodSuggestion: ${it.message?.trim()}")
                Log.d("TAG", "getFoodSuggestion: ${it.localizedMessage?.trim()}")
            }
    }
}