package com.codeleg.tickit.ui.fragment

import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import com.codeleg.tickit.R
import com.codeleg.tickit.databinding.FragmentLoginBinding
import com.codeleg.tickit.ui.viewmodel.AuthViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialFade

class LoginFragment : Fragment() {

    var _binding: FragmentLoginBinding? = null
    val binding get() = _binding!!
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
        _binding = FragmentLoginBinding.inflate(layoutInflater , container , false)

        binding.tvSignUpRedirect.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.auth_container, SignUpFragment())
                .commit()
        }
        binding.btnLogin.setOnClickListener { validateInputs() }


        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private fun setupLoadingDialog() {
    loadingDialog = Dialog(requireContext())
    loadingDialog.setContentView(R.layout.dialog_loading)
    loadingDialog.setCancelable(false)
    loadingDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }


private fun validateInputs() {
   val email = binding.etEmail.text.toString()
    val password = binding.etPassword.text.toString()
    if(email.isBlank()){
        binding.etEmail.error = "Invalid Email"
        binding.etEmail.requestFocus()
        return
    }
    if(password.isBlank()) {
        binding.etPassword.error = "Invalid Password"
        binding.etPassword.requestFocus()
        return
    }

    loadingDialog.show()
    authVM.login(email, password) { isSuccessful, errorMsg ->

        loadingDialog.dismiss()
        if(isSuccessful){
            Toast.makeText(requireContext() , "Login Successful", Toast.LENGTH_SHORT).show()
        } else {
            Snackbar.make(binding.root , errorMsg ?: "Login Failed", Snackbar.LENGTH_SHORT).show()
        }
    }



}
}
