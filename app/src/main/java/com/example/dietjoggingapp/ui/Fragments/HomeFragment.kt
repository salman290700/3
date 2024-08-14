package com.example.dietjoggingapp.ui.Fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.CalDef
import com.example.dietjoggingapp.database.DailyCalories
import com.example.dietjoggingapp.database.Fooddiet
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.databinding.FragmentHomeBinding
import com.example.dietjoggingapp.other.registerUtils
import com.example.dietjoggingapp.ui.LoginActivity
import com.example.dietjoggingapp.ui.viewmodels.MainViewModel
import com.example.dietjoggingapp.utility.Constants
import com.example.dietjoggingapp.utility.toast
import com.example.dietjoggingapp.utility.utils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.math.RoundingMode
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.roundToInt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    val database = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance().currentUser
    private var joggingSuggestion: Float = 0f
    private lateinit var overCalorie: String
    private val email = auth?.email!!
    private val viewModel: MainViewModel by viewModels()
    private lateinit var user: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (auth == null) {
            val intent = Intent(requireActivity(), LoginActivity::class.java)
            startActivity(intent)
        }
        binding.btnLetsJogging.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_trackingFragment)
        }

        getFooddiet()

//        binding.btnAddFood.setOnClickListener {
//            val intent = Intent(requireActivity().applicationContext, AddDetailFood::class.java)
//            startActivity(intent)
//        }
        val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        database.collection("USERS").document(email).get()
            .addOnCompleteListener {
                user = it.result.toObject(User::class.java)!!
                binding.tvOW.text = user?.overweight?.roundToInt().toString()
                binding.tvbmr.text = user?.bmr.toString()
                binding.tvWeight.text = user?.weight.toString().toFloat().roundToInt().toString()
                binding.tvAge.text = registerUtils.ageInYear(user.birthYear, user.birthMonth, user.birthDate).toString()
                binding.tvTallincm.text = (user?.height!! * 100.0f).roundToInt().toString()

//                BMI Indexing
                if (user?.bmi!! < 16) {
                    binding.tvOW.text = "Kurus Sekali"
                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
                } else if (user?.bmi!! >= 16 && user?.bmi!! < 17) {
                    binding.tvOW.text = "Kurus"
                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
                } else if (user?.bmi!! >= 17 && user?.bmi!! < 18.5) {
                    binding.tvOW.text = "Sedikit Kurus"
                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
                } else if (user?.bmi!! >= 18.5 && user?.bmi!! < 25) {
                    binding.tvOW.text = "Ideal"
                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
                } else if (user?.bmi!! >= 25 && user?.bmi!! < 30) {
                    binding.tvOW.text = "Berlebih"
                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
                } else if(user?.bmi!! >=30 && user?.bmi!! < 35) {
                    binding.tvOW.text = "Obesitas Kelas 1"
                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
                } else if(user?.bmi!! >=35 && user?.bmi!! < 40) {
                    binding.tvOW.text = "Obesitas Kelas 2"
                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
                } else {
                    binding.tvOW.text = "Obesitas Kelas 3"
                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
                }
                countOW()
                countJogSug()

                var CALDEF = CalDef()
                database.collection("CALDEF").document(user?.email.toString()).get()
                    .addOnCompleteListener {
                        CALDEF = it.result.toObject(CalDef::class.java)!!
                        var calDef = 0.0f
                        if(CALDEF == null) {
                            CALDEF = CalDef(user.email!!, 0.0f)
                            database.collection("CALDEF").document(user?.email.toString()).set(CALDEF)
                                .addOnCompleteListener {
                                    Log.d("TAG", "onViewCreated onCOmplete: ${it.result.toString()}")
                                }
                                .addOnFailureListener {
                                    Log.d("TAG", "onViewCreated: ${it.localizedMessage.toString()}")
                                    Log.d("TAG", "onViewCreated: ${it.message.toString()}")
                                }
                        } else {
                            database.collection("FOODDIET").document(user?.email.toString()).get()
                                .addOnCompleteListener {
                                    calDef = CALDEF.caldef
                                    val foodDiet = it.result.toObject(Fooddiet::class.java)
                                    if (foodDiet != null) {
                                        if(foodDiet?.date == "${Date().year}-${Date().month}-${Date().date}") {
                                            val calDefFood = user.dailyCalorie - foodDiet?.calorie!!.toFloat()
                                            binding.tvTargetKalori.text = "${(foodDiet.calorie!!).roundToInt().toString()} Kcal"
                                            var daysDiet = utils.countDietDays(user?.bmi!!, calDef, user.maxWeight, user.weight, calDefFood, user?.height!!, user?.bmr!!)
                                            Log.d("TAG", "onViewCreated: ${daysDiet}")
                                            println("onViewCreated: ${daysDiet}")
                                            var day = 0
                                            var month = 0
                                            var year = 0
                                            var tvDietText = ""
//                                            daysDiet(daysDiet.toFloat())
                                            if (daysDiet == 0 && user?.bmi!! >= 18.5 && user?.bmi!! < 25) {
                                                binding.tvDiet.text = "Anda tidak perlu diet"
                                                binding.tvDT.visibility = View.GONE
                                                if(user.bmi < 18.5) {
                                                    binding.tvDiet.text = "Anda perlu konsumsi makanan tinggi kalori"
                                                    binding.tvDT.visibility = View.GONE
                                                    binding.tvTarget.visibility = View.VISIBLE
                                                    binding.tvTargetKalori.visibility = View.GONE
                                                }else if(user?.bmi!! >= 18.5 && user?.bmi!! <25) {
                                                    binding.tvDiet.text = "Anda perlu konsumsi asupan \ngizi seimbang"
                                                    binding.tvDT.visibility = View.GONE
                                                    binding.tvTarget.visibility = View.VISIBLE
                                                    binding.tvTargetKalori.visibility = View.GONE
                                                }
                                            }else if(daysDiet >= 1 && user?.bmi!! < 18.5){
//                                                daysDiet(daysDiet.toFloat())
                                                day = daysDiet
                                                while(day >= 30) {
                                                    day -= 30
                                                    month += 1
                                                    while(month >= 12) {
                                                        month -= 12
                                                        year += 1
                                                    }
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${day} ${month} ${year}")
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                Log.d("TAG", "onViewCreated: Masuknya ke Atas")
                                                if(year > 0) {
                                                    tvDietText = "${year} tahun"
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                if(month > 0) {
                                                    tvDietText += " ${month} bulan"
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                if(day > 0) {
                                                    tvDietText += " ${day} hari"
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                binding.tvDiet.text = "$tvDietText"
//                                                binding.tvDiet.text = "${daysDiet} hari"
                                                binding.tvTargetKalori.text = "Anda perlu konsumsi makanan Tinggi Kalori"
                                                binding.tvDT.visibility = View.VISIBLE
                                                binding.tvDT.text = "Anda perlu melakukan Diet Selama"
                                                binding.tvTarget.visibility = View.VISIBLE
                                            }else {
//                                                daysDiet(daysDiet.toFloat())
                                                day = daysDiet
                                                while(day >= 30) {
                                                    day -= 30
                                                    month += 1
                                                    while(month >= 12) {
                                                        month -= 12
                                                        year += 1
                                                    }
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${day} ${month} ${year}")
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                Log.d("TAG", "onViewCreated: Masuknya ke Atas")
                                                if(year > 0) {
                                                    tvDietText = "${year} tahun"
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                if(month > 0) {
                                                    tvDietText += " ${month} bulan"
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                if(day > 0) {
                                                    tvDietText += " ${day} hari"
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                binding.tvDiet.text = "$tvDietText"
                                                Log.d("TAG", "onViewCreated: ${day} ${month} ${year}")
//                                                binding.tvDiet.text = "$tvDietText"
//                                                binding.tvDiet.text = "${daysDiet} Hari"
                                                Log.d("TAG", "onViewCreated pass: $daysDiet")
                                                binding.tvTargetKalori.text = "Anda perlu konsumsi makanan Rendah Karbohidrat"
                                                binding.tvDT.visibility = View.VISIBLE
                                                binding.tvTarget.visibility = View.VISIBLE
                                            }
//                                            Pendataan makanan diet
//                                            if (user?.bmi!! >= 25) {
//                                                binding.tvTarget.visibility = View.GONE
//                                                binding.tvTargetKalori.text = "Anda perlu melakukan Diet Karbohidrat"
//                                            }else if(user?.bmi!! >= 18.5 && user?.bmi!! < 25){
//                                                binding.tvTarget.visibility = View.GONE
//                                                binding.tvTargetKalori.visibility = View.GONE
//                                            } else {
//                                                binding.tvTarget.visibility = View.GONE
//                                                binding.tvTargetKalori.visibility = View.GONE
//                                            }

                                        }else {
                                            var daysDiet = utils.countDietDays(user?.bmi!!, calDef, user.maxWeight, user.weight, 0.0f, user?.height!!, user?.bmr!!, )
                                            if (daysDiet == 0 && user?.bmi!! >= 18.5 && user?.bmi!! < 25) {
//                                                binding.tvTargetKalori.text = "Anda tidak perlu diet"
                                                binding.tvDT.visibility = View.GONE
                                                binding.tvDiet.visibility = View.GONE
                                                if(user.bmi < 18.5) {
                                                    binding.tvDiet.visibility = View.VISIBLE
//                                                    binding.tvDiet.text = "Anda perlu konsumsi makanan tinggi kalori"
                                                    binding.tvDT.visibility = View.VISIBLE
                                                    binding.tvTarget.visibility = View.VISIBLE
//                                                    binding.tvTargetKalori.visibility = View.GONE
                                                    binding.tvTargetKalori.visibility = View.VISIBLE
                                                    binding.tvTargetKalori.text = "Anda perlu konsumsi asupan \ngizi seimbang"
                                                    var day = 0
                                                    var month = 0
                                                    var year = 0
                                                    var tvDietText = ""
                                                    day = daysDiet
                                                    while(day >= 30) {
                                                        day -= 30
                                                        month += 1
                                                        while(month >= 12) {
                                                            month -= 12
                                                            year += 1
                                                        }
                                                    }
                                                    Log.d("TAG", "onViewCreated pass: ${day} ${month} ${year}")
                                                    Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                    Log.d("TAG", "onViewCreated: Masuknya ke Atas")
                                                    if(year > 0) {
                                                        tvDietText = "${year} tahun"
                                                    }
                                                    Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                    if(month > 0) {
                                                        tvDietText += " ${month} bulan"
                                                    }
                                                    Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                    if(day > 0) {
                                                        tvDietText += " ${day} hari"
                                                    }
                                                    Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                    binding.tvDiet.text = "$tvDietText"
                                                }else if(user?.bmi!! >= 18.5 && user?.bmi!! <25) {
                                                    binding.tvDiet.visibility = View.GONE
                                                    binding.tvDT.visibility = View.GONE
                                                    binding.tvTarget.visibility = View.VISIBLE
                                                    binding.tvTargetKalori.visibility = View.VISIBLE
                                                    binding.tvTarget.visibility = View.VISIBLE
                                                    binding.tvTargetKalori.text = "Anda perlu mengonsumsi makanan gizi seimbang"
                                                }
                                            }else if(daysDiet >= 1 && user?.bmi!! < 18.5){
//                                                daysDiet(daysDiet.toFloat())
                                                var day = 0
                                                var month = 0
                                                var year = 0
                                                var tvDietText = ""
                                                day = daysDiet
                                                while(day >= 30) {
                                                    day -= 30
                                                    month += 1
                                                    while(month >= 12) {
                                                        month -= 12
                                                        year += 1
                                                    }
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${day} ${month} ${year}")
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                Log.d("TAG", "onViewCreated: Masuknya ke Atas")
                                                if(year > 0) {
                                                    tvDietText = "${year} tahun"
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                if(month > 0) {
                                                    tvDietText += " ${month} bulan"
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                if(day > 0) {
                                                    tvDietText += " ${day} hari"
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                binding.tvDiet.text = "$tvDietText"
//                                                binding.tvDiet.text = "${daysDiet} Hari"
                                                binding.tvTargetKalori.text = "Anda perlu konsumsi makanan Tinggi Kalori"
                                                binding.tvDT.visibility = View.VISIBLE
                                                binding.tvDT.text = "Anda perlu melakukan Diet Selama"
                                                binding.tvTarget.visibility = View.VISIBLE
                                            }else {
                                                Log.d("TAG", "onViewCreated pas: $daysDiet")
//                                                daysDiet(daysDiet.toFloat())
                                                var day = 0
                                                var month = 0
                                                var year = 0
                                                var tvDietText = ""
                                                Log.d("TAG", "onViewCreated pass: ${day} ${month} ${year}")
                                                day = daysDiet
                                                Log.d("TAG", "onViewCreated pass: ${day} ${month} ${year}")
                                                while(day >= 30) {
                                                    day -= 30
                                                    month += 1
                                                    Log.d("TAG", "onViewCreated pass day & month: ${day} ${month}")
                                                    while(month >= 12) {
                                                        month -= 12
                                                        year += 1
                                                        Log.d("TAG", "onViewCreated pass year: ${year}")
                                                    }
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${day} ${month} ${year}")
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                if(year > 0) {
                                                    tvDietText = "${year} tahun"
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                if(month > 0) {
                                                    tvDietText += " ${month} bulan"
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                if(day > 0) {
                                                    tvDietText += " ${day} hari"
                                                }
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                Log.d("TAG", "onViewCreated pass: ${tvDietText}")
                                                Log.d("TAG", "onViewCreated: Masuknya ke bawah")
                                                binding.tvDiet.text = "$tvDietText"
//                                                binding.tvDiet.text = "$daysDiet Hari"
                                                Log.d("TAG", "onViewCreated pas: $daysDiet")
                                                binding.tvTargetKalori.text = "Anda perlu konsumsi makanan Rendah Karbohidrat"
                                                binding.tvDT.visibility = View.VISIBLE
                                                binding.tvTarget.visibility = View.VISIBLE
                                            }
//                                            Pendataan makanan diet
//                                            if (user?.bmi!! >= 25) {
//                                                binding.tvTarget.visibility = View.GONE
//                                                binding.tvTargetKalori.text = "Anda perlu melakukan Diet Karbohidrat"
//                                            }else if(user?.bmi!! >= 18.5 && user?.bmi!! < 25){
//                                                binding.tvTarget.visibility = View.GONE
//                                                binding.tvTargetKalori.visibility = View.GONE
//                                            } else {
//                                                binding.tvTarget.visibility = View.GONE
//                                                binding.tvTargetKalori.visibility = View.GONE
//                                            }
                                        }
                                    }else {
                                        database.collection("FOODDIET").document(user?.email.toString()).set(Fooddiet(user.email!!, 0.0f))
                                            .addOnFailureListener {
                                                Log.d("TAG", "onResume: ${it.message.toString()}")
                                                Log.d("TAG", "onResume: ${it.localizedMessage.toString()}")
                                            }
                                        binding.tvTarget.visibility = View.VISIBLE
                                        binding.tvTargetKalori.text = ""
                                    }

                                }
                        }
                    }.addOnFailureListener{
                        Log.d("TAG", "onResume: ${it.message.toString()}")
                        Log.d("TAG", "onResume: ${it.localizedMessage.toString()}")
                    }

            }.addOnFailureListener {
                Log.d("TAG", "onResume: ${it.message.toString()}")
                Log.d("TAG", "onResume: ${it.localizedMessage.toString()}")
            }
        var user1 = FirebaseAuth.getInstance().currentUser
        var caldef = CalDef(user1!!.email.toString(), 0.0f)

        database.collection("CALDEF").document(user1!!.email.toString()).get()
            .addOnCompleteListener {
                if(it.result.toObject(CalDef::class.java) == null) {
                    database.collection("CALDEF").document(user1!!.email.toString()).set(caldef)
                        .addOnCompleteListener {
                            toast("Update data Calorie Defisit Berhasil")
                        }.addOnFailureListener {
                            toast("update data Calorie Deficit Gagal")
                        }
                }else {
                    var caldef = it.result.toObject(CalDef::class.java)

                    if (caldef?.date == "${Date().year} ${Date().month} ${Date().date}") {
                        var Caldef = caldef!!.caldef!!.toFloat()
                        var updateCaldef: HashMap<String, Any> = HashMap()
                        updateCaldef.put("ca;def", Caldef)
                        var date = "${Date().year} ${Date().month} ${Date().date}"
                        updateCaldef.put("date", date)
                        database.collection("CalDef").document(user1!!.email.toString()).update("caldef", Caldef)
                    }else {
                        var Caldef = 0.0f
                        var updateCaldef: HashMap<String, Any> = HashMap()
                        updateCaldef.put("ca;def", Caldef)
                        updateCaldef.put("date", Date())
                        database.collection("CalDef").document(user1!!.email.toString()).update("caldef", Caldef)
                    }
                }
            }
            .addOnFailureListener {
                toast("${it.localizedMessage.toString()}")
                toast("${it.message.toString()}")
            }
    }

    private fun getFooddiet() {
        database.collection("FOODDIET").document(auth?.email.toString()).get()
            .addOnCompleteListener {
                val fooddiet = it.result.toObject(Fooddiet::class.java)
                if(fooddiet != null) {
                    Log.d("TAG", "getFooddiet: ${fooddiet.calorie}")
                }else {

                }
            }
            .addOnFailureListener {
                toast(it.localizedMessage.toString())
                toast(it.message.toString())
            }
    }

    private fun daysDiet(daysDiet: Float) {
        var day = 0.0f
        var month = 0.0f
        var year = 0.0f
        var tvDietText = ""
        while(daysDiet >= 30.0f) {
            day = daysDiet - 30.0f
            month += 1F
            while(month >= 12F) {
                month -= 12F
                year += 1F
            }
        }
        if(year >= 0) {
            tvDietText = "${year} tahun "
        }
        if (month >= 0) {
            tvDietText += "${month} bulan "
        }
        if (day >= 0) {
            tvDietText += "${day} hari"
        }
        Log.d("TAG", "daysDiet: ${year} ${month} ${day}")
        println("daysDiet: ${year} ${month} ${day}")
        binding.tvDiet.visibility = View.VISIBLE
        binding.tvDiet.text = tvDietText

    }

//    override fun onResume() {
//        super.onResume()
//        database.collection("USERS").document(email).get()
//            .addOnCompleteListener {
//                user = it.result.toObject(User::class.java)!!
//                if (user?.bmi!! < 16) {
//                    binding.tvOW.text = "Kurus Sekali"
//                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
//                } else if (user?.bmi!! >= 16 && user?.bmi!! < 17) {
//                    binding.tvOW.text = "Kurus"
//                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
//                } else if (user?.bmi!! >= 17 && user?.bmi!! < 18.5) {
//                    binding.tvOW.text = "Sedikit Kurus"
//                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
//                } else if (user?.bmi!! >= 18.5 && user?.bmi!! < 25) {
//                    binding.tvOW.text = "Ideal"
//                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
//                } else if (user?.bmi!! >= 25 && user?.bmi!! < 30) {
//                    binding.tvOW.text = "Berlebih"
//                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
//                } else if(user?.bmi!! >=30 && user?.bmi!! < 35) {
//                    binding.tvOW.text = "Obesitas Kelas 1"
//                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
//                } else if(user?.bmi!! >=35 && user?.bmi!! < 40) {
//                    binding.tvOW.text = "Obesitas Kelas 2"
//                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
//                } else {
//                    binding.tvOW.text = "Obesitas Kelas 3"
//                    binding.tvblebih.text = "Kategori Berat badan ${user?.fullName!!.trim()}"
//                }
//                binding.tvbmr.text = user?.bmr.toString()
//                binding.tvWeight.text = user?.weight.toString()
//                binding.tvTallincm.text = (user?.height!! * 100.0f).toString()
//                countOW()
//                countJogSug()
//            }.addOnFailureListener {
//                Log.d("TAG", "onResume: ${it.message.toString()}")
//                Log.d("TAG", "onResume: ${it.localizedMessage.toString()}")
//            }
//    }

    private fun maxWeight(height: Float) : Float {
        var maxWeight = 25 * (height * height)
        return maxWeight
    }

    private fun countOW() {
        if (user?.weight!! > user?.maxWeight!!) {
//            binding.tvOW.text = "${(user?.weight!! - user?.maxWeight!!).toString()} KG"
        }else {
//            binding.tvOW.text = "Normal"
//            binding.tvblebih.text = "Berat badan ${user.fullName}"
        }
    }

    private fun countJogSug() {
        var dietCalorie = 500
//        var K = dietCalorie * 20/100

        var K = dietCalorie
        var C = (2.8 * 7.7 * ((user?.weight!!) / 200))
        Log.d("TAG", "countJogSug: ${C}")
        var J = K/C
        binding.tvJogSug.text = "${J.toString()} menit"

        if (user?.weight!! < user?.maxWeight!!) {
            binding.tvJogSug.text = "No Jogging"
            binding.tvjog.visibility = View.GONE
            binding.tvJogSug.visibility = View.GONE
        }else {
            binding.tvJogSug.text = "${J.roundToInt().toString().trim()} menit"
        }
    }

//    private fun calculateJoggingSuggestionInKm(caloriesBurned: Float, weight: Float): Float {
//        var bmi =
//        val distanceInKm = caloriesBurned / weight
//        joggingSuggestion = distanceInKm
//        return joggingSuggestion
//    }
}
