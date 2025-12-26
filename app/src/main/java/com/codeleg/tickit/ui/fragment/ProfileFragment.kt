package com.codeleg.tickit.ui.fragment

import android.content.Intent
import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.codeleg.tickit.R
import com.codeleg.tickit.databinding.FragmentProfileBinding
import com.codeleg.tickit.ui.activity.AuthActivity
import com.codeleg.tickit.ui.viewmodel.MainViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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
        binding.btnLogout.setOnClickListener { logout() }
        binding.itemDeleteTodos.setOnClickListener {
            deleteAllTodos()
        }
        return binding.root
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

        // show alert dialog to confirm logout
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
        val username = mainVM.getUsername()
        val user = mainVM.getCurrentUser()
        binding.tvUsername.text = username
        binding.tvEmail.text = user?.email ?: "No Email"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}