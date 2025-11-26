package com.microhabitcoach.ui.onboarding

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.microhabitcoach.R
import com.microhabitcoach.data.database.DatabaseModule
import com.microhabitcoach.databinding.FragmentOnboardingBinding
import com.microhabitcoach.ui.onboarding.OnboardingStep.Companion.TOTAL_STEPS
import kotlinx.coroutines.launch

class OnboardingFragment : Fragment() {

    private var _binding: FragmentOnboardingBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: OnboardingAdapter

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        // Handle permission result if needed
        // For now, we continue regardless
        moveToNextStep()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOnboardingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupViewPager()
        setupControls()
        updateProgress(0)
    }

    private fun setupViewPager() {
        adapter = OnboardingAdapter(this) { step ->
            handleStepAction(step)
        }
        binding.viewPagerOnboarding.adapter = adapter

        binding.viewPagerOnboarding.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateProgress(position)
                updateControls(position)
            }
        })
    }

    private fun setupControls() {
        binding.btnSkip.setOnClickListener {
            skipOnboarding()
        }

        binding.btnNext.setOnClickListener {
            val currentStep = binding.viewPagerOnboarding.currentItem
            val step = OnboardingStep.fromPosition(currentStep)
            handleStepAction(step)
        }
    }

    private fun handleStepAction(step: OnboardingStep) {
        when (step) {
            OnboardingStep.WELCOME,
            OnboardingStep.TEMPLATES -> {
                moveToNextStep()
            }
            OnboardingStep.PERMISSION_MOTION -> {
                requestActivityRecognitionPermission()
            }
            OnboardingStep.PERMISSION_LOCATION -> {
                requestLocationPermission()
            }
            OnboardingStep.PERMISSION_NOTIFICATIONS -> {
                requestNotificationPermission()
            }
        }
    }

    private fun requestActivityRecognitionPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            } else {
                moveToNextStep()
            }
        } else {
            moveToNextStep()
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            moveToNextStep()
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            } else {
                completeOnboarding()
            }
        } else {
            completeOnboarding()
        }
    }

    private fun moveToNextStep() {
        val currentItem = binding.viewPagerOnboarding.currentItem
        if (currentItem < TOTAL_STEPS - 1) {
            binding.viewPagerOnboarding.currentItem = currentItem + 1
        } else {
            completeOnboarding()
        }
    }

    private fun updateProgress(position: Int) {
        val progress = ((position + 1) * 100) / TOTAL_STEPS
        binding.progressIndicator.progress = progress
    }

    private fun updateControls(position: Int) {
        val isLastStep = position == TOTAL_STEPS - 1
        binding.btnNext.text = if (isLastStep) {
            getString(R.string.finish)
        } else {
            getString(R.string.next)
        }

        // Hide skip on last step
        binding.btnSkip.visibility = if (isLastStep) View.GONE else View.VISIBLE
    }

    private fun skipOnboarding() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.skip_onboarding)
            .setMessage(R.string.skip_onboarding_message)
            .setPositiveButton(R.string.skip) { _, _ ->
                completeOnboarding()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun completeOnboarding() {
        // Mark onboarding as complete
        lifecycleScope.launch {
            try {
                val database = DatabaseModule.getDatabase(requireContext())
                val dao = database.userPreferencesDao()
                val preferences = dao.getUserPreferences() ?: com.microhabitcoach.data.database.entity.UserPreferences()
                
                val updated = preferences.copy(
                    hasCompletedOnboarding = true,
                    updatedAt = System.currentTimeMillis()
                )
                dao.insertUserPreferences(updated)
                
                // Navigate to Today screen
                findNavController().navigate(
                    OnboardingFragmentDirections.actionOnboardingFragmentToTodayFragment()
                )
            } catch (e: Exception) {
                e.printStackTrace()
                // Navigate anyway if save fails
                findNavController().navigate(
                    OnboardingFragmentDirections.actionOnboardingFragmentToTodayFragment()
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

