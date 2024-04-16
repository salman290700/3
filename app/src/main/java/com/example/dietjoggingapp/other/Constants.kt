package com.example.dietjoggingapp.other

import android.graphics.Color
import com.example.dietjoggingapp.services.Polyline

object Constants {
    const val RUNNING_DATABASE_NAME = "running_db"
    const val REQUEST_CODE_LOCATION_PERMISSION = 0
    const val ACTION_START_OR_RESUME_SERVICE ="ACTION_START_OR_RESUME_SERVICE"
    const val ACTION_PAUSE_SERVICE ="ACTION_PAUSE_SERVICE"
    const val ACTION_STOP_SERVICE ="ACTION_STOP_SERVICE"
    const val ID_CHANNEL_NOTIFICATION = "jogging_tracking"
    const val NAME_CHANNEL_NOTIFICATION = "tracking_notif"
    const val ACTION_SHOW_TRACKING_FRAGMENT = "ACTION_SHOW_TRACKING_FRAGMENT"
    const val NOTIFICATION_ID = 1
    const val LOCATION_UPDATE_INTERVAL = 5000L
    const val FASTEST_LOCATION_INTERVAL = 2000L
    const val POLYLINE_COLOR = Color.RED
    const val POLYLINE_WIDTH = 8F
    const val MAP_ZOOM = 15f

    const val TIMER_UPDATE_INTERVAL = 50L

    const val SHARED_PREFEREMCES_NAME = "sharedPref"
    const val KEY_FIRST_TIME_TOGGLE = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_NAME = "KEY_FIRST_TIME_TOGGLE"
    const val KEY_WEIGHT = "KEY_FIRST_TIME_TOGGLE"

    object FirestoreTable {
        val PROJECT = "PROJECTS"
        val USERS = "USERS"
        val PROJECT_PHOTO = "PROJECT_PHOTO"
        val PROJECT_COLLABORATION = "PROJECT_COLLABORATION"
        val JOGGING = "JOGGING"
        val FOOD = "FOODS"
    }

    object FireStoreDocumentField{
        val DATE = "date"
        val USER_ID = "userId"

    }

    object sharedPreferences {
        val LOCAL_SHARED_PREF = "local_shared_pref"
        val USER_SESSION = "user_session"
    }

    object FireDatabase {
        val IDECABE22 = "idecabe"
    }

    object FirebaseStorageConstants {
        val ROOT_DIRECTORY = "app"
        val PROJECT_ICON = "project_icon"
        val PROJECT_PHOTO = "project_photo"
        val PROFILE_PHOTO = "profile_photo"
    }


    enum class HomeTabs(val index: Int, val key: String){
        PROJECTS(0, "PROJECTS")
    }
}