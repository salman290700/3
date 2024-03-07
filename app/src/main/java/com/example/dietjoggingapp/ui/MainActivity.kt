package com.example.dietjoggingapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toolbar
import androidx.appcompat.app.ActionBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.dietjoggingapp.R
import com.example.dietjoggingapp.databinding.ActivityMainBinding
import com.example.dietjoggingapp.other.Constants.ACTION_SHOW_TRACKING_FRAGMENT
import com.example.dietjoggingapp.ui.Fragments.TrackingFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    val firebaseUser = FirebaseAuth.getInstance().currentUser
    val sessionId: String = firebaseUser?.uid.toString().trim()
//    @Inject
//    lateinit var joggingDAO: JoggingDAO
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loginFirst()

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
//    Fragment Transaction
        val fragmentManager: FragmentManager = supportFragmentManager
        val fragmentTransaction: FragmentTransaction = fragmentManager.beginTransaction()
        val trackingFragment = TrackingFragment()
        val bundle = Bundle()
        bundle.putString("EXTRA_USER_SESSION", sessionId)
        trackingFragment.arguments = bundle
        val navView: BottomNavigationView = binding.navView
        navigateTrackingFragmentIfNeeded(intent)
        val navHostController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)

        val appBar = AppBarConfiguration(
            setOf(
                R.id.joggingsFragment, R.id.foodsFragment, R.id.AccountFragment
            )
        )

    setupActionBarWithNavController(navHostController, appBar)
    navView.setupWithNavController(navHostController)
//        Test Inject JoggingDAO
//        Log.d(TAG, "onCreate: joggingDao${joggingDAO.hashCode()}")
    }
    fun loginFirst () {
        Log.d("TAG", "loginFirst: " + sessionId.toString())
        if (firebaseUser == null) {
            val intent: Intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateTrackingFragmentIfNeeded(intent)
    }

    private fun navigateTrackingFragmentIfNeeded(intent: Intent?) {
        val navHostController = findNavController(R.id.nav_host_fragment_activity_bottom_navigation)
        if(intent?.action == ACTION_SHOW_TRACKING_FRAGMENT) {
            navHostController.navigate(R.id.action_global_trackingFragment)
        }
    }
}