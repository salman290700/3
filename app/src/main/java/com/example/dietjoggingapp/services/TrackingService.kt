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
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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
import com.example.dietjoggingapp.database.domains.ActivityClassified
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
import java.math.BigDecimal
import javax.inject.Inject

typealias Polyline = MutableList<LatLng>
typealias Polylines = MutableList<Polyline>

@AndroidEntryPoint
class TrackingService: LifecycleService(), SensorEventListener {

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

    companion object {
        val timingRunInMillis = MutableLiveData<Long>()
        val isTracking = MutableLiveData<Boolean>()
        val pathPoints = MutableLiveData<Polylines>()
        var isStart = MutableLiveData<BigDecimal>()
//        val isFirstRun = MutableLiveData<Boolean>()
    }

    private fun postInitValues() {
//        isFirstRun.postValue(true)
        isTracking.postValue(false)
        pathPoints.postValue(mutableListOf())
        timingRunInMillis.postValue(0L)
        timingRungInSeconds.postValue(0L)
        isStart.postValue((round(results?.get(2)!!.toFloat(), 2)))
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

                    ax = arrayListOf()
                    ay = arrayListOf()
                    az = arrayListOf()

                    gx = arrayListOf()
                    gy = arrayListOf()
                    gz = arrayListOf()

                    lx = arrayListOf()
                    ly = arrayListOf()
                    lz = arrayListOf()

                    sensorManager = this.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                    mAccelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
                    mGroScope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)!!
                    mLinearAcceleration = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)!!

                    activityClassifier = ActivityClassified(this.applicationContext)

                    sensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST)
                    sensorManager.registerListener(this, mGroScope, SensorManager.SENSOR_DELAY_FASTEST)
                    sensorManager.registerListener(this, mLinearAcceleration, SensorManager.SENSOR_DELAY_FASTEST)
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

    private fun predictActivity() {
        var data = java.util.ArrayList<Float>()

        if(ax.size >= TIME_STAMP && ay.size >= TIME_STAMP && az.size >= TIME_STAMP &&
            gx.size >= TIME_STAMP && gy.size >= TIME_STAMP && gz.size >= TIME_STAMP &&
            lx.size >= TIME_STAMP && ly.size >= TIME_STAMP && lz.size >= TIME_STAMP
        ) {

            data.addAll(ax.subList(0, TIME_STAMP))
            data.addAll(ay.subList(0, TIME_STAMP))
            data.addAll(az.subList(0, TIME_STAMP))

            data.addAll(gx.subList(0, TIME_STAMP))
            data.addAll(gy.subList(0, TIME_STAMP))
            data.addAll(gz.subList(0, TIME_STAMP))

            data.addAll(lx.subList(0, TIME_STAMP))
            data.addAll(ly.subList(0, TIME_STAMP))
            data.addAll(lz.subList(0, TIME_STAMP))
            val list= data.toFloatArray()
            var list2: kotlin.collections.ArrayList<Float> = arrayListOf()

            results = activityClassifier.predictProbability(data = list)

            data?.toMutableList()?.clear()
            ax.clear()
            ay.clear()
            az.clear()

            gx.clear()
            gy.clear()
            gz.clear()

            lx.clear()
            ly.clear()
            lz.clear()

        }
    }

    private fun round(value: Float, decimal_places: Int): BigDecimal {
        var bigDecimal: BigDecimal = BigDecimal(value.toString())
        bigDecimal = bigDecimal.setScale(decimal_places, BigDecimal.ROUND_HALF_UP);
        return bigDecimal
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

    override fun onSensorChanged(event: SensorEvent?) {
        var sensor = event?.sensor

        if(sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            event?.values?.get(0)?.let { ax.add(it) }
            event?.values?.get(1)?.let { ay.add(it) }
            event?.values?.get(2)?.let { az.add(it) }
        }else if(sensor?.type == Sensor.TYPE_GYROSCOPE) {
            event?.values?.get(0)?.let { gx.add(it) }
            event?.values?.get(1)?.let { gy.add(it) }
            event?.values?.get(2)?.let { gz.add(it) }
        } else {
            event?.values?.get(0)?.let { lx.add(it) }
            event?.values?.get(1)?.let { ly.add(it) }
            event?.values?.get(2)?.let { lz.add(it) }
        }

        predictActivity()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        TODO("Not yet implemented")
    }


}