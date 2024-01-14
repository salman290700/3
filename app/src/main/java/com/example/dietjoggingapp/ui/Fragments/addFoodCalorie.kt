package com.example.dietjoggingapp.ui.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.databinding.FragmentAddFoodCalorieBinding
import com.example.dietjoggingapp.databinding.FragmentJoggingBinding

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [addFoodCalorie.newInstance] factory method to
 * create an instance of this fragment.
 */
class addFoodCalorie : Fragment() {
    private lateinit var binding: FragmentAddFoodCalorieBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAddFoodCalorieBinding.inflate(inflater, container, false)
        return binding.root
    }



}