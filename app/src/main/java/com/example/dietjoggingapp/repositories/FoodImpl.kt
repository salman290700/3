package com.example.dietjoggingapp.repositories

import com.example.dietjoggingapp.database.Food
import com.example.dietjoggingapp.other.Constants
import com.example.dietjoggingapp.other.UiState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class FoodImpl(val auth: FirebaseAuth, val database: FirebaseFirestore): FoodRepo {
    override fun addFood(
        food: Food,
        result: (UiState<String>) -> Unit
    ) {
        val currentUser = auth.currentUser
        val document = database.collection(Constants.FirestoreTable.FOOD).document(currentUser!!.uid)
        var foodId = "${currentUser.uid}${Date()}"
//        var food  = Food(foodImg = foodId, foodName = foodName, foodDescription = foodDescription, foodCalorie = 123f, userId = currentUser.uid)
        document.set(food)
            .addOnCompleteListener {
                result.invoke(
                    UiState.Success("Upload data makanan berhasil...")
                )
            }.addOnFailureListener {
                UiState.Success(it.localizedMessage.toString())
            }
    }

    override fun getFood(userId: String, result: (UiState<MutableList<Food>>) -> Unit) {
        val currUser = auth.currentUser
        val document = database.collection(Constants.FirestoreTable.FOOD)
        document.whereEqualTo(Constants.FireStoreDocumentField.USER_ID, currUser!!.uid).orderBy("date")
            .get()
            .addOnSuccessListener {
                val foodList: MutableList<Food> = arrayListOf()

                for (document in it) {
                    val food = document.toObject(Food::class.java)

                    foodList.add(food)
                }

                result.invoke(
                    UiState.Success(foodList)
                )
            }
            .addOnFailureListener {
                result.invoke(
                    UiState.failure(
                        it.localizedMessage.toString()
                    )
                )
            }



    }

    override fun editFood(FoodId: String, result: (UiState<String>) -> Unit) {
        TODO("Not yet implemented")
    }
}