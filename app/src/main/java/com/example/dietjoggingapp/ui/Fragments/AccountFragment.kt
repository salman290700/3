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
import com.example.dietjoggingapp.databinding.FragmentAccountBinding
import com.example.dietjoggingapp.other.Constants
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.ui.LoginActivity
import com.example.dietjoggingapp.ui.viewmodels.AuthViewModel
import com.example.dietjoggingapp.utility.hide
import com.example.dietjoggingapp.utility.show
import com.example.dietjoggingapp.utility.toast
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountFragment : Fragment() {
    private lateinit var binding: FragmentAccountBinding
    val viewModel: AuthViewModel by viewModels()

    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    fun observer() {
        viewModel.logout.observe(viewLifecycleOwner, Observer {state ->
            when(state) {
                is UiState.Success -> {
                    binding.progressCircular.hide()
                    toast(state.data)
                    binding.btnLogout.hide()

                }
                is UiState.failure -> {
                    binding.progressCircular.hide()
                    toast(state.error)
                }
                is UiState.Loading -> {
                    binding.progressCircular.show()
                }
            }
        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.btnAccount.setOnClickListener {
            findNavController().navigate(R.id.AccountDetailFragment)
        }
        binding.btnLogout.setOnClickListener {

            val firebaseAuth = FirebaseAuth.getInstance()

            viewModel.logout()
            Log.d("TAG", "onViewCreated: viewModel" + firebaseAuth.currentUser?.uid.toString().trim())
            firebaseAuth.signOut()
            Log.d("TAG", "onViewCreated: " + firebaseAuth.currentUser?.uid.toString().trim())
            activity?.run {
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
            }
        }
        observer()
    }
}