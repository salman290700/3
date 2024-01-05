package com.example.dietjoggingapp.services

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.NotificationManager.IMPORTANCE_LOW
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationRequest
import android.os.Build
import android.os.Looper
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.other.Constants
import com.example.dietjoggingapp.other.Constants.ACTION_PAUSE_SERVICE
import com.example.dietjoggingapp.other.Constants.ACTION_START_OR_RESUME_SERVICE
import com.example.dietjoggingapp.other.Constants.ACTION_STOP_SERVICE
import com.example.dietjoggingapp.other.Constants.FASTEST_LOCATION_INTERVAL
import com.example.dietjoggingapp.other.Constants.ID_CHANNEL_NOTIFICATION
import com.example.dietjoggingapp.other.Constants.LOCATION_UPDATE_INTERVAL
import com.example.dietjoggingapp.other.Constants.NAME_CHANNEL_NOTIFICATION
import com.example.dietjoggingapp.other.Constants.NOTIFICATION_ID
import com.example.dietjoggingapp.other.Constants.TIMER_UPDATE_INTERVAL
import com.example.dietjoggingapp.other.TrackingUtil
import com.example.dietjoggingapp.ui.MainActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService: LifecycleService() {

    var isFirstRun = true

    @Inject
    lateinit var fusedLocationProviderClient: FusedLocationProviderClient

//    Live data for timeing
    private val timingRungInSeconds = MutableLiveData<Long>()

    @Inject
    lateinit var notificationBuilder: NotificationCompat.Builder

    lateinit var currentNotification: NotificationCompat.Builder

//    var isFIrstRun: Boolean = true
    var isServiceKilled: Boolean = false

    companion object {
        val timingRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
//        val isFirstRun = MutableLiveData<Boolean>()
    }

    private fun postInitValues() {
//        isFirstRun.postValue(true)
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timingRunInMillis.postValue(0L)
        timingRungInSeconds.postValue(0L)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            when(it.action) {
                /* Start Service */
                ACTION_START_OR_RESUME_SERVICE -> {
                    if (isFirstRun) {
                        startForegroundService()
                        isFirstRun = false
                    } else {
                        Timber.d("Resuming Service...")
                        startTimer()
                    }
                    Timber.d("Started or resume service")
                }
                /* Pause Service */
                ACTION_PAUSE_SERVICE -> {
                    Timber.d("Pause Service")
                    pauseService()
                }
                /* Stop Service */
                ACTION_STOP_SERVICE -> {
                    Timber.d("Stop Service")
                    stopService()
                }
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }



    override fun onCreate() {
        super.onCreate()
        currentNotification = notificationBuilder
        postInitValues()
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        isTracking.observe(this, Observer {
            updateLocationTracking(it)
            updateNotificationTracking(it)
        })
    }

    private fun stopService() {
        isServiceKilled = true
        isFirstRun = true
        pauseService()
        postInitValues()
        stopForeground(true)
        stopSelf()
    }

    private fun updateNotificationTracking(isTracking: Boolean) {
        val notifAction = if(isTracking) "Pause" else "Resume"
        val pendingIntent = if(isTracking) {
            val pauseIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_PAUSE_SERVICE
            }
            PendingIntent.getService(this, 1, pauseIntent, FLAG_UPDATE_CURRENT)
        } else {
            val resumeIntent = Intent(this, TrackingService::class.java).apply {
                action = ACTION_START_OR_RESUME_SERVICE
            }
            PendingIntent.getService(this, 2, resumeIntent, FLAG_UPDATE_CURRENT)
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        currentNotification.javaClass.getDeclaredField("mActions").apply {
            isAccessible = true
            set(currentNotification, ArrayList<NotificationCompat.Action>())
        }

        if(!isServiceKilled) {
            currentNotification = notificationBuilder
                .addAction(R.drawable.ic_baseline_pause_24, notifAction, pendingIntent)
            notificationManager.notify(NOTIFICATION_ID, currentNotification.build())
        }
    }

    private fun addEmptyPolyline()  = pathPoints.value?.apply {
        add(mutableListOf())
        pathPoints.postValue(this)
    } ?: pathPoints.postValue(mutableListOf(mutableListOf()))

    private fun addPathPoint(location: Location) {
        location?.let {
            val pos = LatLng(location.latitude, location.longitude)
            pathPoints.value?.apply {
                last().add(pos)
                pathPoints.postValue(this)
            }
        }
    }

    private var isTimerEnabled = false
    private  var lapTime = 0L
    private var timeRun = 0L
    private var timeStarted = 0L
    private var lastSecondTimeStamp = 0L

//    Start Timing
    private fun startTimer() {
        addEmptyPolyline()
        isTracking.postValue(true)
        timeStarted = System.currentTimeMillis()
        isTimerEnabled = false
        CoroutineScope((Dispatchers.Main)).launch {
            while(isTracking.value!!) {
//                Time Differentiation with timeStarted and lapTime
                lapTime = System.currentTimeMillis() - timeStarted

//                Update Value of Live Data
                timingRunInMillis.postValue(timeRun + lapTime)
                if(timingRunInMillis.value!! >= lastSecondTimeStamp + 1000L) {
                    timingRungInSeconds.postValue(timingRungInSeconds.value!! + 1)
                    lastSecondTimeStamp += 1000L
                }
                delay(TIMER_UPDATE_INTERVAL)
            }
            timeRun += lapTime
        }
    }

    private fun pauseService() {
        isTracking.postValue(false)
        isTimerEnabled = false
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationTracking(isTracking: Boolean) {
        if (isTracking) {
            if (TrackingUtil.locationPermissions(this)) {
                val request = com.google.android.gms.location.LocationRequest().apply {
                    interval = LOCATION_UPDATE_INTERVAL
                    fastestInterval = FASTEST_LOCATION_INTERVAL
                    priority = PRIORITY_HIGH_ACCURACY
                }
                fusedLocationProviderClient.requestLocationUpdates(
                    request,
                    locationCallback,
                    Looper.getMainLooper()
                )
            }
        }else {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback)
        }
    }

    val locationCallback = object: LocationCallback() {
        override fun onLocationResult(p0: LocationResult) {
            super.onLocationResult(p0)
            if(isTracking.value!!) {
                p0?.locations?.let { locations ->
                    for(location in locations) {
                        addPathPoint(location)
                        Timber.d("New Location: ${location.latitude} ${location.longitude}")
                    }
                }
            }
        }
    }



    private fun startForegroundService() {
        startTimer()
        isTracking.postValue(true)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            createNotificatoinChannel(notificationManager)
        }

        startForeground(NOTIFICATION_ID, notificationBuilder.build())

        timingRungInSeconds.observe(this, Observer {
            if(!isServiceKilled) {
                val notification = currentNotification
                    .setContentText(TrackingUtil.getFormattedStopWatchTime(it * 1000L))
                notificationManager.notify(NOTIFICATION_ID, notification.build())
            }
        })
    }
    
    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificatoinChannel(notificationManager: NotificationManager) {
        val channel = NotificationChannel(
            ID_CHANNEL_NOTIFICATION,
            NAME_CHANNEL_NOTIFICATION,
            IMPORTANCE_LOW
        )
        notificationManager.createNotificationChannel(channel)
    }


}