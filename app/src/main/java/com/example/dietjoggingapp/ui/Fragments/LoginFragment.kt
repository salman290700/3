package com.example.dietjoggingapp.ui.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.databinding.FragmentLoginBinding
import com.example.dietjoggingapp.databinding.FragmentRegisterBinding
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.ui.viewmodels.AuthViewModel
import com.example.dietjoggingapp.utility.hide
import com.example.dietjoggingapp.utility.show
import com.example.dietjoggingapp.utility.toast
import com.google.android.material.bottomnavigation.BottomNavigationItemView
import dagger.hilt.android.AndroidEntryPoint

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */

@AndroidEntryPoint
class LoginFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentLoginBinding
    private lateinit var email: String
    private lateinit var password: String
    private lateinit var name: String
    private val authViewModel: AuthViewModel by viewModels()

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
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
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
         * @return A new instance of fragment LoginFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            LoginFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        observer()
        binding.btnLogin.setOnClickListener {
            email = binding.etEmail.text.toString()
            password = binding.etPassword.text.toString()
            if(validation()) {
                authViewModel.login(email, password)
                findNavController().navigate(R.id.navigate_to_jogging_fragment)
            }else {
                Log.d("TAG", "validation: LoginFalse")
            }
        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.navigate_to_register_fragment)
        }
    }

    fun observer() {
        authViewModel.login.observe(viewLifecycleOwner, Observer {state ->
            when(state) {
                is UiState.Success -> {
                    binding.progressCircular.hide()
                    toast(state.data)
                    binding.btnLogin.hide()
                    findNavController().navigate(R.id.navigate_to_jogging_fragment)
                }
                is UiState.failure -> {
                    binding.progressCircular.hide()
                    toast(state.error)
                    binding.btnLogin.setText("Sign In")
                }
                is UiState.Loading -> {
                    binding.progressCircular.show()
                    binding.btnLogin.setText("")
                }
            }

        })
    }

    fun validation(): Boolean {
        var isvalid = true
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()

        if(email.isNullOrEmpty() || password.isNullOrEmpty()) {
            toast("Please fill email & password up..!")
            isvalid = false
            Log.d("TAG", "validation: LoginFalse")
        }
        else if(password.length < 6) {
            toast("Please enter minimum 6 Characters password")
            isvalid = false
            Log.d("TAG", "validation: LoginFalse")
        }
        return isvalid
    }


}