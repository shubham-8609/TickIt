package com.codeleg.tickit.ui.fragment

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.codeleg.tickit.R
import com.codeleg.tickit.databinding.FragmentSignUpBinding
import com.codeleg.tickit.ui.activity.MainActivity
import com.codeleg.tickit.ui.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialFade

class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val authVM: AuthViewModel by activityViewModels()
    private lateinit var loadingDialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFade()
        exitTransition = MaterialFade()
        setupLoadingDialog()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(layoutInflater, container, false)

        binding.tvLoginRedirect.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, LoginFragment())
                .commit()
        }
        binding.btnSignUp.setOnClickListener { validateInputs() }
        return binding.root
    }


    private fun setupLoadingDialog() {
        loadingDialog = Dialog(requireContext())
        loadingDialog.setContentView(R.layout.dialog_loading)
        loadingDialog.setCancelable(false)
        loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }

    private fun validateInputs() {
        val username = binding.etUsername.text.toString()
        val email = binding.etEmail.text.toString()
        val password = binding.etPassword.text.toString()
        val confirmPassword = binding.etCmfPassword.text.toString()

        if (username.isBlank()) {
            binding.etUsername.error = "Invalid Username"
            binding.etUsername.requestFocus()
            return
        }
        if (email.isBlank()) {
            binding.etEmail.error = "Invalid Email"
            binding.etEmail.requestFocus()
            return
        }
        if (password.isBlank()) {
            binding.etPassword.error = "Invalid  Password"
            binding.etPassword.requestFocus()
            return
        }
        if (password != confirmPassword) {
            binding.etCmfPassword.error = "Passwords doesn't  match"
            binding.etCmfPassword.requestFocus()
            return
        }

        loadingDialog.show()
        authVM.signUp(username, email, password) { isSuccessful, errorMsg ->
            loadingDialog.dismiss()
            if (isSuccessful) {
                Toast.makeText(requireContext() , "Sign Up Successful", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireActivity(), MainActivity::class.java))
                requireActivity().finish()
            }else{
                Snackbar.make(binding.root , errorMsg ?: "Sign Up Failed", Snackbar.LENGTH_LONG).show()
            }

        }

    }
}