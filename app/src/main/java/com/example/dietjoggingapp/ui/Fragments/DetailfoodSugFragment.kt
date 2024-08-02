package com.example.dietjoggingapp.ui.Fragments

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.Ingredients
import com.example.dietjoggingapp.database.domains.Step
import com.example.dietjoggingapp.database.foodsugser
import com.example.dietjoggingapp.databinding.FragmentDetailfoodSugBinding
import com.google.api.Distribution.BucketOptions.Linear

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [DetailfoodSugFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class DetailfoodSugFragment : Fragment() {
    private lateinit var binding: FragmentDetailfoodSugBinding
    private lateinit var foodSuggest: foodsugser
    var ingredient = Ingredients()
    var ingr = ""
    private var steps: MutableList<Step> = mutableListOf()
    var Step = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentDetailfoodSugBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


//        var bundle = Bundle()
//        foodSuggest = arguments?.getParcelable("foodSuggest")!!
////        foodSuggest = bundle.getParcelable("foodSuggest", foodsugser::class.java)!!
//        Log.d("TAG", "getFoodSuggest: ${foodSuggest.name.toString().trim()}")
        getFoodSuggest()

        binding.tvfoodname.text = foodSuggest.name
        binding.tvfooddesc.text = foodSuggest.desc
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getData() {
        val bundle = Bundle()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val food = bundle.getParcelable("foodSuggest", foodsugser::class.java)
            binding.tvfoodname.text = food?.name
            binding.tvfooddesc.text = food?.desc

            for (i in 0..food?.ingredients!!.size) {
                ingredient = food.ingredients.get(i)
                ingr = ingr + "\n- ${ingredient.toString()}\n"
            }

            binding.tvfoodIngredients.text = ingr
            var steps = food.steps
            var Step = ""
            for(i in 0..steps.size) {
                Step = "\n- ${steps.get(i).stepDesc}"
            }
            binding.tvfoodSteps.text = Step
        }else {
            val food = bundle.getParcelable("foodSuggest", foodsugser::class.java)
            binding.tvfoodname.text = food?.name
            binding.tvfooddesc.text = food?.desc
            var ingredient = Ingredients()
            var ingr = ""
            for (i in 0..food?.ingredients!!.size) {
                ingredient = food.ingredients.get(i)
                ingr = ingr + "\n- ${ingredient.toString()}\n"
            }

            binding.tvfoodIngredients.text = ingr
            var steps = food.steps
            var Step = ""
            for(i in 0..steps.size) {
                Step = "\n- ${steps.get(i).stepDesc}"
            }
            binding.tvfoodSteps.text = Step
        }
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getFoodSuggest() {
        foodSuggest = arguments?.getParcelable("foodSuggest")!!
        var mainLayout = binding.mainLayout as LinearLayout
//        foodSuggest = bundle.getParcelable("foodSuggest", foodsugser::class.java)!!
        Log.d("TAG", "getFoodSuggest: ${foodSuggest.name.toString().trim()}")
        for (i in 0..foodSuggest?.ingredients!!.size-1) {
            ingredient = foodSuggest.ingredients.get(i)
            if (foodSuggest.ingredients.get(i).servingZiseInTbsp ==  null ||  foodSuggest.ingredients.get(i).servingZiseInTbsp ==  "") {
                ingr = ingr + "\n \u25CF ${ingredient.name} ${ingredient.servingSizeInGram.toString()} grams\n"
//                val tv: TextView = TextView(requireActivity().applicationContext)
//                tv.text = ingr
//                mainLayout.addView(tv)
            }else {
                ingr = ingr + "\n- ${ingredient.name} ${ingredient.servingZiseInTbsp}\n"
            }

        }
        steps = foodSuggest.steps

        for(i in 0..steps.size-1) {
            Step = Step + "\n- ${steps.get(i).stepDesc} "
        }
        binding.tvfoodname.text = foodSuggest.name
        binding.tvfooddesc.text = foodSuggest.desc
        binding.tvfoodIngredients.text = ingr
        binding.tvfoodSteps.text = Step
    }
}