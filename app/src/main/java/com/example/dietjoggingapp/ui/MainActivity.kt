package com.example.dietjoggingapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.databinding.ActivityMainBinding
import com.example.dietjoggingapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
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
        navigateTrackingFragmentIfNeeded(intent)
        navHostController = findNavController(R.id.navHostFragmentContainer)
        setSupportActionBar(binding.toolbar)
        binding.navigationView.setupWithNavController(navHostController)
        navHostController.addOnDestinationChangedListener{_, destination, _ ->
            when(destination.id){
                R.id.setiingsFragment, R.id.joggingFragments, R.id.statisticsFragment ->
                    binding.navigationView.visibility = View.VISIBLE
                else ->binding.navigationView.visibility = View.GONE
            }
        }
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