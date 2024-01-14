package com.example.dietjoggingapp.ui.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.databinding.FragmentRegisterBinding
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.ui.viewmodels.AuthViewModel
import com.example.dietjoggingapp.utility.hide
import com.example.dietjoggingapp.utility.show
import com.example.dietjoggingapp.utility.toast
import dagger.hilt.android.AndroidEntryPoint
import java.util.*

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RegisterFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class RegisterFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentRegisterBinding
    val viewModel: AuthViewModel by viewModels()
    private lateinit var name: String
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var confPassword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentRegisterBinding.inflate(layoutInflater, container, false)
        // Inflate the layout for this fragment
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment RegisterFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observer()
        binding.tvLogin.setOnClickListener{
            findNavController().navigate(R.id.navigation_to_login)
        }

        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            if(validation()) {
                viewModel.registerUser(email = email,
                password = pass,
                user = getUserObj())
            }
        }


    }

    fun observer() {
        viewModel.register.observe(viewLifecycleOwner, Observer{state ->
            when(state) {
                is UiState.Success -> {
                    binding.progressCircular.hide()
                    findNavController().navigate(R.id.navigation_to_login)
                    toast(state.data)
                }
                is UiState.Loading -> {
                    binding.progressCircular.show()
                }
                is UiState.failure -> {
                    binding.progressCircular.hide()
                    toast(state.error)
                    binding.btnRegister.text = ""
                }
            }
        })
    }

    fun getUserObj(): User {
        val name = binding.etName.text.toString()
        val email = binding.etEmail.text.toString()
        val weight = binding.etWeight.text.toString().toFloat()
        val height = binding.etHeight.text.toString().toFloat()
        val age = binding.etAge.text.toString().toFloat()
        return User(
            userId = "",
            fullName = name,
            email = email,
            date = Date(),
            weight = weight,
            height = height,
            age = age,
            bmr = dailyCalorie()
        )
    }

    private fun dailyCalorie(): Float {
        val weight = binding.etWeight.text.toString().toFloat()
        val height = binding.etHeight.text.toString().toFloat()
        val age = binding.etAge.text.toString().toFloat()
        var gender: String = ""

        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == R.id.rbMale) {
                gender = "Male"
            } else {
                gender = "female"
            }
        }

        var bmr = 0.0f

        if(gender == "Male") {
            bmr = (10.0f * weight + 6.25f * height - 5.0f * age + 5.0f).toFloat()
        } else {
            bmr = (10.0f * weight + 6.25f * height - 5.0f * age - 161.0f).toFloat()
        }

        return bmr
    }

    fun validation(): Boolean {
        var isValid = true
        name = binding.etName.text.toString()
        email = binding.etEmail.text.toString()
        password = binding.etPassword.text.toString()
        confPassword = binding.etConfPassword.text.toString()

        if (email.isNullOrEmpty()){
            isValid = false
            toast(getString(R.string.enter_email))
        }
        if (name.isNullOrEmpty()){
            isValid = false
            toast(getString(R.string.enter_fullname))
        }
        if (password.isNullOrEmpty()){
            isValid = false
            toast(getString(R.string.enter_pass))
        }
        if (confPassword.isNullOrEmpty()){
            isValid = false
            toast("Please enter Confirm Password")
        }
        if (confPassword != password ) {
            isValid = true
            toast("Password & Confirm Password not identic")
        }
        if (password?.length!! < 6){
            isValid = false
            toast(getString(R.string.longer_pass))
        }
        return isValid
    }
}