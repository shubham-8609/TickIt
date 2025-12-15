package com.codeleg.tickit.ui.activity

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codeleg.tickit.R
import com.codeleg.tickit.databinding.ActivityAuthBinding
import com.codeleg.tickit.ui.fragment.LoginFragment
import com.codeleg.tickit.ui.fragment.SignUpFragment

class AuthActivity : AppCompatActivity() {
    lateinit var binding: ActivityAuthBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        manageInsects()

        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction().apply {
                replace(binding.authContainer.id , LoginFragment())
            }.commit()
        }
    }
    private fun manageInsects() {
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}