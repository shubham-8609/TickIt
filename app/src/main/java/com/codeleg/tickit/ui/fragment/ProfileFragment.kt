package com.codeleg.tickit.ui.fragment

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.codeleg.tickit.R
import com.codeleg.tickit.databinding.FragmentProfileBinding
import com.codeleg.tickit.databinding.LayoutUpdatePassBinding
import com.codeleg.tickit.ui.activity.AuthActivity
import com.codeleg.tickit.ui.viewmodel.MainViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.IntArraySerializer

class ProfileFragment : Fragment() {

    private  var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val mainVM: MainViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        populateData()
        mainVM.loadUsername()
        binding.btnLogout.setOnClickListener { logout() }
        binding.itemDeleteTodos.setOnClickListener {
            deleteAllTodos()
        }
        binding.itemUpdatePassword.setOnClickListener {
            updatePassLogic()
        }
        binding.itemDeleteAccount.setOnClickListener { deleteAccountLogic() }
        return binding.root
    }

    private fun deleteAccountLogic() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { dialog, _ ->
                dialog.dismiss()
                lifecycleScope.launch {
                    val result = mainVM.deleteAccount()
                    if (result.isSuccess) {
                        startActivity(Intent(requireActivity(), AuthActivity::class.java))
                        Toast.makeText(requireContext() , "Account deleted successfully", Toast.LENGTH_SHORT).show()
                        requireActivity().finish()
                    } else {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Error")
                            .setMessage(result.exceptionOrNull()?.localizedMessage ?: "Unknown error occurred")
                            .setPositiveButton("OK") { errDialog, _ ->
                                errDialog.dismiss()
                            }
                            .show()
                    }
                }
            }
            .show()
    }

    private fun updatePassLogic() {
        val dialogBinding = LayoutUpdatePassBinding.inflate(layoutInflater)
        val dialog = BottomSheetDialog(requireContext())
        dialog.setContentView(dialogBinding.root)
        dialogBinding.btnUpdatePassword.setOnClickListener {
            val newPass = dialogBinding.etNewPassword.text.toString()
            val oldPass = dialogBinding.etOldPassword.text.toString()
            if (newPass.isEmpty() || oldPass.isEmpty()) {
                dialogBinding.etNewPassword.error = "Fields cannot be empty"
                return@setOnClickListener
            }
            if(newPass.length < 6){
                dialogBinding.etNewPassword.error = "Password must be at least 6 characters long"
                return@setOnClickListener
            }
            lifecycleScope.launch {
                val result = mainVM.udpatePass(oldPass, newPass)
                if (result.isSuccess) {
                    dialog.dismiss()
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Success")
                        .setMessage("Password updated successfully.")
                        .setPositiveButton("OK") { successDialog, _ ->
                            successDialog.dismiss()
                        }
                        .show()
                } else {
                    MaterialAlertDialogBuilder(requireContext())
                        .setTitle("Error")
                        .setMessage(result.exceptionOrNull()?.localizedMessage ?: "Unknown error occurred")
                        .setPositiveButton("OK") { errDialog, _ ->
                            errDialog.dismiss()
                        }
                        .show()
                }
            }


        }
        dialog.show()
    }

    private fun deleteAllTodos() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete All Todos")
            .setMessage("Are you sure you want to delete all your todos? This action cannot be undone.")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Delete") { dialog, _ ->
                dialog.dismiss()
                mainVM.deleteAllTodos { success, errorMsg ->
                    if (success) {
                        populateData()
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Success")
                            .setMessage("All todos have been deleted.")
                            .setPositiveButton("OK") { successDialog, _ ->
                                successDialog.dismiss()
                            }
                            .show()
                    } else {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle("Error")
                            .setMessage(errorMsg ?: "Unknown error occurred")
                            .setPositiveButton("OK") { errDialog, _ ->
                                errDialog.dismiss()
                            }
                            .show()
                    }
                }
            }
            .show()
    }

    private fun logout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton("Logout") { dialog, _ ->

                dialog.dismiss()
                mainVM.logout()
                startActivity(Intent(requireActivity(), AuthActivity::class.java))
                requireActivity().finish()
            }
            .show()

    }

    private fun populateData() {
        mainVM.username.observe(viewLifecycleOwner) { username ->
            binding.tvUsername.text = username
        }
        val user = mainVM.getCurrentUser()
        binding.tvEmail.text = user?.email ?: "No Email"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}