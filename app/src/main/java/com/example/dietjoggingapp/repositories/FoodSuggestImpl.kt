package com.example.dietjoggingapp.repositories

import android.content.Context
import android.os.StrictMode
import android.util.Log
import com.example.dietjoggingapp.adapters.FoodSuggestAdapter
import com.example.dietjoggingapp.database.domains.*
import com.example.dietjoggingapp.other.UiState

import com.google.gson.Gson

import okhttp3.*
import org.json.JSONArray

import java.io.IOException
import javax.inject.Singleton

class FoodSuggestImpl: FoodSuggestRepo {
    private lateinit var volleySingleton: Singleton
    private lateinit var listFoodSuggest: MutableList<FoodSuggest>
    private val gson = Gson()
    private lateinit var adapter: FoodSuggestAdapter
    override fun getFoodSuggestion(context: Context, url: String, result: (UiState<List<FoodSuggest>>) -> Unit) {
                listFoodSuggest = arrayListOf()
                val client = OkHttpClient()
                val policy= StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy)
                var listFoodSuggest = arrayListOf<FoodSuggest>()
                val request = Request.Builder()
                    .url("https://low-carb-recipes.p.rapidapi.com/search?tags=low-carb&maxAddedSugar=0&limit=5")
                    .get()
                    .addHeader("X-RapidAPI-Key", "af01d0da1cmshf3bec2d6210b1d8p1a6d54jsn7407d83c4f15")
                    .addHeader("X-RapidAPI-Host", "low-carb-recipes.p.rapidapi.com")
                    .build()
                val response = client.newCall(request).execute()
                client.newCall(request).enqueue(object: Callback{
                    override fun onFailure(call: Call, e: IOException) {
                        TODO("Not yet implemented")
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
    }
}