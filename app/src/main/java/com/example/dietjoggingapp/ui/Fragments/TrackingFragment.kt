package com.example.dietjoggingapp.ui.Fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.DailyCalories
import com.example.dietjoggingapp.database.Jogging
import com.example.dietjoggingapp.database.User
import com.example.dietjoggingapp.database.domains.ActiivtyClassified
import com.example.dietjoggingapp.database.domains.ActivityClassified
import com.example.dietjoggingapp.databinding.FragmentTrackingBinding
import com.example.dietjoggingapp.other.Constants
import com.example.dietjoggingapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.dietjoggingapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.dietjoggingapp.other.Constants.ACTION_STOP_SERVICE
import com.example.dietjoggingapp.other.Constants.MAP_ZOOM
import com.example.dietjoggingapp.other.Constants.POLYLINE_COLOR
import com.example.dietjoggingapp.other.Constants.POLYLINE_WIDTH
import com.example.dietjoggingapp.other.TrackingUtil
import com.example.dietjoggingapp.other.UiState
import com.example.dietjoggingapp.services.Polyline
import com.example.dietjoggingapp.services.TrackingService
import com.example.dietjoggingapp.ui.MainActivity
import com.example.dietjoggingapp.ui.viewmodels.MainViewModel
import com.example.dietjoggingapp.utility.hide
import com.example.dietjoggingapp.utility.show
import com.example.dietjoggingapp.utility.toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.AndroidEntryPoint
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking), SensorEventListener, EasyPermissions.PermissionCallbacks {
    val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentTrackingBinding
    private lateinit var user: User
    private var map: GoogleMap? = null
    
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()

    private var isStart: Boolean = false
    
    private var currentTimeInMilliseconds = 0L
    
    private var menu: Menu? = null

    var isFIrstRun: Boolean = true

    var weight = 80f
    var avgSpeed = 0f
    var dateTimeStamp = Calendar.getInstance().timeInMillis
    var caloriesBurned = 0f
    var jogging: Jogging? = null
    var bitmap: String? = ""
    var distanceInMeter = 0f
    val auth = FirebaseAuth.getInstance().currentUser?.email

    private val TIME_STAMP = 100
    private val TAG: String = "Tracking Fragment"
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

    private var results: FloatArray? = null
    private lateinit  var activityClassifier: ActivityClassified

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var isFineLocation: Boolean = false
    private var isCoarseLocation: Boolean = false
    private var isBackgroundLocation: Boolean = false
    private var isForeground: Boolean = false
    private var isReadExternal: Boolean = false

    private var permissionRequest: MutableList<String> = arrayListOf()

    val database = FirebaseFirestore.getInstance()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
//        return rootView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
            permissionLauncher()
            permissionLauncherVersionQLater()
        }else {
            permissionLauncherVersionQLater()
            permissionLauncher()
        }
        requestPermission()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        mAccelerometer= sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        mGroScope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!
        mLinearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!
        binding.mapView.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener{
            toggleRun()
        }
        database.collection(Constants.FirestoreTable.USERS).document(auth.toString())
            .get()
            .addOnCompleteListener {
                this.user = it.result.toObject(User::class.java)!!
                Timber.d("onViewCreated ${user.toString().trim()}")
            }
            .addOnFailureListener {

            }
        binding.btnToggleFinishRun.setOnClickListener{
            if (map != null) {
                zoomToAllJoggingTrack()
                createJogging()
            }else {
                toast("Jogging Canceled")
                val intent = Intent(requireActivity(), MainActivity::class.java)
                startActivity(intent)
            }
        }
        binding.mapView.getMapAsync {
            map = it
            addAllPolylines()
        }
        subscribeToObservers()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.toolbar_tracking_menu, menu)
        this.menu = menu
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)
        if(currentTimeInMilliseconds > 0L) {
            this.menu?.getItem(0)?.isVisible = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.miCancelTracking -> {
                showCancelTrackingDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun showCancelTrackingDialog() {
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setTitle("Cancel the Run ?")
            .setMessage("Are you sure to cancel your jogging ?")
            .setIcon(R.drawable.ic_baseline_delete_24)
            .setPositiveButton("yes"){_, _,->
                stopRun()
            }
            .setNegativeButton("No"){dialogInterface, _->
                dialogInterface.cancel()
            }
            .create()
        dialog.show()
    }

    private fun stopRun() {
        sendCommandToService(ACTION_STOP_SERVICE)
        findNavController().navigate(R.id.action_home)
    }

    private fun zoomToAllJoggingTrack() {
        var bounds = LatLngBounds.Builder()
        Log.d("TAG", "zoomToAllJoggingTrack: ${pathPoints.toString().trim()}")
        for(polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }
        Log.d(TAG, "zoomToAllJoggingTrack: ${pathPoints.isEmpty().toString().trim()}")
        if (map != null) {
            if (pathPoints.toString() == "[[]]" || pathPoints.toString() == "[[], []]") {
                toast("Jogging Di Cancel")
                Log.d(TAG, "zoomToAllJoggingTrack: ${pathPoints.toString().trim()}")
            }else {
                map?.moveCamera(
                    CameraUpdateFactory.newLatLngBounds(
                        bounds.build(),
                        binding.mapView.width,
                        binding.mapView.height,
                        (binding.mapView.height * 0.05f).toInt()
                    )
                )
            }
        }else {
            Toast.makeText(requireActivity(), "Jogging di Cancel", Toast.LENGTH_SHORT)
            findNavController().navigate(R.id.action_home)
        }

    }

    private fun createJogging(){
//        if (validation()){
//            if(jogging == null){
//                viewModel.addJogging(getJogging())
//            }else {
//
//            }
//        }
        getJogging()
        Timber.d("createJogging: ${jogging.toString().trim()}")

        viewModel.addJogging.observe(viewLifecycleOwner){state ->
            when(state){
                is UiState.Loading -> {
                    binding.progressBar.show()
                }
                is UiState.failure -> {
                    binding.progressBar.hide()
                    toast(state.error)
                }
                is UiState.Success -> {
                    binding.progressBar.hide()
                    toast(state.data.second)
                    Log.d("TAG", "createJogging: jogging" + jogging?.caloriesBurned.toString().trim())
                    Log.d("TAG", "createJogging: " + jogging!!.id)
                    Log.d("TAG", "createjogging: " + jogging!!.caloriesBurned)
                }
            }
        }
        stopRun()
    }

    private fun getJogging(){
        val auth = FirebaseAuth.getInstance().currentUser
        var simpleDateFormat = SimpleDateFormat("yyyyMMddHHmmSS", Locale.TAIWAN)
        var filename = simpleDateFormat.format(Date())
        var path = activity?.applicationContext?.filesDir?.path
        var file = File(path, "DietJoggingApp")
        if(!file.exists()) {
            file.mkdirs()
        }

        var file2 = File(file, filename + ".png")
        var fOut = ByteArrayOutputStream()


        map?.snapshot { bmp ->
            var distanceInMeter = 0f
            bmp?.compress(Bitmap.CompressFormat.PNG, 50, fOut)
            var data = fOut.toByteArray()

            for(polyline in pathPoints) {
                distanceInMeter += TrackingUtil.calculatePolilyneDistance(polyline)
            }

            if (data != null) {
                var avgSpeed = round((distanceInMeter / 1000f) / (currentTimeInMilliseconds / 1000f / 60 / 60 ) * 10 ) / 10f
                val dateTimeStamp = Calendar.getInstance().timeInMillis
                val caloriesBurned = ((distanceInMeter / 1000f) * user.weight)

                this.distanceInMeter = distanceInMeter
                this.jogging = jogging
                this.dateTimeStamp = dateTimeStamp
                this.caloriesBurned = caloriesBurned
                this.avgSpeed = avgSpeed
                this.bitmap = ""
                val documentDaillyCalorie = auth!!.email?.let { database.collection("USERS").document(it) }
                var dailyCalories = DailyCalories()
                var calorie = 0f
                documentDaillyCalorie!!.get().addOnCompleteListener {
                    if (it.isSuccessful) {
                        val documentSnapshot = it.result
                        var dailyCalories = documentSnapshot.toObject(User::class.java)!!
                        calorie = dailyCalories.dailyCalorie - caloriesBurned
                        dailyCalories.dailyCalorie = calorie
                        documentDaillyCalorie?.update("dailyCalorie", dailyCalories.dailyCalorie)!!.addOnCompleteListener {

                        }.addOnFailureListener {
                            Toast.makeText(requireActivity(), it.localizedMessage, Toast.LENGTH_SHORT)
                        }
                    }
                }.addOnFailureListener {
                    Toast.makeText(requireActivity(), it.localizedMessage, Toast.LENGTH_SHORT)
                }
                fOut.flush()
                fOut.close()

                Log.d("TAG", "getJogging: currentTimeInMillis" + this.currentTimeInMilliseconds.toString().trim())
                Log.d("TAG", "getJogging: calories burned" + this.caloriesBurned.toString().trim())
                Log.d("TAG", "getJogging: dateTimeStamp" + this.dateTimeStamp.toString().trim())
                Log.d("TAG", "getJogging: user weight" + user.weight.toString().trim())
                Log.d("TAG", "getJogging: distancen in meter" + this.distanceInMeter.toString().trim())
                Log.d("TAG", "getJogging: bitmap" + this.bitmap.toString().trim())
                Log.d("TAG", "getJogging: distance in meter" + distanceInMeter.toString().trim())
                jogging = Jogging("", auth.uid, "", this.dateTimeStamp, this.avgSpeed, this.distanceInMeter, this.currentTimeInMilliseconds,this.caloriesBurned)
                Log.d("TAG", "getJogging: jogging test ${this.jogging?.caloriesBurned.toString().trim() }}")
                Log.d("TAG", "getJogging: return ${this.jogging?.caloriesBurned.toString().trim()}")

                val document = database.collection(Constants.FirestoreTable.JOGGING).document()

                if (jogging != null) {
                    jogging?.id = document.id
                    document.set(jogging!!)
                        .addOnSuccessListener {
                            var storage = FirebaseStorage.getInstance().getReference("photo/jogging/${user.userId}/${document.id}.png")
                            storage.putBytes(data)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        storage.downloadUrl
                                            .addOnCompleteListener {
                                                var imageUrl = it.result.toString()
                                                val document2 = database.collection(Constants.FirestoreTable.JOGGING).document(document.id)
                                                document2.update("img", imageUrl)
                                                Log.d("TAG", "getJogging: url ${imageUrl}")
                                            }
                                    } else {
                                        Log.d("TAG", "getJogging: Image upload task is not successfull")
                                    }
                                }
                            toast("Success")
                            Log.d("TAG", "getJogging: Success Save gmbar")
                        }
                        .addOnFailureListener{
                            Toast.makeText(activity?.applicationContext, "Error : ${it.toString().trim()}", Toast.LENGTH_SHORT)
                        }
                    Log.d("TAG", "addJogging: distance in meters " + jogging?.distanceInMeters)
                    Log.d("TAG", "addJogging: calories burned" + jogging?.caloriesBurned)
                    Log.d("TAG", "addJogging: ${document.id.toString().trim()}")
                }else {
                    Log.d("TAG", "getJogging: Joggign is null")
                }
                Log.d("TAG", "addJogging: " + jogging?.id)
                Log.d("TAG", "addJogging: addJogging" + this.caloriesBurned)
            }else {
                toast("Jogging di cancel")
            }
        }
    }

    private fun subscribeToObservers() {
        TrackingService.isTracking.observe(viewLifecycleOwner, Observer {
            updateTracking(it)
        })
        TrackingService.pathPoints.observe(viewLifecycleOwner, Observer{
            pathPoints = it
            addLatestPolyline()
            moveCameraToUser()
        })
        TrackingService.timingRunInMillis.observe(viewLifecycleOwner, Observer {
            currentTimeInMilliseconds = it
            val formattedTime = TrackingUtil.getFormattedStopWatchTime(currentTimeInMilliseconds, true)
            binding.tvTimer.text = formattedTime.trim()
        })

        TrackingService.isStart.observe(viewLifecycleOwner, Observer {
            if (it > BigDecimal(0.5)) {
                isStart = true
            }
            else {
                isStart = false
            }
        })
    }

    private fun toggleRun() {
        if(isTracking) {
            menu?.getItem(0)?.isVisible = true
            sendCommandToService(ACTION_PAUSE_SERVICE)
        }else {
            sendCommandToService(ACTION_START_OR_RESUME_SERVICE)
        }
    }

    private fun updateTracking(isTracking: Boolean) {
        this.isTracking = isTracking
        isFIrstRun = false
        if(!isTracking && isFIrstRun){ // &&  !isStart) {
            binding.btnToggleRun.text = getString(R.string.Start).trim()
            binding.btnToggleFinishRun.visibility = View.GONE
        } else if(!isTracking && !isFIrstRun ){ //&& !isStart) {
            binding.btnToggleRun.text = getString(R.string.Start).trim()
            binding.btnToggleFinishRun.visibility = View.VISIBLE
        } else {
            binding.btnToggleRun.text = getString(R.string.Stop).trim()
            menu?.getItem(0)?.isVisible = true
            binding.btnToggleFinishRun.visibility = View.GONE
        }
    }

    private fun moveCameraToUser() {
        if (pathPoints.isNotEmpty() && pathPoints.last().isNotEmpty()) {
            map?.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    pathPoints.last().last(),
                    MAP_ZOOM
                )
            )
        }
    }

    private fun addAllPolylines() {
        for(polyline in pathPoints) {
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .addAll(polyline)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun addLatestPolyline() {
        if(pathPoints.isNotEmpty() && pathPoints.last().size > 1) {
            val preLastLatLng = pathPoints.last()[pathPoints.last().size - 2]
            val lastLatLng = pathPoints.last().last()
            val polylineOptions = PolylineOptions()
                .color(POLYLINE_COLOR)
                .width(POLYLINE_WIDTH)
                .add(preLastLatLng)
                .add(lastLatLng)
            map?.addPolyline(polylineOptions)
        }
    }

    private fun sendCommandToService(action: String ) {
        Intent(requireContext(), TrackingService::class.java).also{
            it.action = action
            requireContext().startService(it)
        }
    }

    override fun onResume() {
        super.onResume()
//        binding.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
//        binding.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
//        binding.mapView?.onStop()
    }

    override fun onPause() {

        super.onPause()
        sensorManager = requireActivity().getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, mGroScope, SensorManager.SENSOR_DELAY_FASTEST)
        sensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_FASTEST)
//        binding.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
//        binding.mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
//        binding.mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
//        binding.mapView?.onSaveInstanceState(outState)
    }

    override fun onSensorChanged(event: SensorEvent?) {

    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    private fun toFloatArray(data: kotlin.collections.ArrayList<Float>): FloatArray? {
        var i = 0
        val array = FloatArray(data.size)
        for (f in data) {
            array[i++] = f ?: Float.NaN
        }
        return array
    }

    private fun toFloatArray2(data: FloatArray?): kotlin.collections.ArrayList<Float> {
        var i = 0
        val array = kotlin.collections.ArrayList<Float>()
        for (f in data!!) {
            array[i++] = f ?: Float.NaN
        }
        return array
    }

    private fun requestPermissions() {
        if (TrackingUtil.locationPermissions(requireContext())) {
            return
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            EasyPermissions.requestPermissions(
                this,
                "You need to Accept location Permissions to Use this app",
                Constants.REQUEST_CODE_LOCATION_PERMISSION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            EasyPermissions.requestPermissions(
                this,
                "You need to Accept location Permissions to Use this app",
                Constants.REQUEST_CODE_LOCATION_PERMISSION,
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
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ permissions ->
            isFineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: isFineLocation
            isCoarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: isCoarseLocation
            isBackgroundLocation = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] ?: isBackgroundLocation
        }

        Log.d(TAG, "requestPermissionVersionQLater: ${isBackgroundLocation.let { it.toString() }}, ${isFineLocation.let { it.toString() }}, ${isCoarseLocation.let { it.toString() }}")
        requestPermission()
    }
}