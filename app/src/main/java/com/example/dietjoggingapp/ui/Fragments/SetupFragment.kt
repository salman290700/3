package com.example.dietjoggingapp.ui.Fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.fragment.findNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.databinding.FragmentSetupBinding
import com.example.dietjoggingapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.dietjoggingapp.other.Constants.KEY_NAME
import com.example.dietjoggingapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SetupFragment: Fragment(R.layout.fragment_setup) {
    @Inject
    lateinit var  sharedPref: SharedPreferences

    @set:Inject
    var isFirstAppOpen = true

//    private val toolbar: TextView = requireActivity().findViewById(R.id.tvToolbarTitle) as TextView
    private lateinit var binding: FragmentSetupBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSetupBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!isFirstAppOpen) {
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.setUpFragment, true)
                .build()
            findNavController().navigate(
                R.id.action_setUpFragment_to_joggingFragments,
                savedInstanceState,
                navOptions
            )
        }

        binding.tvContinue.setOnClickListener{
            val success = writePersonalDataToSharedPref()
            if(success) {
                findNavController().navigate(R.id.action_setUpFragment_to_joggingFragments)
            }else {
                Snackbar.make(requireView(), "Please Enter ALl the Fields", Snackbar.LENGTH_SHORT).show()
            }
//            findNavController().navigate(R.id.action_setUpFragment_to_joggingFragments)
        }
    }

    private fun saveDataToFirestore(name: String, age: String) {

    }

    private fun writePersonalDataToSharedPref():Boolean {
        val name = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString()
        if(name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, false)
            .apply()
        val toolbarText = "Let's Go, $name"
        return true
    }
}