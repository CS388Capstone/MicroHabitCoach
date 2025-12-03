package com.microhabitcoach.ui.settings

import android.app.TimePickerDialog
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.microhabitcoach.R
import com.microhabitcoach.databinding.FragmentSettingsBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels {
        SettingsViewModel.Factory(requireActivity().application)
    }

    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeViewModel()
        setupClickListeners()
    }

    private fun setupUI() {
        // Set app version
        try {
            val packageInfo = requireContext().packageManager.getPackageInfo(
                requireContext().packageName, 0
            )
            val versionName = packageInfo.versionName
            val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                packageInfo.longVersionCode.toString()
            } else {
                @Suppress("DEPRECATION")
                packageInfo.versionCode.toString()
            }
            binding.appVersionText.text = "$versionName ($versionCode)"
        } catch (e: Exception) {
            binding.appVersionText.text = "Unknown"
        }
    }

    private fun observeViewModel() {
        // Observe preferences
        viewModel.preferences.observe(viewLifecycleOwner) { preferences ->
            preferences?.let {
                binding.notificationsSwitch.isChecked = it.notificationsEnabled
                binding.batteryOptimizationSwitch.isChecked = it.batteryOptimizationMode
                
                // Update Quiet Hours buttons
                binding.quietHoursStartButton.text = it.quietHoursStart ?: getString(R.string.quiet_hours_not_set)
                binding.quietHoursEndButton.text = it.quietHoursEnd ?: getString(R.string.quiet_hours_not_set)
            }
        }

        // Observe permission status
        viewModel.motionPermissionStatus.observe(viewLifecycleOwner) { granted ->
            binding.motionPermissionStatus.text = if (granted) {
                getString(R.string.permission_granted)
            } else {
                getString(R.string.permission_denied)
            }
        }

        viewModel.locationPermissionStatus.observe(viewLifecycleOwner) { granted ->
            binding.locationPermissionStatus.text = if (granted) {
                getString(R.string.permission_granted)
            } else {
                getString(R.string.permission_denied)
            }
        }

        viewModel.notificationPermissionStatus.observe(viewLifecycleOwner) { granted ->
            binding.notificationPermissionStatus.text = if (granted) {
                getString(R.string.permission_granted)
            } else {
                getString(R.string.permission_denied)
            }
        }

        // Observe action messages
        viewModel.actionMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_SHORT).show()
                viewModel.clearActionMessage()
            }
        }
    }

    private fun setupClickListeners() {
        // Notifications switch
        binding.notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateNotificationsEnabled(isChecked)
        }

        // Battery optimization switch
        binding.batteryOptimizationSwitch.setOnCheckedChangeListener { _, isChecked ->
            viewModel.updateBatteryOptimizationMode(isChecked)
        }

        // Quiet Hours start button
        binding.quietHoursStartButton.setOnClickListener {
            showTimePicker(true)
        }

        // Quiet Hours end button
        binding.quietHoursEndButton.setOnClickListener {
            showTimePicker(false)
        }

        // Permission buttons
        binding.motionPermissionButton.setOnClickListener {
            viewModel.openSystemSettings("motion")
        }

        binding.locationPermissionButton.setOnClickListener {
            viewModel.openSystemSettings("location")
        }

        binding.notificationPermissionButton.setOnClickListener {
            viewModel.openSystemSettings("notification")
        }

        // Data management buttons
        binding.clearHistoryButton.setOnClickListener {
            showClearHistoryConfirmation()
        }

        binding.resetStreaksButton.setOnClickListener {
            showResetStreaksConfirmation()
        }
    }

    private fun showTimePicker(isStartTime: Boolean) {
        val calendar = Calendar.getInstance()
        val currentPrefs = viewModel.preferences.value
        
        // Try to parse existing time
        val timeString = if (isStartTime) {
            currentPrefs?.quietHoursStart
        } else {
            currentPrefs?.quietHoursEnd
        }
        
        if (timeString != null) {
            try {
                val time = timeFormat.parse(timeString)
                if (time != null) {
                    calendar.time = time
                }
            } catch (e: Exception) {
                // Use current time if parsing fails
            }
        }

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                val selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
                val currentPrefs = viewModel.preferences.value
                if (isStartTime) {
                    viewModel.updateQuietHours(selectedTime, currentPrefs?.quietHoursEnd)
                } else {
                    viewModel.updateQuietHours(currentPrefs?.quietHoursStart, selectedTime)
                }
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        ).show()
    }

    private fun showClearHistoryConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_completion_history)
            .setMessage(R.string.clear_history_confirmation)
            .setPositiveButton(R.string.confirm) { _, _ ->
                viewModel.clearCompletionHistory()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showResetStreaksConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.reset_all_streaks)
            .setMessage(R.string.reset_streaks_confirmation)
            .setPositiveButton(R.string.confirm) { _, _ ->
                viewModel.resetAllStreaks()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh permission status when returning from settings
        viewModel.checkPermissionStatus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

