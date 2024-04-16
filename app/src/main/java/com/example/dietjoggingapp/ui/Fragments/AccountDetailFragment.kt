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
import androidx.navigation.fragment.findNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.databinding.FragmentAccountDetailBinding
import com.example.dietjoggingapp.utility.Constants
import com.example.dietjoggingapp.utility.toast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [AccountDetailFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class AccountDetailFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var weight: Float = 0f
    private var calorie: Float = 0f
    private var tallInCm: Float = 0f
    private lateinit var email: String
    private lateinit var name: String
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var binding: FragmentAccountDetailBinding
    private val database = FirebaseFirestore.getInstance()
    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        val currUser = auth.currentUser
        if(currUser.toString().isNullOrEmpty()){
            toast("Anda belum login, Silahkan Login terlebih dahulu")
            findNavController().navigate(R.id.loginFragment)
        }

        initUserValue()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        initUserValue()
        binding = FragmentAccountDetailBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment AccountDetailFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            AccountDetailFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imgProfile.setOnClickListener{
            ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }
        var user = FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val document = database.collection(com.example.dietjoggingapp.other.Constants.FirestoreTable.USERS).document(user)
        document.get().addOnCompleteListener {
            var user = it.result.toObject(User::class.java)
            email = user!!.email
            name = user!!.fullName
            weight = user!!.weight
            calorie = user!!.dailyCalorie
            tallInCm = user!!.height

            binding.tvEmail.text = email
            binding.tvName.text = name
            binding.tvWeight.text = weight.toString().trim()
            binding.tvTallInCm.text = tallInCm.toString().trim()
            binding.tvTallInCm.text = tallInCm.toString().trim()
        }.addOnFailureListener {
            Log.d("TAG", "initUserValue: ${it.toString().trim()}")
        }

        binding.btnEditProfile.setOnClickListener {
            findNavController().navigate(R.id.EditFragment)
        }
    }

    fun initUserValue() {
//        We Already Logedin
        var user = FirebaseAuth.getInstance().currentUser!!.uid.toString()
        val document = database.collection(com.example.dietjoggingapp.other.Constants.FirestoreTable.USERS).document(user)
        document.get().addOnCompleteListener {
            var user = it.result.toObject(User::class.java)
            email = user!!.email
            name = user!!.fullName
            weight = user!!.weight
            calorie = user!!.dailyCalorie
            tallInCm = user!!.height
        }.addOnFailureListener{
            Log.d("TAG", "initUserValue: ${it.toString().trim()}")
        }
    }

    private fun initCloudStorage(uri: Uri) {
        var user = FirebaseAuth.getInstance().currentUser!!
        var ref: FirebaseStorage = FirebaseStorage.getInstance()

        var storage = ref.getReference("photo/users/${user.uid.toString()}.png")
        storage.putFile(uri).addOnCompleteListener {
            Log.d("TAG", "initCloudStorage: ${user.uid.toString().trim()}")
            Toast.makeText(requireActivity().applicationContext, "Photo has already uploadad", Toast.LENGTH_SHORT)
        }.addOnFailureListener{
            Toast.makeText(requireActivity().applicationContext, it.toString().trim(), Toast.LENGTH_SHORT)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode == Activity.RESULT_OK) {
            val uri: Uri = data?.data!!
//            User uri to take the image
            binding.imgProfile.setImageURI(uri)
            initCloudStorage(uri)
        }else if(resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireActivity().applicationContext, ImagePicker.getError(data), Toast.LENGTH_SHORT)
        }else {
            Toast.makeText(requireActivity().applicationContext, "Task Canceled", Toast.LENGTH_SHORT)
        }
    }
}