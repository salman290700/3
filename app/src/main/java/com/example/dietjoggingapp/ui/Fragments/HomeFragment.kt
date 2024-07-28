package com.example.dietjoggingapp.ui.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.DailyCalories
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.databinding.FragmentHomeBinding
import com.example.dietjoggingapp.other.registerUtils
import com.example.dietjoggingapp.ui.LoginActivity
import com.example.dietjoggingapp.utility.Constants
import com.example.dietjoggingapp.utility.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.math.RoundingMode
import kotlin.math.roundToInt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    val database = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance().currentUser
    private var joggingSuggestion: Float = 0f
    private lateinit var overCalorie: String
    private val email = auth?.email!!
    private lateinit var user: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (auth == null) {
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
        }
        binding.btnLetsJogging.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_trackingFragment)
        }

//        binding.btnAddFood.setOnClickListener {
//            val intent = Intent(requireActivity().applicationContext, AddDetailFood::class.java)
//            startActivity(intent)
//        }
        database.collection("USERS").document(email).get()
            .addOnCompleteListener {
                user = it.result.toObject(User::class.java)!!
                binding.tvOW.text = user?.overweight?.roundToInt().toString()
                binding.tvbmr.text = user?.bmr.toString()
                binding.tvWeight.text = user?.weight.toString().toFloat().roundToInt().toString()
                binding.tvAge.text = registerUtils.ageInYear(user.birthYear, user.birthMonth, user.birthDate).toString()
                binding.tvTallincm.text = (user?.height!! * 10.0f).toBigDecimal().setScale(1).toString()
                countOW()
                countJogSug()
            }.addOnFailureListener {
                Log.d("TAG", "onResume: ${it.message.toString()}")
                Log.d("TAG", "onResume: ${it.localizedMessage.toString()}")
            }
    }

    override fun onResume() {
        super.onResume()
        database.collection("USERS").document(email).get()
            .addOnCompleteListener {
                user = it.result.toObject(User::class.java)!!
                if(user?.overweight!! > 0.0) {
                    binding.tvOW.text = user?.overweight.toString()
                }else {
                    binding.tvOW.text = "Normal"
                    binding.tvblebih.text = "Berat badan ${user?.fullName!!.trim()}"
                }
                binding.tvbmr.text = user?.bmr.toString()
                binding.tvWeight.text = user?.weight.toString()
                binding.tvTallincm.text = (user?.height!! * 100.0f).toString()
                countOW()
                countJogSug()
            }.addOnFailureListener {
                Log.d("TAG", "onResume: ${it.message.toString()}")
                Log.d("TAG", "onResume: ${it.localizedMessage.toString()}")
            }
    }

    private fun maxWeight(height: Float) : Float {
        var maxWeight = 25 * (height * height)
        return maxWeight
    }

    private fun countOW() {

        if (user?.maxWeight!! > user?.weight!!) {
            binding.tvOW.text = "${(user?.weight!! - user?.maxWeight!!).toString()} KG"
        }else {
            binding.tvOW.text = "Normal"
            binding.tvblebih.text = "Berat badan ${user.fullName}"
        }
    }

    private fun countJogSug() {
        var dietCalorie = user?.bmr!!-1000
//        var K = dietCalorie * 20/100

        var K = dietCalorie
        var C = (2.8 * 7.7 * (user?.weight!! * 2.2))/200
        Log.d("TAG", "countJogSug: ${C}")
        var J = K/C
        binding.tvJogSug.text = "${J.toString()} Menit"

        if (user?.weight!! < user?.maxWeight!!) {
            binding.tvJogSug.text = "No Jogging"
        }else {
            binding.tvJogSug.text = "${J.roundToInt().toString().trim()} Menit"
        }
    }

//    private fun calculateJoggingSuggestionInKm(caloriesBurned: Float, weight: Float): Float {
//        var bmi =
//        val distanceInKm = caloriesBurned / weight
//        joggingSuggestion = distanceInKm
//        return joggingSuggestion
//    }

}
