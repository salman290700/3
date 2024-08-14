package com.example.dietjoggingapp.ui.Fragments

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.adapters.FoodSuggestAdapter
import com.example.dietjoggingapp.database.Fooddiet
import com.example.dietjoggingapp.database.Ingredients
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.database.domains.FoodSuggest
import com.example.dietjoggingapp.database.foodsugser
import com.example.dietjoggingapp.databinding.FragmentFoodSuggestBinding
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.ui.LoginActivity
import com.example.dietjoggingapp.ui.MainActivity
import com.example.dietjoggingapp.ui.viewmodels.FoodSuggestViewModel
import com.example.dietjoggingapp.utility.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt


@AndroidEntryPoint
class FoodSuggestFragment : Fragment(R.layout.fragment_food_suggest), FoodSuggestAdapter.RecyclerViewClickListener {
    private lateinit var binding: FragmentFoodSuggestBinding
    private val viewModel: FoodSuggestViewModel by viewModels()
    private lateinit var foodSuggestadapter: FoodSuggestAdapter
    private val TAG: String = "Food Suggest Fragment"



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFoodSuggestBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        foodSuggestadapter = FoodSuggestAdapter()



        Log.d("TAG", "onCreate: Create Food Page")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val url = "https://low-carb-recipes.p.rapidapi.com/search?tags=low-carb&maxAddedSugar=0&limit=5"
        val context = activity?.baseContext
        val user = FirebaseAuth.getInstance().currentUser?.email.toString()
        val database = FirebaseFirestore.getInstance()
        var maxCalPerServe = 0.0f
        database.collection("USERS").document(user).get()
            .addOnCompleteListener {
                val userData = it.result.toObject(User::class.java)

                if(userData != null) {
                    database.collection("FOODDIET").document(user).get()
                        .addOnCompleteListener {
                            val foodDiet = it.result.toObject(Fooddiet::class.java)
                            if(foodDiet == null || foodDiet?.calorie?.roundToInt() == 0) {
                                maxCalPerServe = userData.maxCalPerServe

                                viewModel.getFoodSuggest(context!!, url, maxCalPerServe, userData?.bmi!!)
                                val rvFood = binding.rvFoodSuggest

                                binding.rvFoodSuggest.adapter = foodSuggestadapter
                                viewModel.getFoodSuggest.observe(viewLifecycleOwner, Observer { state ->
                                    when(state){
                                        is UiState.Loading ->{

                                        }
                                        is UiState.failure -> {
                                            Log.d("TAG", "onViewCreated: ${state.error}")
                                            Log.d("TAG", "onViewCreated: ${state.error}")
                                            toast(state.error)
                                            findNavController().navigate(R.id.action_home)
                                        }
                                        is UiState.Success -> {
                                            foodSuggestadapter.submitList(state.data.toMutableList())
                                            Log.d(TAG, "onViewCreated: ${state.data.get(0).toString().trim()}")
                                        }
                                    }
                                })

                                foodSuggestadapter.listener = this
                            }else {
                                maxCalPerServe = userData?.bmr!! - foodDiet.calorie!!.toFloat()
                                if (maxCalPerServe <= 0) {
                                    toast("Anda tidak bisa makan lagi")
                                    val intent = Intent(requireActivity(), MainActivity::class.java)
                                    startActivity(intent)
                                }
                                viewModel.getFoodSuggest(context!!, url, maxCalPerServe, userData?.bmi!!)
                                val rvFood = binding.rvFoodSuggest

                                binding.rvFoodSuggest.adapter = foodSuggestadapter
                                viewModel.getFoodSuggest.observe(viewLifecycleOwner, Observer { state ->
                                    when(state){
                                        is UiState.Loading ->{

                                        }
                                        is UiState.failure -> {
                                            Log.d("TAG", "onViewCreated: ${state.error}")
                                            toast(state.error)
                                            findNavController().navigate(R.id.action_home)
                                        }
                                        is UiState.Success -> {
                                            foodSuggestadapter.submitList(state.data.toMutableList())
                                            Log.d(TAG, "onViewCreated: ${state.data.get(0).toString().trim()}")
                                        }
                                    }
                                })

                                foodSuggestadapter.listener = this
                            }
                        }
                }
            }.addOnFailureListener {
                toast("${it.localizedMessage.toString()}")
                toast("${it.message.toString()}")
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
            }
        Log.d("TAG", "onViewCreated foodrec: ${maxCalPerServe}")

    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onItemClicked(view: View, foodSuggest: FoodSuggest) {
        val name = foodSuggest.name
        val desc = foodSuggest.description
        val image = foodSuggest.image
        Log.d(TAG, "onItemClicked: ${name.toString().trim()}")
        var ingredients = mutableListOf<Ingredients>()
        for (i in 0..foodSuggest.ingredients.size-1) {
            var ingredient = Ingredients()
            if (foodSuggest.ingredients.get(i).servingSize.grams == null) {
                ingredient = Ingredients(foodSuggest.ingredients.get(i).name, 0f,foodSuggest.ingredients.get(i).servingSize.desc)
            } else {
                ingredient = Ingredients(foodSuggest.ingredients.get(i).name, foodSuggest.ingredients.get(i).servingSize.grams)
            }
            ingredients.add(i, ingredient)
            Log.d("TAG", "onItemClicked: ${ingredient.toString().trim()}")
        }
        Log.d(TAG, "onItemClicked: ${foodSuggest.nutrients.caloriesKCal}")

        val foodSuggestSer = foodsugser(foodSuggest.id, name, desc, foodSuggest.steps, image, ingredients, foodSuggest.nutrients.caloriesKCal!!)
        val bundle = Bundle()
        bundle.putParcelable("foodSuggest", foodSuggestSer)
        val fragment = DetailfoodSugFragment()
        fragment.arguments = bundle
        findNavController().navigate(R.id.action_detailFoodSug, bundle)
    }
}
