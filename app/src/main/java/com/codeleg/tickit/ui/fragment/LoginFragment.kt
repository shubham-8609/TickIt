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
import androidx.lifecycle.lifecycleScope
import com.codeleg.tickit.R
import com.codeleg.tickit.databinding.ForgetPassLayoutBinding
import com.codeleg.tickit.databinding.FragmentLoginBinding
import com.codeleg.tickit.ui.activity.MainActivity
import com.codeleg.tickit.ui.viewmodel.AuthViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.transition.platform.MaterialFade
import kotlinx.coroutines.launch

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
        binding.tvForgotPassword.setOnClickListener {
            val dialogBinding = ForgetPassLayoutBinding.inflate(layoutInflater)
            val dialog = BottomSheetDialog(requireContext())
            dialog.setContentView(dialogBinding.root)
            dialogBinding.btnSend.setOnClickListener {
                val email = dialogBinding.etEmail.text.toString()
                if(email.isBlank()) {
                    dialogBinding.etEmail.error = "Invalid Email"
                    dialogBinding.etEmail.requestFocus()
                    return@setOnClickListener
                }
                loadingDialog.show()
                lifecycleScope.launch {
                    val result = authVM.sendPassResetLink(email)
                    loadingDialog.dismiss()
                    result.onSuccess {
                        Toast.makeText(requireContext() , "Password reset link sent to your email." , Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                    result.onFailure { exception ->
                        Snackbar.make(binding.root , exception.message ?: "Failed to send reset link", Snackbar.LENGTH_SHORT).show()
                    }
                }
            }
            dialog.show()
        }

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

    lifecycleScope.launch {
        val result = authVM.login(email, password)
        loadingDialog.dismiss()
        result.onSuccess {
            Toast.makeText(requireContext() , "Login Successful", Toast.LENGTH_SHORT).show()
            startActivity(Intent(requireContext() , MainActivity::class.java))
            requireActivity().finish()
        }
        result.onFailure { exception ->
            Snackbar.make(binding.root , exception.message ?: "Login Failed", Snackbar.LENGTH_SHORT).show()
        }
    }
}
}
