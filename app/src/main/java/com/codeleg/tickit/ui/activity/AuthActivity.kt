package com.codeleg.tickit.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codeleg.tickit.databinding.ActivityAuthBinding
import com.codeleg.tickit.ui.fragment.LoginFragment
import com.codeleg.tickit.ui.viewmodel.AuthViewModel
import androidx.activity.viewModels


class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private  val authVM: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        manageInsets()
        if (authVM.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction().apply {
                replace(binding.authContainer.id , LoginFragment())
            }.commit()
        }
    }
    private fun manageInsets() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}