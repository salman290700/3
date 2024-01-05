package com.example.dietjoggingapp.ui.Fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.databinding.FragmentJoggingBinding
import com.example.dietjoggingapp.databinding.FragmentSettingsBinding
import com.example.dietjoggingapp.other.Constants
import com.example.dietjoggingapp.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.dietjoggingapp.other.Constants.KEY_NAME
import com.example.dietjoggingapp.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment: Fragment(R.layout.fragment_settings) {
    private lateinit var binding: FragmentSettingsBinding

    @Inject
    lateinit var sharedPref: SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnApplyChanges.setOnClickListener {
            val success = applyChangesToSharedPref()
            if(success) {
                Snackbar.make(
                    view,
                    "Saved changes",
                    Snackbar.LENGTH_LONG
                )
            }else {
                Snackbar.make(
                    view,
                    "Please fill all the fields",
                    Snackbar.LENGTH_LONG
                )
            }
        }
    }

    private fun loadFieldsFromSharedPref() {
        val name = sharedPref.getString(KEY_NAME, "")
        val weight = sharedPref.getFloat(KEY_WEIGHT, 80f)
        binding.etName.setText(name)
        binding.etWeight.setText(weight.toString())

    }
    
    private fun applyChangesToSharedPref(): Boolean {
        val nameText = binding.etName.text.toString()
        val weightText = binding.etWeight.text.toString()

        if(nameText.isEmpty() || weightText.isEmpty()) {
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, nameText)
            .putFloat(KEY_WEIGHT, weightText.toFloat())
            .apply()
        val toolbarText = "Let's Go $nameText"
        requireActivity().findViewById<TextView>(R.id.tvToolbarTitle).text = toolbarText
        return true
    }
}