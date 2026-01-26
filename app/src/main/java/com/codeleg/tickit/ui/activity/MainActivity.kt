package com.codeleg.tickit.ui.activity

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.codeleg.tickit.R
import com.codeleg.tickit.ui.viewmodel.MainViewModelFactory
import com.codeleg.tickit.database.repository.TodoRepository
import com.codeleg.tickit.database.repository.UserRepository
import com.codeleg.tickit.databinding.ActivityMainBinding
import com.codeleg.tickit.ui.fragment.HomeFragment
import com.codeleg.tickit.ui.fragment.ProfileFragment
import com.codeleg.tickit.ui.viewmodel.MainViewModel
import com.codeleg.tickit.worker.DailyIncompleteTodoWorker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import kotlin.getValue

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val todoRepo: TodoRepository by lazy { TodoRepository() }
    private val userRepo: UserRepository by lazy { UserRepository() }
    private val notificationPermissionLauncher by lazy {
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Notification permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(
                    this,
                    "Notifications disabled. You may miss budget alerts.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
    val viewModelFactory by lazy {
        MainViewModelFactory(todoRepo, userRepo)
    }
    val mainVM: MainViewModel by viewModels { viewModelFactory }

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
        lifecycleScope.launch { createPeriodicWorkRequest() }
        setupBottomNavigation()
        askNotificationPermissionIfNeeded()
    }

    private fun createPeriodicWorkRequest() {
        val dailyWorkRequest = PeriodicWorkRequestBuilder<DailyIncompleteTodoWorker>(
            16,
            TimeUnit.MINUTES
        ).setBackoffCriteria(
            androidx.work.BackoffPolicy.EXPONENTIAL,
            10,
            TimeUnit.SECONDS
        ).build()

        WorkManager.getInstance(applicationContext).enqueueUniquePeriodicWork("daily_incomplete_todo_worker" ,
            ExistingPeriodicWorkPolicy.KEEP , dailyWorkRequest)

        Log.d("codeleg", "Periodic Work Request Created")
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

    private fun askNotificationPermissionIfNeeded() {
        // Only required for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            val permission = Manifest.permission.POST_NOTIFICATIONS

            val isGranted = ContextCompat.checkSelfPermission(
                this,
                permission
            ) == PackageManager.PERMISSION_GRANTED

            if (!isGranted) {
                notificationPermissionLauncher.launch(permission)
            }
        }
    }

    private fun replaceFragment(fragment: Fragment, tag: String) {
        supportFragmentManager.beginTransaction()
            .replace(binding.activityMainContainer.id, fragment, tag)
            .commit()
    }
}
