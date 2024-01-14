package com.example.dietjoggingapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.databinding.ActivityMainBinding
import com.example.dietjoggingapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var navHostController: NavController

//    @Inject
//    lateinit var joggingDAO: JoggingDAO
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        navigateTrackingFragmentIfNeeded(intent)
        navHostController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)

        val appBar = AppBarConfiguration(
            setOf(
                R.id.joggingsFragment, R.id.setiingsFragment, R.id.foodsFragment
            )
        )
    setupActionBarWithNavController(navHostController, appBar)
    navView.setupWithNavController(navHostController)
//        Test Inject JoggingDAO
//        Log.d(TAG, "onCreate: joggingDao${joggingDAO.hashCode()}")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateTrackingFragmentIfNeeded(intent)
    }

    private fun navigateTrackingFragmentIfNeeded(intent: Intent?) {
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navHostController.navigate(R.id.action_global_trackingFragment)
        }
    }
}