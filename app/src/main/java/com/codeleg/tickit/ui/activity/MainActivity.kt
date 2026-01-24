package com.codeleg.tickit.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.codeleg.tickit.R
import com.codeleg.tickit.ui.viewmodel.MainViewModelFactory
import com.codeleg.tickit.database.repository.TodoRepository
import com.codeleg.tickit.databinding.ActivityMainBinding
import com.codeleg.tickit.ui.fragment.HomeFragment
import com.codeleg.tickit.ui.fragment.ProfileFragment
import com.codeleg.tickit.ui.viewmodel.MainViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import kotlin.getValue
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val todoRepo : TodoRepository by lazy { TodoRepository() }

    val viewModelFactory by lazy {
        MainViewModelFactory(todoRepo)
    }
    val mainVM: MainViewModel by viewModels {viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // ✅ Correct ViewBinding usage
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        manageInsets()

        // Load default fragment
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment(), "HOME")
        }

        setupBottomNavigation()
    }

    private fun manageInsets() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.activity_main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0)
            insets
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.option_home -> {
                    replaceFragment(HomeFragment(), "HOME")
                    true
                }
                R.id.option_profile -> {
                    replaceFragment(ProfileFragment(), "PROFILE")
                    true
                }
                else -> false
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(binding.activityMainContainer.id, fragment, tag)
            .commit()
    }
}
