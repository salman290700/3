package com.example.dietjoggingapp.ui.Fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.adapters.FoodSuggestAdapter
import com.example.dietjoggingapp.database.Ingredients
import com.example.dietjoggingapp.database.domains.FoodSuggest
import com.example.dietjoggingapp.database.foodsugser
import com.example.dietjoggingapp.databinding.FragmentFoodSuggestBinding
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.ui.viewmodels.FoodSuggestViewModel
import dagger.hilt.android.AndroidEntryPoint


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
        viewModel.getFoodSuggest(context!!, url)
        val rvFood = binding.rvFoodSuggest

        binding.rvFoodSuggest.adapter = foodSuggestadapter
        viewModel.getFoodSuggest.observe(viewLifecycleOwner, Observer { state ->
            when(state){
                is UiState.Loading ->{

                }
                is UiState.failure -> {
                    Log.d("TAG", "onViewCreated: ${state.error}")
                }
                is UiState.Success -> {
                    foodSuggestadapter.submitList(state.data.toMutableList())
                    Log.d(TAG, "onViewCreated: ${state.data.get(0).toString().trim()}")
                }
            }
        })

        foodSuggestadapter.listener = this
    }

    override fun onItemClicked(view: View, foodSuggest: FoodSuggest) {
        val name = foodSuggest.name
        val desc = foodSuggest.description
        val image = foodSuggest.image
        var ingredients = mutableListOf<Ingredients>()

        for (i in 0..foodSuggest.ingredients.size) {
            var ingredient = Ingredients(foodSuggest.ingredients.get(i).name, foodSuggest.ingredients.get(i).servingSize.grams)
            ingredients.add(i, ingredient)
        }

        val foodSuggestSer = foodsugser(foodSuggest.id, name, desc, foodSuggest.tags, foodSuggest.steps, image, ingredients)
        val bundle = Bundle()
        bundle.putParcelable("foodSuggest", foodSuggestSer)
    }
}
