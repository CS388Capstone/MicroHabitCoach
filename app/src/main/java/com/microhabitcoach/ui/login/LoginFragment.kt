package com.microhabitcoach.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.microhabitcoach.R
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.databinding.FragmentLoginBinding
import kotlinx.coroutines.launch

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViews()
    }

    private fun setupViews() {
        binding.etPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptLogin()
                true
            } else {
                false
            }
        }

        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.tvSignUp.setOnClickListener {
            // For now, just show a message - can be implemented later
            Snackbar.make(binding.root, R.string.signup_coming_soon, Snackbar.LENGTH_SHORT).show()
        }

        // Clear errors when user starts typing
        binding.etUsername.doAfterTextChanged {
            binding.tilUsername.error = null
        }
        binding.etPassword.doAfterTextChanged {
            binding.tilPassword.error = null
        }
    }

    private fun attemptLogin() {
        val username = binding.etUsername.text?.toString()?.trim().orEmpty()
        val password = binding.etPassword.text?.toString().orEmpty()

        // Basic validation
        var hasError = false

        if (username.isEmpty()) {
            binding.tilUsername.error = getString(R.string.error_required)
            hasError = true
        }

        if (password.isEmpty()) {
            binding.tilPassword.error = getString(R.string.error_required)
            hasError = true
        }

        if (hasError) {
            return
        }

        // Mock authentication - for now, accept any non-empty username/password
        // In production, this would check against a backend or local database
        performLogin(username, password)
    }

    private fun performLogin(username: String, password: String) {
        // Mock login - always succeeds
        // Save username if needed (can use SharedPreferences or database)
        navigateToOnboardingOrMain()
    }

    private fun navigateToOnboardingOrMain() {
        // Check if onboarding is already completed
        lifecycleScope.launch {
            try {
                val database = DatabaseModule.getDatabase(requireContext())
                val dao = database.userPreferencesDao()
                val preferences = dao.getUserPreferences()
                
                if (preferences?.hasCompletedOnboarding == true) {
                    // Skip onboarding, go directly to Today
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToTodayFragment()
                    )
                } else {
                    // Go to onboarding
                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToOnboardingFragment()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Default to onboarding on error
                findNavController().navigate(
                    LoginFragmentDirections.actionLoginFragmentToOnboardingFragment()
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

