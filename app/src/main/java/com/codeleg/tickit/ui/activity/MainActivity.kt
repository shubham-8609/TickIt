package com.codeleg.tickit.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.codeleg.tickit.R
import com.codeleg.tickit.databinding.ActivityMainBinding
import com.codeleg.tickit.ui.fragment.HomeFragment
import com.codeleg.tickit.ui.fragment.ProfileFragment
import com.codeleg.tickit.ui.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import kotlin.getValue

class MainActivity : AppCompatActivity() {
    private val homeFragment = HomeFragment()
    private val profileFragment = ProfileFragment()
    private var activeFragment: Fragment = homeFragment

    private val mainVM: MainViewModel by viewModels()
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        binding = ActivityMainBinding.inflate(layoutInflater)
        manageInsets()
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(binding.activityMainContainer.id, HomeFragment())
            }.commit()
        }
        setupFragments()
        setupBottomNavigation()
    }

    private fun manageInsets() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }


    private fun setupFragments() {
        supportFragmentManager.beginTransaction()
            .add(R.id.activity_main_container, profileFragment, "PROFILE")
            .hide(profileFragment)
            .add(R.id.activity_main_container, homeFragment, "HOME")
            .commit()

        activeFragment = homeFragment
    }

    private fun switchFragment(target: Fragment) {
        if (activeFragment == target) return

        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(target)
            .commit()

        activeFragment = target
    }


    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.option_home -> switchFragment(homeFragment)
                R.id.option_profile -> switchFragment(profileFragment)
            }
            true
        }
    }

}