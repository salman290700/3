package com.example.dietjoggingapp.ui.Fragments

import android.app.Activity
import android.app.DatePickerDialog
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
import com.example.dietjoggingapp.databinding.FragmentEditBinding

import com.example.dietjoggingapp.other.registerUtils
import com.example.dietjoggingapp.utility.toast
import com.example.dietjoggingapp.utility.utils
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.math.RoundingMode
import java.text.SimpleDateFormat
import kotlin.math.roundToInt

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
    private var gender = ""

    val current_date = SimpleDateFormat("dd").format(System.currentTimeMillis()).toInt()
    val current_month = SimpleDateFormat("MM").format(System.currentTimeMillis()).toInt()
    val current_year = SimpleDateFormat("yyy").format(System.currentTimeMillis()).toInt()
    var ageDay: Int = 0
    var ageMonth: Int = 0
    var ageYear: Int = 0
    private var age: Int = 0

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
        email = auth
        database.collection("USERS").document(email).get()
            .addOnCompleteListener {
                if (it.isSuccessful) {
                    user = it.result.toObject(User::class.java)!!
                    binding.etEmail.setText(user.email)
                    binding.etName.setText(user.fullName)
                    binding.etHeight.setText((user.height * 100).roundToInt().toString())
                    binding.etWeight.setText(user.weight.toString())
                    ageDay = user.birthDate
                    ageMonth = user.birthMonth
                    ageYear = user.birthYear
                    binding.etBirthDate.setText("${ageYear}-${ageMonth}-${ageDay}")
                    if (user.gender == "male") {
                        binding.rbMale.isChecked = true
                    }else {
                        binding.rbFemale.isChecked = true
                    }
//                    binding.etBirthDate.setText("${user.birthDate} - ${user.birthMonth} - ${user.birthYear}")
                    //update birthdate
                }
            }
            .addOnFailureListener {
                Log.d("TAG", "onViewCreated: ${it.localizedMessage.toString()}")
                Log.d("TAG", "onViewCreated: ${it.message.toString()}")
            }

        val datePickerDialog =
                DatePickerDialog(
                    requireContext(), DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                        ageDay = dayOfMonth
                        ageMonth = month
                        ageYear = year
                        var stringBD: String = "${ageYear} - ${ageMonth} - ${ageDay}"
                        binding.etBirthDate.setText(stringBD)
                    },
                    current_year, current_month, current_date)
        binding.etBirthDate.setOnClickListener {
            datePickerDialog?.show()
        }
        binding.save.setOnClickListener {
            save(ageDay, ageMonth, ageYear)
        }
    }

    private fun dailyCalorie(weight: Float, height: Float, age: Float):Float {
        var gender: String = ""

        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->

        }

        if(binding.rbMale.isChecked) {
            gender = "male"
        } else {
            gender = "female"
        }

        var bmr = 0.0f

        if(gender == "male") {
            bmr = 66 + 13.7f * (weight * 2.2f) + 5.0f * (height * 2.54f) - 6.78f * age
        } else {
            bmr = 66 + 9.6f * (weight * 2.2f) + 1.8f * (height * 2.54f) - 4.7f * age
        }
        return bmr
    }

    private fun maxWeight(height: Float) : Float {
        var maxWeight = 25 * (height * height)
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
        binding.rgGender.setOnCheckedChangeListener { group, checkedId ->

        }

        if(binding.rbMale.isChecked) {
            gender = "male"
        } else if(binding.rbFemale.isChecked){
            gender = "female"
        }
        val fullname = binding.etName.text.toString()
        val weight = binding.etWeight.text.toString().toFloat().toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
        val height = binding.etHeight.text.toString().toFloat().toBigDecimal().setScale(2, RoundingMode.UP).toFloat()/100
        val maxWeight: Float = maxWeight((binding.etHeight.text.toString().toFloat() / 100.00).toBigDecimal().setScale(2, RoundingMode.DOWN).toFloat())
        val ow: Float = ow(binding.etWeight.text.toString().toFloat(), binding.etHeight.text.toString().toFloat())
        age = registerUtils.ageInYear(ageYear, ageMonth, ageDay)

        var updateUser: HashMap<String, Any> = HashMap()
        var user = User()

        user.fullName = binding.etName.text.toString()
        user.weight = binding.etWeight.text.toString().toBigDecimal().setScale(2, RoundingMode.UP).toFloat()
        user.height = height
        user.bmi = utils.bmi(height, weight)
        user.age = age
        val bmr = utils.bmr(gender, weight, height * 100, age, utils.bmi(height, weight))
        user.bmr = bmr
        val dailyCalorie = utils.dailyCalorie(gender, weight, height * 100, age)
        user.dailyCalorie = dailyCalorie
        user.maxWeight = maxWeight
        user.overweight = ow
        user.gender = gender
        user.birthDate = ageDay
        user.birthMonth = ageMonth
        user.birthYear = ageYear
        user.maxCalPerServe = utils.maxCalPerServe(gender, weight, height * 100, age, user.bmi)
        updateUser.put("fullName", fullname)
        updateUser.put("weight", weight)
        updateUser.put("height", height)
        updateUser.put("age", age)
        updateUser.put("bmi", user.bmi)
        updateUser.put("bmr", bmr)
        updateUser.put("dailyCalorie", dailyCalorie)
        updateUser.put("maxWeight", maxWeight)
        updateUser.put("overweight", ow)
        Log.d("TAG", "save: ${gender}")
        updateUser.put("maxCalPerServe", user.maxCalPerServe)
        updateUser.put("gender", gender)
        Log.d("TAG", "save: ${gender}")
        updateUser.put("birthDate", ageDay)
        updateUser.put("birthMonth", ageMonth)
        updateUser.put("birthYear", ageYear)
        database.collection("USERS").document(email).update(updateUser)
            .addOnCompleteListener {
                if(it.isSuccessful) {
                    toast("Update Profile Berhasil")
                    findNavController().navigate(R.id.action_home)
                }else {
                    toast("Update Profile Gagal")
                }
            }.addOnFailureListener {
                Log.d("TAG", "save: ${it.localizedMessage.toString().trim()}")
                Log.d("TAG", "save: ${it.message.toString().trim()}")
            }
//        database.collection("USERS").document(email).update(
//            "fullname", binding.etName.text,
//        "weight", binding.etWeight.text,
//        "height", binding.etHeight.text,
//        "age", age,
//        "bmr", bmr,
//        "maxWeight", maxWeight,
//        "overweight", ow,
//            "gender", gender,
//        "birthDate", ageDay,
//        "birthMonth", ageMonth,
//        "birthYear", ageYear)
//            .addOnCompleteListener {
//                if (it.isSuccessful) {
//                    toast("Edit Profile Berhasil")
//                } else {
//                    toast("Edit Profile Gagal")
//                }
//            }
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