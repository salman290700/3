package com.example.dietjoggingapp.ui.Fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.adapters.JoggingAdapter
import com.example.dietjoggingapp.databinding.FragmentJoggingBinding
import com.example.dietjoggingapp.other.Constants.REQUEST_CODE_LOCATION_PERMISSION
import com.example.dietjoggingapp.other.SortType
import com.example.dietjoggingapp.other.TrackingUtil
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.ui.viewmodels.MainViewModel
import com.example.dietjoggingapp.utility.hide
import com.example.dietjoggingapp.utility.show
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

private const val TAG = "JoggingFragment"
@AndroidEntryPoint
class JoggingFragment: Fragment(R.layout.fragment_jogging), EasyPermissions.PermissionCallbacks {
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isFineLocation: Boolean = false
    private var isCoarseLocation: Boolean = false
    private var isBackgroundLocation: Boolean = false
    private var isForeground: Boolean = false
    private var isReadExternal: Boolean = false

    private var permissionRequest: MutableList<String> = arrayListOf()
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentJoggingBinding
    private lateinit var joggingAdapter: JoggingAdapter
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
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            permissionLauncher()
        }else {
            permissionLauncherVersionQLater()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requestPermission()
        setupRecyclerView()

        binding.spFilter.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

            override fun onItemSelected(adapterView: AdapterView<*>?, view: View?, pos: Int, id: Long) {
                when(pos) {
                    0 -> viewModel.sortJogging(SortType.DATE)
                    1 -> viewModel.sortJogging(SortType.DISTANCE)
                    2 -> viewModel.sortJogging(SortType.TIME_IN_MILLIS)
                    3 -> viewModel.sortJogging(SortType.CALORIES_BURNED)
                    4 -> viewModel.sortJogging(SortType.AVG_SPEED)
                }
            }
        }

        viewModel.getJoggings.observe(viewLifecycleOwner, Observer{state ->
            when(state) {
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    joggingAdapter.submitList(state.data.toMutableList())
                }
                is UiState.failure -> {
                    binding.progressBar.hide()
                }
            }
        })

//        viewModel.joggingSortByDate.observe(viewLifecycleOwner, Observer {
//            joggingAdapter.submitList(it)
//        })

        binding.fab.setOnClickListener {
            findNavController().navigate(R.id.action_joggingFragments_to_trackingFragment)
        }
    }

    private fun setupRecyclerView() = binding.rvRuns.apply {
        joggingAdapter = JoggingAdapter()
        adapter = joggingAdapter
        layoutManager = LinearLayoutManager(requireContext())
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