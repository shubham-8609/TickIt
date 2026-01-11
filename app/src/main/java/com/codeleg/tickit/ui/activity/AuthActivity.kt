package com.codeleg.tickit.ui.activity

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.codeleg.tickit.databinding.ActivityAuthBinding
import com.codeleg.tickit.ui.fragment.LoginFragment
import com.codeleg.tickit.ui.viewmodel.AuthViewModel
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import com.codeleg.tickit.R
import com.codeleg.tickit.utils.AuthUiState
import kotlinx.coroutines.launch


class AuthActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAuthBinding
    private  val authVM: AuthViewModel by viewModels()
    private lateinit var loadingDialog: Dialog
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupLoadingDialog()
        lifecycleScope.launch {
        manageInsets()
        if(savedInstanceState == null){
            supportFragmentManager.beginTransaction().apply {
                replace(binding.authContainer.id , LoginFragment())
            }.commit()
        }
        }
        if (authVM.isUserLoggedIn()) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
        lifecycleScope.launchWhenStarted {
            authVM.authState.collect { state ->
                when(state){
                    is AuthUiState.Loading -> loadingDialog.show()
                    is AuthUiState.Success -> navigateToHome()
                    is AuthUiState.Error -> {
                        loadingDialog.dismiss()
                        Toast.makeText(this@AuthActivity , state.message , Toast.LENGTH_SHORT)

                    }
                    AuthUiState.Idle -> Unit
                }
            }
        }
    }

    private fun navigateToHome(){
        loadingDialog.dismiss()
        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this , MainActivity::class.java))
        finish()
    }
    private fun setupLoadingDialog() {
        loadingDialog = Dialog(this)
        loadingDialog.setContentView(R.layout.dialog_loading)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
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