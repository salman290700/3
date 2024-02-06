package com.example.dietjoggingapp.ui.Fragments

import android.content.Intent
import android.graphics.Bitmap
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.database.Jogging
import com.example.dietjoggingapp.database.User
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
import com.google.firebase.storage.StorageReference
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {
    val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentTrackingBinding
    private lateinit var user: User
    private var map: GoogleMap? = null
    
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    
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
    val auth = FirebaseAuth.getInstance().currentUser?.uid

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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
            zoomToAllJoggingTrack()
            createJogging()
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
        findNavController().navigate(R.id.action_tracking_fragment_to_jogging_fragment)
    }

    private fun zoomToAllJoggingTrack() {
        val bounds = LatLngBounds.Builder()
        for(polyline in pathPoints) {
            for (pos in polyline) {
                bounds.include(pos)
            }
        }

        map?.moveCamera(
            CameraUpdateFactory.newLatLngBounds(
                bounds.build(),
                binding.mapView.width,
                binding.mapView.height,
                (binding.mapView.height * 0.05f).toInt()
            )
        )
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

            var avgSpeed = round((distanceInMeter / 1000f) / (currentTimeInMilliseconds / 1000f / 60 / 60 ) * 10 ) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeter / 1000f) * user.weight.toFloat())
            this.distanceInMeter = distanceInMeter
            this.jogging = jogging
            this.dateTimeStamp = dateTimeStamp
            this.caloriesBurned = caloriesBurned
            this.avgSpeed = avgSpeed
            this.bitmap =""
            fOut.flush()
            fOut.close()

            Log.d("TAG", "getJogging: currentTimeInMillis" + this.currentTimeInMilliseconds.toString().trim())
            Log.d("TAG", "getJogging: calories burned" + this.caloriesBurned.toString().trim())
            Log.d("TAG", "getJogging: dateTimeStamp" + this.dateTimeStamp.toString().trim())
            Log.d("TAG", "getJogging: user weight" + user.weight.toString().trim())
            Log.d("TAG", "getJogging: distancen in meter" + this.distanceInMeter.toString().trim())
            Log.d("TAG", "getJogging: bitmap" + this.bitmap.toString().trim())
            Log.d("TAG", "getJogging: distance in meter" + distanceInMeter.toString().trim())
            jogging = Jogging("", auth.toString(), "", this.dateTimeStamp, this.avgSpeed, this.distanceInMeter, this.currentTimeInMilliseconds,this.caloriesBurned)
            Log.d("TAG", "getJogging: jogging test ${this.jogging?.caloriesBurned.toString().trim() }}")
            Log.d("TAG", "getJogging: return ${this.jogging?.caloriesBurned.toString().trim()}")

            val document = database.collection(Constants.FirestoreTable.JOGGING).document()

            if (jogging != null) {
                jogging?.id = document.id
                document.set(jogging!!)
                    .addOnSuccessListener {
                        var storage = FirebaseStorage.getInstance().getReference("photo/jogging/${user.userId}/${document.id}.png")
                        storage.putBytes(data)
                            .addOnSuccessListener {
                                toast("Success")
                                Log.d("TAG", "getJogging: Success Save gmbar")
                            }
                            .addOnFailureListener{
                                Toast.makeText(activity?.applicationContext, "Error : ${it.toString().trim()}", Toast.LENGTH_SHORT)
                            }
                        val imageUrl = storage.downloadUrl.toString().trim();
                        val document2 = database.collection(Constants.FirestoreTable.JOGGING).document(document.id)
                        document2.update("img", imageUrl)
                        Log.d("TAG", "addJogging: distance in meters " + jogging?.distanceInMeters)
                        Log.d("TAG", "addJogging: calories burned" + jogging?.caloriesBurned)
                        Log.d("TAG", "addJogging: ${document.id.toString().trim()}")
                        Log.d("TAG", "getJogging: ${document2.id} ${imageUrl}")
                    }
                    .addOnFailureListener{
                        Log.d("TAG", "addJogging: " + it.localizedMessage)
                    }
            }else {
                Log.d("TAG", "getJogging: Joggign is null")
            }
            Log.d("TAG", "addJogging: " + jogging?.id)
            Log.d("TAG", "addJogging: addJogging" + this.caloriesBurned)
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
        if(!isTracking && isFIrstRun) {
            binding.btnToggleRun.text = getString(R.string.Start).trim()
            binding.btnToggleFinishRun.visibility = View.GONE
        } else if(!isTracking && !isFIrstRun) {
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
        binding.mapView?.onResume()
    }

    override fun onStart() {
        super.onStart()
        binding.mapView?.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView?.onStop()
    }

    override fun onPause() {
        super.onPause()
        binding.mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mapView?.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        binding.mapView?.onSaveInstanceState(outState)
    }
}