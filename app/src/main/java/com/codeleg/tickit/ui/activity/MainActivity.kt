package com.codeleg.tickit.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codeleg.tickit.R
import com.codeleg.tickit.databinding.ActivityMainBinding
import com.codeleg.tickit.ui.fragment.HomeFragment
import com.codeleg.tickit.ui.viewmodel.MainViewModel
import com.google.firebase.database.FirebaseDatabase
import kotlin.getValue

class MainActivity : AppCompatActivity() {
    private val mainVM: MainViewModel by viewModels()
    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)
        manageInsets()
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
     if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().apply {
                replace(binding.activityMainContainer.id, HomeFragment())
            }.commit()
        }
        mainVM.loadTodos()
    }

    private fun manageInsets() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}