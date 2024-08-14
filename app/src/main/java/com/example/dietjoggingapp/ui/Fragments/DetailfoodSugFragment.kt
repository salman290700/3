package com.example.dietjoggingapp.ui.Fragments

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.navigation.fragment.findNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.Fooddiet
import com.example.dietjoggingapp.database.Ingredients
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.database.domains.Step
import com.example.dietjoggingapp.database.foodsugser
import com.example.dietjoggingapp.databinding.FragmentDetailfoodSugBinding
import com.example.dietjoggingapp.utility.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

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
    val auth = FirebaseAuth.getInstance().currentUser?.email.toString()
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
        happyDiet()
    }

    private fun happyDiet() {
        val database = FirebaseFirestore.getInstance()
        database.collection("USERS").document(auth).get()
            .addOnCompleteListener {
                val user = it.result.toObject(User::class.java)
                val bmr = user?.bmr!!.toFloat()
                database.collection("FOODDIET").document(auth).get()
                    .addOnCompleteListener {

                        if(it.result.toObject(Fooddiet::class.java) == null) {
                            val fodiet = Fooddiet(user.email, 0.0f)
                            database.collection("FOODDIET").document(auth).set(fodiet)
                        }else {
                            val fooddiet = it.result.toObject(Fooddiet::class.java)
                            Log.d("TAG", "happyDiet: ${fooddiet!!.calorie}")
                            val cal  = fooddiet?.calorie!! + foodSuggest?.calorie!!.toFloat()
                            if(cal!! >= bmr) {
                                binding.btnPilih.visibility = View.INVISIBLE
                            }
                        }
                    }
            }
        binding.btnPilih.setOnClickListener {
            updateFoodDiet()
        }
    }

    private fun updateFoodDiet() {
        val auth = FirebaseAuth.getInstance().currentUser?.email.toString()
        val database = FirebaseFirestore.getInstance()
        val calendar = Calendar.getInstance()
        val current = calendar.get(Calendar.HOUR_OF_DAY)
        var  string = ""
        if(current >= 0 && current <= 12) {
            string = "makan pagi"
        } else if(current >= 12 && current <= 18) {
            string = "makan siang"
        } else {
            string = "makan malam"
        }
        toast("Selamat diet ${string} anda berhasil")
        val foodDiet = Fooddiet(auth, foodSuggest.calorie)
        database.collection("FOODDIET").document(auth).get()
            .addOnCompleteListener {
                if(it.result.toObject(Fooddiet::class.java) == null) {
                    database.collection("FOODDIET").document(auth).set(foodDiet)
                        .addOnFailureListener {
                            toast("error : :${it.localizedMessage.toString()}")
                            toast("error : :${it.message.toString()}")
                        }
                } else {
                    var food = it.result.toObject(Fooddiet::class.java)
                    if(food?.date == "${Date().year}-${Date().month}-${Date().date}") {
                        food.calorie = foodDiet.calorie!! + (food.calorie!!)
                        Log.d("TAG", "updateFoodDietCalorie: ${food.calorie}")
                        database.collection("FOODDIET").document(auth).update("calorie", food.calorie)
                            .addOnCompleteListener { 
                                if (it.result != null) {
                                    Log.d("TAG", "updateFoodDiet: ${it.result.toString()}")
                                    Log.d("TAG", "updateFoodDiet: ${food.calorie}")
                                }
                            }
                            .addOnFailureListener {
                                toast("error : :${it.localizedMessage.toString()}")
                                toast("error : :${it.message.toString()}")
                            }
                    }else {
                        database.collection("FOODDIET").document(auth).set(foodDiet).addOnFailureListener {
                            toast(it.localizedMessage.toString())
                            toast(it.message.toString())
                        }
                    }
                }
                findNavController().navigate(R.id.action_home)
            }
            .addOnFailureListener {
                toast("error : :${it.localizedMessage.toString()}")
                toast("error : :${it.message.toString()}")
            }

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