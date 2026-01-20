package com.codeleg.tickit.ui.fragment

import android.content.Context
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
import com.codeleg.tickit.database.local.ThemePreferences
import com.codeleg.tickit.databinding.FragmentProfileBinding
import com.codeleg.tickit.databinding.LayoutUpdatePassBinding
import com.codeleg.tickit.ui.activity.AuthActivity
import com.codeleg.tickit.ui.viewmodel.MainViewModel
import com.codeleg.tickit.utils.ThemeMode
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager

import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.IntArraySerializer
import androidx.core.content.edit
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import com.codeleg.tickit.database.local.ThemeKeys

class ProfileFragment : Fragment() {

    private  var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val mainVM: MainViewModel by activityViewModels()
    private var isUserThemeToggle = false
    private var isUserDynamicToggle = false
    private var isAppearanceExpanded = false




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)

        observeTheme()
        observeDynamicColors()

        setupThemeChangeListener()
        setupDynamicColorListener()

        populateData()
        mainVM.loadUsername()

        binding.btnLogout.setOnClickListener { logout() }
        binding.itemDeleteTodos.setOnClickListener { deleteAllTodos() }
        binding.itemUpdatePassword.setOnClickListener { updatePassLogic() }
        binding.itemDeleteAccount.setOnClickListener { deleteAccountLogic() }
        lifecycleScope.launch {
            delay(150)
            binding.layoutAppearanceContent.visibility  = View.GONE
        }
        binding.cardAppearanceHeader.setOnClickListener {
            isAppearanceExpanded = !isAppearanceExpanded

            TransitionManager.beginDelayedTransition(
                binding.root as ViewGroup,
                AutoTransition()
            )

            binding.layoutAppearanceContent.visibility =
                if (isAppearanceExpanded) View.VISIBLE else View.GONE

            binding.ivAppearanceArrow.animate()
                .rotation(if (isAppearanceExpanded) 180f else 0f)
                .setDuration(200)
                .start()
        }

        return binding.root
    }


    private fun observeTheme() {
        viewLifecycleOwner.lifecycleScope.launch {
            ThemePreferences.getTheme(requireContext()).collect { mode ->
                updateThemeSelection(mode)
            }
        }
    }


    private fun observeDynamicColors() {
        viewLifecycleOwner.lifecycleScope.launch {
            ThemePreferences.isDynamicColorsEnabled(requireContext()).collect { enabled ->
                binding.switchDynamicColors.isChecked = enabled
            }
        }
    }


    private fun setupThemeChangeListener() {
        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
        if(!isUserThemeToggle){
                isUserThemeToggle = true
                return@setOnCheckedChangeListener
        }
            val selectedMode = when (checkedId) {
                R.id.rbLight -> ThemeMode.LIGHT
                R.id.rbDark -> ThemeMode.DARK
                else -> ThemeMode.SYSTEM
            }

            viewLifecycleOwner.lifecycleScope.launch {
                ThemePreferences.setTheme(requireContext(), selectedMode)
            }

            showThemeWarning()
        }
    }

    private fun setupDynamicColorListener() {

        binding.switchDynamicColors.setOnCheckedChangeListener { _, isChecked ->
            if(!isUserDynamicToggle){
                isUserDynamicToggle = true
                return@setOnCheckedChangeListener
            }
            viewLifecycleOwner.lifecycleScope.launch {
                ThemePreferences.setDynamicColors(requireContext(), isChecked)
            }

            showThemeWarning()
        }
    }
    private fun updateThemeSelection(mode: ThemeMode) {
        when (mode) {
            ThemeMode.SYSTEM -> binding.rbSystem.isChecked = true
            ThemeMode.LIGHT -> binding.rbLight.isChecked = true
            ThemeMode.DARK -> binding.rbDark.isChecked = true
        }
    }



    private fun showThemeWarning() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Apply theme")
            .setMessage(
                "The app needs to restart to apply the selected theme. " +
                        "This won’t affect your data."
            )

            .show()
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