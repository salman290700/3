package com.example.dietjoggingapp.ui.Fragments

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.databinding.FragmentFoodBinding
import com.example.dietjoggingapp.other.Constants
import com.example.dietjoggingapp.ui.viewmodels.FoodViewModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint

/**
 * A simple [Fragment] subclass.
 * Use the [FoodFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
@AndroidEntryPoint
class FoodFragment : Fragment() {
        private lateinit var binding: FragmentFoodBinding
        val viewModel: FoodViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentFoodBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        Init View

        val rvFood = binding.rvRuns
        val addBtn = binding.fab
        val loading = binding.progressBar

//     floatAction Button
        addBtn.setOnClickListener {
            val intent = Intent(requireActivity().applicationContext, AddDetailFood::class.java)
            startActivity(intent)
        }
    }

    private fun addFoodToFirebase() {

    }

    private fun initCloudStorage(uri: Uri): String{

        var ref = FirebaseStorage.getInstance()
        val auth = FirebaseAuth.getInstance().currentUser
        var storage = ref.reference
        val imageFolder = storage.child("photo/${Constants.FirestoreTable.FOOD}/${auth!!.uid}/")

        imageFolder.putFile(uri).addOnCompleteListener {
            if (it.isSuccessful) {

            }
            imageFolder.downloadUrl
            Log.d("TAG", "initCloudStorage:")
            Toast.makeText(requireActivity().applicationContext, "Photo has already uploadad", Toast.LENGTH_SHORT)

        }.addOnFailureListener{
            Toast.makeText(requireActivity().applicationContext, it.toString().trim(), Toast.LENGTH_SHORT)
        }.addOnSuccessListener {
            Log.d("TAG", "initCloudStorage:")
        }
        return imageFolder.downloadUrl.toString()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data!!
//            User uri to take the image
            var imageLink = initCloudStorage(uri)
        }else if(resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireActivity().applicationContext, ImagePicker.getError(data), Toast.LENGTH_SHORT)
        }else {
            Toast.makeText(requireActivity().applicationContext, "Task Canceled", Toast.LENGTH_SHORT)
        }
    }
}