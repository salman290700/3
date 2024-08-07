package com.example.dietjoggingapp.ui.Fragments

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.adapters.JoggingAdapter
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.database.domains.ActivityClassified
import com.example.dietjoggingapp.databinding.FragmentJoggingBinding
import com.example.dietjoggingapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.dietjoggingapp.other.TrackingUtil
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.ui.viewmodels.MainViewModel
import com.example.dietjoggingapp.utility.hide
import com.example.dietjoggingapp.utility.show
import com.example.dietjoggingapp.utility.toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

private const val TAG = "JoggingFragment"
@AndroidEntryPoint
class JoggingFragment: Fragment(R.layout.fragment_jogging), EasyPermissions.PermissionCallbacks{
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isFineLocation: Boolean = false
    private var isCoarseLocation: Boolean = false
    private var isBackgroundLocation: Boolean = false
    private var isForeground: Boolean = false
    private var isReadExternal: Boolean = false

    private var permissionRequest: MutableList<String> = arrayListOf()
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: com.example.dietjoggingapp.databinding.FragmentJoggingBinding
    private lateinit var joggingAdapter: JoggingAdapter
    private lateinit var user: User
    private lateinit var firebaseFirestore: FirebaseFirestore

    private val TIME_STAMP = 100
    private val TAG: String = "Jogging Fragment"
    private lateinit var ax: MutableList<Float>
    private lateinit var ay: MutableList<Float>
    private lateinit var az: MutableList<Float>

    private lateinit var gx: MutableList<Float>
    private lateinit var gy: MutableList<Float>
    private lateinit var gz: MutableList<Float>

    private lateinit var lx: MutableList<Float>
    private lateinit var ly: MutableList<Float>
    private lateinit var lz: MutableList<Float>

    private lateinit var sensorManager: SensorManager
    private lateinit var mAccelerometer: Sensor
    private lateinit var mGroScope: Sensor
    private lateinit var mLinearAcceleration: Sensor

    private var results = arrayListOf<Float>()
    private lateinit  var activityClassifier: ActivityClassified
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentJoggingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseFirestore = FirebaseFirestore.getInstance()
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            permissionLauncher()
        }else {
            permissionLauncherVersionQLater()
        }
        requestPermission()
        Log.d("TAG", "onCreate: ${requireActivity().toString().trim()}")
        joggingAdapter = JoggingAdapter()
//        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onNothingSelected(p0: AdapterView<*>?) {
//
//            }
//
//            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long) {
//                when(pos) {
//                    0 -> viewModel.sortJogging(SortType.DATE)
//                    1 -> viewModel.sortJogging(SortType.DISTANCE)
//                    2 -> viewModel.sortJogging(SortType.TIME_IN_MILLIS)
//                    3 -> viewModel.sortJogging(SortType.CALORIES_BURNED)
//                    4 -> viewModel.sortJogging(SortType.AVG_SPEED)
//                }
//            }
//        }

        val firebaseAuth = FirebaseAuth.getInstance().currentUser?.uid?.toString()?.trim()
        viewModel.getJogging(firebaseAuth.toString().trim())

//        viewModel.joggingSortByDate.observe(viewLifecycleOwner, Observer {
//            joggingAdapter.submitList(it)
//        })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_joggingFragments_to_trackingFragment)
        }
        binding.rvRuns.adapter = joggingAdapter
        viewModel.getJoggings.observe(viewLifecycleOwner, Observer{state ->
            when(state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    if (state.data.toMutableList() != null) {
                        joggingAdapter.submitList(state.data.toMutableList())
                    }

                    Log.d(TAG, "onViewCreated: ${state.data.toString().trim()}")
                }
                is UiState.failure -> {
                    toast(state.error)
                    binding.progressBar.hide()
                }
            }
        })
    }

    private fun requestPermissions() {
        if (TrackingUtil.locationPermissions(requireContext())) {
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to Accept location Permissions to Use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to Accept location Permissions to Use this app",
                REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        TODO("Not yet implemented")
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        TODO("Not yet implemented")
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            AppSettingsDialog.Builder(this).build().show()
        } else {
            requestPermissions()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)

    }

    private fun requestPermissionVersionQLater() {
        isFineLocation = ContextCompat.checkSelfPermission(
            context?.applicationContext!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isCoarseLocation = ContextCompat.checkSelfPermission(
            context?.applicationContext!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isBackgroundLocation = ContextCompat.checkSelfPermission(
            context?.applicationContext!!,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!isFineLocation){
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (!isCoarseLocation){
            permissionRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (!isBackgroundLocation){
            permissionRequest.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
        if (permissionRequest.isNotEmpty()){
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

        Log.d(TAG, "requestPermissionVersionQLater: ${isBackgroundLocation.let { it.toString() }}, ${isFineLocation.let { it.toString() }}, ${isCoarseLocation.let { it.toString() }}")
    }

    private fun permissionLauncherVersionQLater(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            isFineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isReadExternal
            isCoarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: isCoarseLocation
            isBackgroundLocation = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: isBackgroundLocation
        }
        Log.d(TAG, "requestPermissionVersionQLater: ${isBackgroundLocation.let { it.toString() }}, ${isFineLocation.let { it.toString() }}, ${isCoarseLocation.let { it.toString() }}")
        requestPermissionVersionQLater()
    }

    private fun requestPermission() {
        //        Add some permisison Request with permission that we need
        if (!isReadExternal){
            permissionRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (!isFineLocation){
            permissionRequest.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (!isCoarseLocation){
            permissionRequest.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (!isForeground){
            permissionRequest.add(Manifest.permission.FOREGROUND_SERVICE)
        }
        //        Change Permission Into Granted if Permisison is Granted
        isReadExternal = ContextCompat.checkSelfPermission(
            context?.applicationContext!!,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED

        isFineLocation = ContextCompat.checkSelfPermission(
            context?.applicationContext!!,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isCoarseLocation = ContextCompat.checkSelfPermission(
            context?.applicationContext!!,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isBackgroundLocation = ContextCompat.checkSelfPermission(
            context?.applicationContext!!,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        isForeground = ContextCompat.checkSelfPermission(
            context?.applicationContext!!,
            Manifest.permission.FOREGROUND_SERVICE
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionRequest.isNotEmpty()){
            permissionLauncher.launch(permissionRequest.toTypedArray())
        }

        Log.d(TAG, "requestPermissionVersionQLater: ${isBackgroundLocation.let { it.toString() }}, ${isFineLocation.let { it.toString() }}, ${isCoarseLocation.let { it.toString() }}")
    }

    private fun permissionLauncher(){
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){permissions ->
            isFineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isFineLocation
            isCoarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: isCoarseLocation
            isBackgroundLocation = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: isBackgroundLocation
        }

        Log.d(TAG, "requestPermissionVersionQLater: ${isBackgroundLocation.let { it.toString() }}, ${isFineLocation.let { it.toString() }}, ${isCoarseLocation.let { it.toString() }}")
        requestPermission()
    }
}