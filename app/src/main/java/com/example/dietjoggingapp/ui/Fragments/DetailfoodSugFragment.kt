package com.example.dietjoggingapp.ui.Fragments

import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.Ingredients
import com.example.dietjoggingapp.database.foodsugser
import com.example.dietjoggingapp.databinding.FragmentDetailfoodSugBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

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
        getData()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun getData() {
        val bundle = Bundle()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
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

}