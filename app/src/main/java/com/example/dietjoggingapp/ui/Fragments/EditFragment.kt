package com.example.dietjoggingapp.ui.Fragments

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBindings
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.databinding.FragmentAccountDetailBinding
import com.example.dietjoggingapp.databinding.FragmentEditBinding

import com.example.dietjoggingapp.other.Constants
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EditFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EditFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var binding: FragmentEditBinding
    private var access = false

    private var weight: Float = 0f
    private var calorie: Float = 0f
    private var tallInCm: Float = 0f
    private lateinit var email: String
    private lateinit var name: String
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseFirestore.getInstance()
    private lateinit var user: User

    val current_date = SimpleDateFormat("dd").format(System.currentTimeMillis()).toInt()
    val current_month = SimpleDateFormat("MM").format(System.currentTimeMillis()).toInt()
    val current_year = SimpleDateFormat("yyy").format(System.currentTimeMillis()).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentEditBinding.inflate(layoutInflater, container, false)
        init()
        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EditFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EditFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    fun age(year: Int, Month: Int, dayOfMonth: Int): String {
        var modCurrentDate = current_date
        var modCurrentMonth = current_month
        var modCurrentYear = current_year

        val ageDay: Int
        val ageMonth: Int
        val ageYear: Int

        if (dayOfMonth > modCurrentDate) {
            modCurrentDate += 30
            modCurrentMonth -= 1

            ageDay = modCurrentDate - dayOfMonth
        }else {
            ageDay = modCurrentDate - dayOfMonth
        }

        if (Month > modCurrentMonth) {
            modCurrentMonth += 12
            modCurrentYear -= 1

            ageMonth = modCurrentMonth - Month
        }else {
            ageMonth = modCurrentMonth - Month
        }

        ageYear = modCurrentYear - year
        return ageYear.toString() +  "years"
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.imgProfile.setOnClickListener {
            ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }

        var user = FirebaseAuth.getInstance().currentUser!!.uid.toString()

//        binding.add



        val document = database.collection(com.example.dietjoggingapp.other.Constants.FirestoreTable.USERS).document(user)
        document.get().addOnCompleteListener {
            var user = it.result.toObject(User::class.java)
            email = user!!.email
            name = user!!.fullName
            weight = user!!.weight
            calorie = user!!.dailyCalorie
            tallInCm = user!!.height

            val datePickerDialog = DatePickerDialog(requireActivity()!!.applicationContext, DatePickerDialog.OnDateSetListener{ view, year, month, date ->
//                textViewBirthDate.text = age(year, month, date)
            }, current_year, current_month, current_date)

            binding.tvEmail.text = email.toString().trim()
            binding.tvEmail.setTextColor(Color.WHITE)
            binding.tvName.text = name
            binding.tvName.setTextColor(Color.WHITE)

            binding.etWeight.setText(weight.toString().trim())

            binding.etHeight.setText(tallInCm.toString().trim())
            binding.etBirthDate.setOnClickListener {
                datePickerDialog.show()
            }
            binding.etBirthDate.setText("${user.birthDate.toString().trim()}-${user.birthMonth.toString().trim()}-${user.birthYear.toString().trim()}")
        }.addOnFailureListener {
            Log.d("TAG", "initUserValue: ${it.toString().trim()}")
        }

        val current_date = SimpleDateFormat("dd").format(System.currentTimeMillis()).toInt()
        val current_month = SimpleDateFormat("MM").format(System.currentTimeMillis()).toInt()
        val current_year = SimpleDateFormat("yyy").format(System.currentTimeMillis()).toInt()

        val ageDay: Int
        val ageMonth: Int
        val ageYear: Int

        val datePickerDialog = DatePickerDialog(requireActivity()!!.applicationContext, DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->  },
            current_year, current_month, current_date)
        binding.etBirthDate.setOnClickListener {
            datePickerDialog.show()
        }

        ageDay = datePickerDialog.datePicker.dayOfMonth
        ageMonth = datePickerDialog.datePicker.month
        ageYear = datePickerDialog.datePicker.year

        binding.btnSaveProfile.setOnClickListener {
            weight = binding.etWeight.text.toString().toFloat()
            tallInCm = binding.etHeight.text.toString().toFloat()


            val document = database.collection(com.example.dietjoggingapp.other.Constants.FirestoreTable.USERS).document(user)
            document.get().addOnCompleteListener {
                var user: User = it?.result!!.toObject(User::class.java)!!
                var gender = user.gender.toString().trim()
                var age = user.age
                if(gender == "Male") {
                    calorie = (10.0f * weight + 6.25f * tallInCm - 5.0f * age + 5.0f).toFloat()
                } else {
                    calorie = (10.0f * weight + 6.25f * tallInCm - 5.0f * age - 161.0f).toFloat()
                }

                val doc_user = database.collection(Constants.FirestoreTable.USERS).document(user.userId)

                if (ageDay.toString().isEmpty() && ageMonth.toString().isEmpty() && ageYear.toString().isEmpty()) {
                    doc_user.update(
                        "dailyCalori", calorie,
                        "height", tallInCm,
                        "weight", weight
                    ).addOnFailureListener {
                        Log.d("TAG", "initUserValue: ${it.toString().trim()}")
                    }.addOnCompleteListener {
                        findNavController().navigate(R.id.AccountDetailFragment)
                    }
                }else {
                    doc_user.update(
                        "dailyCalori", calorie,
                        "birthDate", ageDay.toString(),
                        "birthMonth", ageMonth.toString(),
                        "birthYear", ageYear.toString(),
                        "height", tallInCm,
                        "weight", weight
                    )
                }.addOnFailureListener {
                    Log.d("TAG", "initUserValue: ${it.toString().trim()}")
                }.addOnCompleteListener {
                    findNavController().navigate(R.id.AccountDetailFragment)
                }
            }

        }
    }

    private fun init() {
//        We Already Logedin
        val user = FirebaseAuth.getInstance().currentUser

        user.let {
            var userId = it!!.uid
            if(userId.isNotEmpty()) {
                access = true
            }else {
                access = false
                if(!access) {
                    findNavController().navigate(R.id.loginFragment)
                }
            }
        }


    }

    private fun initCloudStorage(uri: Uri) {
        var user = FirebaseAuth.getInstance().currentUser!!
        var ref: FirebaseStorage = FirebaseStorage.getInstance()

        var storage = ref.getReference("photo/users/${user.uid.toString()}.png")
        storage.putFile(uri).addOnCompleteListener {
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