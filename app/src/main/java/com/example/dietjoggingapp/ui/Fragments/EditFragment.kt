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
import com.example.dietjoggingapp.other.registerUtils
import com.example.dietjoggingapp.utility.toast
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    private var auth = FirebaseAuth.getInstance().currentUser?.email.toString()
    private val database = FirebaseFirestore.getInstance()
    private lateinit var user: User

    val current_date = SimpleDateFormat("dd").format(System.currentTimeMillis()).toInt()
    val current_month = SimpleDateFormat("MM").format(System.currentTimeMillis()).toInt()
    val current_year = SimpleDateFormat("yyy").format(System.currentTimeMillis()).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
        binding.addPicture.setOnClickListener {
            ImagePicker.with(this)
                .crop()	    			//Crop image(Optional), Check Customization for more option
                .compress(1024)			//Final image size will be less than 1 MB(Optional)
                .maxResultSize(1080, 1080)	//Final image resolution will be less than 1080 x 1080(Optional)
                .start()
        }


//        binding.add
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
//        val datePickerDialog = DatePickerDialog(requireContext(), this, year, month, day)

        val datePickerDialog =
                DatePickerDialog(
                    requireContext(), DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->  },
                    current_year, current_month, current_date)
        binding.etBirthDate.setOnClickListener {
            datePickerDialog?.show()
        }
        email = auth
        database.collection("USERS").document(email).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    user = it.result.toObject(User::class.java)!!
                    binding.etEmail.setText(user.email)
                    binding.etEmail.setTextColor(Color.WHITE)
                    binding.etName.setText(user.fullName)
                    binding.etName.setTextColor(Color.WHITE)
                    binding.etHeight.setText(user.height.toString())
                    binding.etWeight.setText(user.weight.toString())
//                    binding.etBirthDate.setText("${user.birthDate} - ${user.birthMonth} - ${user.birthYear}")
                    //update birthdate
                }
            }
            .addOnFailureListener {
                Log.d("TAG", "onViewCreated: ${it.localizedMessage.toString()}")
                Log.d("TAG", "onViewCreated: ${it.message.toString()}")
            }
        var ageDay = datePickerDialog.datePicker.dayOfMonth
        var ageMonth = datePickerDialog.datePicker.month
        var ageYear = datePickerDialog.datePicker.year

        binding.save.setOnClickListener {
            save(ageDay, ageMonth, ageYear)
        }
    }

    private fun dailyCalorie(weight: Float, height: Float, age: Float):Float {
        var gender: String = ""

        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == R.id.rbMale) {
                gender = "male"
            } else {
                gender = "female"
            }
        }

        var bmr = 0.0f

        if(gender == "male") {
            bmr = 66 + 13.7f * weight + 5.0f * height - 6.78f * age
        } else {
            bmr = 66 + 9.6f * weight + 1.8f * height - 4.7f * age
        }
        return bmr
    }

    private fun maxWeight(height: Float) : Float {
        var maxWeight = 25 / (height * height)
        return maxWeight
    }

    private fun ow(weight: Float, height: Float): Float {
        val bmi = weight/(height*height)
        var ow = 0.0f
        if (bmi > 25) {
            val maxweight: Float = (user.height * user.height) / 25
            ow = maxweight - weight
        }
        return ow
    }


    fun save(ageDay: Int, ageMonth: Int, ageYear: Int) {
        val age = registerUtils.ageInYear(ageDay, ageMonth, ageYear)
        var gender = ""
        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->
            if(checkedId == R.id.rbMale) {
                gender = "male"
            } else {
                gender = "female"
            }
        }
        database.collection("USERS").document(email).update(
            "fullname", binding.etName.text,
        "weight", binding.etWeight.text,
        "height", binding.etHeight.text,
        "age", age,
        "bmr", dailyCalorie(binding.etWeight.text.toString().toFloat(), binding.etHeight.text.toString().toFloat(), age),
        "maxWeight", maxWeight(binding.etHeight.text.toString().toFloat()),
        "overweight",ow(binding.etWeight.text.toString().toFloat(), binding.etHeight.text.toString().toFloat()),
            "gender", gender,
        "birthDate", ageDay,
        "birthMonth", ageMonth,
        "birthYear", ageYear)
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    toast("Edit Profile Berhasil")
                } else {
                    toast("Edit Profile Gagal")
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
            binding.addPicture.setImageURI(uri)
            initCloudStorage(uri)
        }else if(resultCode == ImagePicker.RESULT_ERROR) {
            Toast.makeText(requireActivity().applicationContext, ImagePicker.getError(data), Toast.LENGTH_SHORT)
        }else {
            Toast.makeText(requireActivity().applicationContext, "Task Canceled", Toast.LENGTH_SHORT)
        }
    }

}