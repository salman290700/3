package com.example.dietjoggingapp.ui.Fragments

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
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
import com.example.dietjoggingapp.ui.viewmodels.AuthViewModel
import com.example.dietjoggingapp.ui.viewmodels.MainViewModel
import com.example.dietjoggingapp.utility.hide
import com.example.dietjoggingapp.utility.show
import com.example.dietjoggingapp.utility.toast
import com.google.android.gms.maps.CameraUpdate
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.*
import javax.inject.Inject
import kotlin.math.round

@AndroidEntryPoint
class TrackingFragment: Fragment(R.layout.fragment_tracking) {
    val viewModel: MainViewModel by viewModels()
    private lateinit var binding: FragmentTrackingBinding
    private var map: GoogleMap? = null
    
    private var isTracking = false
    private var pathPoints = mutableListOf<Polyline>()
    
    private var currentTimeInMilliseconds = 0L
    
    private var menu: Menu? = null

    var isFIrstRun: Boolean = true
    var isServiceKilled: Boolean = false

    @set:Inject
    var weight = 80f

    val authViewModel: AuthViewModel by viewModels()
    private var objJogging: Jogging? = null
    var imageUris: MutableList<Uri> = arrayListOf()

    var avgSpeed = 0f
    var dateTimeStamp = Calendar.getInstance().timeInMillis
    var caloriesBurned = 0
    private var jogging: Jogging? = null
    var bitmap: Bitmap? = null
    var distanceInMeter = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)
        val rootView: View = inflater.inflate(R.layout.fragment_tracking, container, false)
        binding = FragmentTrackingBinding.inflate(inflater, container, false)
        return binding.root
//        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView?.onCreate(savedInstanceState)
        binding.btnToggleRun.setOnClickListener{
            toggleRun()
        }

        binding.btnToggleFinishRun.setOnClickListener{
            zoomToAllJoggingTrack()
            createJogging()
        }
        binding.mapView?.getMapAsync {
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

    private fun addBitmap() {
        val bundle = arguments
        val message = bundle!!.getString("EXTRA_USER_SESSION")
        map?.snapshot { bmp ->
            var distanceInMeter = 0
            for(polyline in pathPoints) {
                distanceInMeter += TrackingUtil.calculatePolilyneDistance(polyline).toInt()
            }
            var avgSpeed = round((distanceInMeter / 1000f) / (currentTimeInMilliseconds / 1000f / 60 / 60 ) * 10 ) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeter / 1000f) * weight).toInt()
            val jogging = Jogging("", message.toString(),bmp, dateTimeStamp, avgSpeed, distanceInMeter, currentTimeInMilliseconds, caloriesBurned)

            Snackbar.make(
                requireActivity().findViewById(R.id.joggingFragments),
                "Run Saved Successfully ${jogging.caloriesBurned}",
                Snackbar.LENGTH_LONG
            ).show()

    }
}

    private fun createJogging(){
        addBitmap()

//        if (validation()){
//            if(jogging == null){
//                viewModel.addJogging(getJogging())
//            }else {
//
//            }
//        }
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
                    if (jogging == null){
                        viewModel.addJogging(getJogging())
                    }
                    jogging = state.data.first
                    Log.d("TAG", "createProject: " + jogging!!.id)
                    Log.d("TAG", "createProjectname: " + jogging!!.caloriesBurned)
                }
            }
        }
        stopRun()
    }

    private fun validation(): Boolean {
        var isValid = true
//        if (binding.etProjectName.text.isNullOrEmpty()){
//            isValid = false
//        }
        return isValid
    }


    private fun getJogging(): Jogging{
        val bundle = arguments
        val message = bundle!!.getString("EXTRA_USER_SESSION")
        map?.snapshot { bmp ->
            var distanceInMeter = 0
            for(polyline in pathPoints) {
                distanceInMeter += TrackingUtil.calculatePolilyneDistance(polyline).toInt()
            }
            var avgSpeed = round((distanceInMeter / 1000f) / (currentTimeInMilliseconds / 1000f / 60 / 60 ) * 10 ) / 10f
            val dateTimeStamp = Calendar.getInstance().timeInMillis
            val caloriesBurned = ((distanceInMeter / 1000f) * weight).toInt()

            this.distanceInMeter = distanceInMeter
            this.jogging = jogging
            this.dateTimeStamp = dateTimeStamp
            this.caloriesBurned = caloriesBurned
            this.avgSpeed = avgSpeed
            this.bitmap = bmp
            val jogging = Jogging("", message.toString(),bitmap, dateTimeStamp, avgSpeed, distanceInMeter, currentTimeInMilliseconds, caloriesBurned)
            Snackbar.make(
                requireActivity().findViewById(R.id.joggingFragments),
                "Run Saved Successfully ${jogging.caloriesBurned}",
                Snackbar.LENGTH_LONG
            ).show()
            Log.d("TAG", "getJogging: " + bmp.toString())
            stopRun()
        }
        return Jogging(
            id = jogging?.id ?: "",
            userId = message.toString(),
            img = bitmap,
            timestamp =  dateTimeStamp,
            avgSpeedInKmh = avgSpeed,
            distanceInMeters = distanceInMeter,
            timeInMillis = currentTimeInMilliseconds,
            caloriesBurned = caloriesBurned
        ).apply { authViewModel.getSession { this.id = it?.userId ?: "" } }
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
            binding.tvTimer.text = formattedTime
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
            binding.btnToggleRun.text = "Start"
            binding.btnToggleFinishRun.visibility = View.GONE
        } else if(!isTracking && !isFIrstRun) {
            binding.btnToggleRun.text = "Start"
            binding.btnToggleFinishRun.visibility = View.VISIBLE
        } else {
            binding.btnToggleRun.text = "Stop"
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
        }else {

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