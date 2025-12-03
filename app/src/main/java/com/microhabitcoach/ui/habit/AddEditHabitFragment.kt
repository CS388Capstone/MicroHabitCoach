package com.microhabitcoach.ui.habit

import android.app.TimePickerDialog
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.microhabitcoach.R
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.data.model.LocationData
import com.microhabitcoach.databinding.FragmentAddEditHabitBinding
import com.microhabitcoach.ui.habit.AddEditHabitViewModel.FormState
import com.microhabitcoach.ui.habit.AddEditHabitViewModel.SaveState
import com.microhabitcoach.util.PermissionHelper
import java.io.IOException
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class AddEditHabitFragment : Fragment() {

    private var _binding: FragmentAddEditHabitBinding? = null
    private val binding get() = _binding!!

    private val args: AddEditHabitFragmentArgs by navArgs()
    private val viewModel: AddEditHabitViewModel by viewModels {
        AddEditHabitViewModel.Factory(requireActivity().application)
    }

    private val reminderTimes = mutableListOf<LocalTime>()
    private val selectedDays = mutableSetOf<Int>()
    private var selectedLocation: LocationData? = null
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")
    
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            getCurrentLocation()
        } else {
            Snackbar.make(
                binding.root,
                R.string.location_permission_denied,
                Snackbar.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditHabitBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        setupCategorySpinner()
        setupMotionTypeDropdown()
        setupTypeSelector()
        setupDayChips()
        setupReminderTimes()
        setupLocationPicker()
        setupPreviewWatchers()
        setupActions()
        observeViewModel()
        prefillFromArgs()
        updatePreview()
    }

    private fun setupCategorySpinner() {
        val categories = HabitCategory.values().map { it.displayName() }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                updatePreview()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
    }

    private fun setupMotionTypeDropdown() {
        val motionItems = listOf("Walk", "Run", "Stationary")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, motionItems)
        binding.actvMotionType.setAdapter(adapter)
        binding.actvMotionType.doAfterTextChanged { updatePreview() }
    }

    private fun setupTypeSelector() = with(binding) {
        chipGroupType.setOnCheckedStateChangeListener { _, _ ->
            toggleSections(currentType())
            updatePreview()
        }
    }

    private fun setupDayChips() {
        val chips = listOf(
            binding.chipMonday to 1,
            binding.chipTuesday to 2,
            binding.chipWednesday to 3,
            binding.chipThursday to 4,
            binding.chipFriday to 5,
            binding.chipSaturday to 6,
            binding.chipSunday to 7
        )
        chips.forEach { (chip, day) ->
            chip.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) selectedDays.add(day) else selectedDays.remove(day)
                updatePreview()
            }
        }
    }

    private fun setupReminderTimes() {
        binding.btnAddReminderTime.setOnClickListener {
            val now = LocalTime.now()
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    val time = LocalTime.of(hour, minute)
                    if (!reminderTimes.contains(time)) {
                        reminderTimes.add(time)
                        renderReminderTimeChips()
                        updatePreview()
                    } else {
                        Snackbar.make(binding.root, R.string.reminder_time_exists, Snackbar.LENGTH_SHORT).show()
                    }
                },
                now.hour,
                now.minute,
                false
            ).show()
        }
        renderReminderTimeChips()
    }

    private fun renderReminderTimeChips() {
        with(binding) {
            chipGroupReminderTimes.removeAllViews()
            reminderTimes.sort()
            reminderTimes.forEach { time ->
                val chip = Chip(requireContext()).apply {
                    text = time.format(timeFormatter)
                    isCloseIconVisible = true
                    setOnCloseIconClickListener {
                        reminderTimes.remove(time)
                        renderReminderTimeChips()
                        updatePreview()
                    }
                }
                chipGroupReminderTimes.addView(chip)
            }
            chipGroupReminderTimes.isVisible = reminderTimes.isNotEmpty()
            tvNoReminderTimes.isVisible = reminderTimes.isEmpty()
        }
    }

    private fun setupLocationPicker() = with(binding) {
        btnPickLocation.setOnClickListener {
            if (PermissionHelper.hasLocationPermission(requireContext())) {
                getCurrentLocation()
            } else {
                locationPermissionLauncher.launch(android.Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }
    
    private fun getCurrentLocation() {
        if (!PermissionHelper.hasLocationPermission(requireContext())) {
            return
        }
        
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        val locationData = LocationData(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            address = getAddressFromLocation(location.latitude, location.longitude)
                        )
                        selectedLocation = locationData
                        binding.tvLocationAddress.text = locationData.address
                            ?: getString(R.string.location_selected)
                        updatePreview()
                    } else {
                        Snackbar.make(
                            binding.root,
                            R.string.location_unavailable,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
                .addOnFailureListener { e ->
                    Snackbar.make(
                        binding.root,
                        getString(R.string.location_error, e.message),
                        Snackbar.LENGTH_SHORT
                    ).show()
                }
        } catch (e: SecurityException) {
            Snackbar.make(
                binding.root,
                R.string.location_permission_denied,
                Snackbar.LENGTH_SHORT
            ).show()
        }
    }
    
    private fun getAddressFromLocation(latitude: Double, longitude: Double): String? {
        return try {
            val geocoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Geocoder(requireContext(), Locale.getDefault())
            } else {
                @Suppress("DEPRECATION")
                Geocoder(requireContext(), Locale.getDefault())
            }
            
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (addresses != null && addresses.isNotEmpty()) {
                val address = addresses[0]
                // Format: Street, City, State
                val addressParts = mutableListOf<String>()
                address.getAddressLine(0)?.let { addressParts.add(it) }
                address.locality?.let { addressParts.add(it) }
                address.adminArea?.let { addressParts.add(it) }
                addressParts.joinToString(", ")
            } else {
                null
            }
        } catch (e: IOException) {
            null
        } catch (e: IllegalArgumentException) {
            null
        }
    }

    private fun setupPreviewWatchers() = with(binding) {
        etHabitName.doAfterTextChanged { updatePreview() }
        etDuration.doAfterTextChanged { updatePreview() }
        etRadius.doAfterTextChanged { updatePreview() }
    }

    private fun setupActions() = with(binding) {
        btnCancel.setOnClickListener { findNavController().popBackStack() }
        btnSave.setOnClickListener { attemptSave() }
        btnDelete.setOnClickListener { attemptDelete() }
    }

    private fun observeViewModel() {
        viewModel.habit.observe(viewLifecycleOwner) { habit ->
            habit?.let { populateForm(it) }
        }
        viewModel.formState.observe(viewLifecycleOwner) { state ->
            when (state) {
                FormState.Loading -> binding.btnSave.isEnabled = false
                is FormState.Error -> {
                    binding.btnSave.isEnabled = true
                    showError(state.message)
                }
                else -> binding.btnSave.isEnabled = true
            }
        }
        viewModel.saveState.observe(viewLifecycleOwner) { state ->
            when (state) {
                SaveState.Saving -> {
                    binding.btnSave.isEnabled = false
                    binding.btnDelete.isEnabled = false
                }
                SaveState.Success -> {
                    binding.btnSave.isEnabled = true
                    binding.btnDelete.isEnabled = true
                    val message = if (viewModel.habit.value != null) {
                        R.string.habit_deleted
                    } else {
                        R.string.habit_saved
                    }
                    Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
                is SaveState.Error -> {
                    binding.btnSave.isEnabled = true
                    binding.btnDelete.isEnabled = true
                    showError(state.message)
                }
                else -> {
                    binding.btnSave.isEnabled = true
                    binding.btnDelete.isEnabled = true
                }
            }
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { showError(it) }
        }
    }

    private fun prefillFromArgs() {
        val habitId = args.habitId
        if (!habitId.isNullOrBlank()) {
            binding.tvTitle.text = getString(R.string.edit_habit)
            binding.btnDelete.isVisible = true
            viewModel.loadHabit(habitId)
        } else {
            binding.btnDelete.isVisible = false
            // Pre-fill from suggestion if available
            args.suggestionName?.takeIf { it.isNotBlank() }?.let {
                binding.etHabitName.setText(it)
            }
            args.suggestionCategory?.let { setCategorySelection(it) }
            args.suggestionType?.let { 
                setTypeSelection(it)
                // Pre-fill default parameters based on type
                prefillDefaultParameters(it)
            }
            updatePreview()
        }
    }

    private fun prefillDefaultParameters(typeString: String) {
        val type = when (typeString.lowercase()) {
            "time", "time-based", "time_based" -> HabitType.TIME
            "motion", "motion-based", "motion_based" -> HabitType.MOTION
            "location", "location-based", "location_based" -> HabitType.LOCATION
            else -> HabitType.TIME
        }
        
        when (type) {
            HabitType.MOTION -> {
                // Pre-fill motion type and duration for motion-based habits
                binding.actvMotionType.setText("Walk", false)
                binding.etDuration.setText("30")
            }
            HabitType.LOCATION -> {
                // Pre-fill radius for location-based habits
                binding.etRadius.setText("100")
            }
            HabitType.TIME -> {
                // Time-based habits don't need default pre-fills
                // User can add reminder times and days
            }
        }
    }

    private fun setCategorySelection(raw: String) {
        val index = HabitCategory.values().indexOfFirst { it.name.equals(raw, ignoreCase = true) }
        if (index >= 0) {
            binding.spinnerCategory.setSelection(index)
        }
    }

    private fun setTypeSelection(raw: String) {
        val type = when (raw.lowercase()) {
            "time", "time-based", "time_based" -> HabitType.TIME
            "motion", "motion-based", "motion_based" -> HabitType.MOTION
            "location", "location-based", "location_based" -> HabitType.LOCATION
            else -> HabitType.TIME
        }
        when (type) {
            HabitType.TIME -> binding.chipGroupType.check(binding.chipTime.id)
            HabitType.MOTION -> binding.chipGroupType.check(binding.chipMotion.id)
            HabitType.LOCATION -> binding.chipGroupType.check(binding.chipLocation.id)
        }
        toggleSections(type)
    }

    private fun toggleSections(type: HabitType) = with(binding) {
        sectionTime.isVisible = type == HabitType.TIME
        sectionMotion.isVisible = type == HabitType.MOTION
        sectionLocation.isVisible = type == HabitType.LOCATION
    }

    private fun attemptSave() {
        val type = currentType()
        val name = binding.etHabitName.text?.toString()?.trim().orEmpty()
        val category = HabitCategory.values()[binding.spinnerCategory.selectedItemPosition]
        val motionType = binding.actvMotionType.text?.toString()?.trim()
        val duration = binding.etDuration.text?.toString()?.toIntOrNull()
        val radius = binding.etRadius.text?.toString()?.toFloatOrNull()

        binding.tilHabitName.error = if (name.isBlank()) getString(R.string.error_required) else null

        if (type == HabitType.MOTION) {
            binding.tilMotionType.error = if (motionType.isNullOrBlank()) getString(R.string.error_required) else null
            binding.tilDuration.error = if (duration == null || duration <= 0) getString(R.string.error_positive_number) else null
        } else {
            binding.tilMotionType.error = null
            binding.tilDuration.error = null
        }

        if (type == HabitType.LOCATION) {
            binding.tilRadius.error = if (radius == null || radius <= 0f) getString(R.string.error_positive_number) else null
        } else {
            binding.tilRadius.error = null
        }

        val validation = viewModel.validateForm(
            name = name,
            type = type,
            reminderTimes = if (type == HabitType.TIME) reminderTimes.toList() else emptyList(),
            reminderDays = if (type == HabitType.TIME) selectedDays.sorted() else emptyList(),
            motionType = if (type == HabitType.MOTION) motionType else null,
            duration = if (type == HabitType.MOTION) duration else null,
            hasLocation = if (type == HabitType.LOCATION) selectedLocation != null else true,
            radius = if (type == HabitType.LOCATION) radius else null
        )

        if (!validation.isValid) {
            showError(validation.errors.firstOrNull() ?: getString(R.string.error_required))
            return
        }

        viewModel.saveHabit(
            existingHabit = viewModel.habit.value,
            name = name,
            category = category,
            type = type,
            reminderTimes = if (type == HabitType.TIME) reminderTimes.toList() else null,
            reminderDays = if (type == HabitType.TIME) selectedDays.sorted() else null,
            motionType = if (type == HabitType.MOTION) motionType else null,
            targetDuration = if (type == HabitType.MOTION) duration else null,
            location = if (type == HabitType.LOCATION) selectedLocation else null,
            geofenceRadius = if (type == HabitType.LOCATION) radius else null
        )
    }

    private fun populateForm(habit: Habit) = with(binding) {
        tvTitle.text = getString(R.string.edit_habit)
        etHabitName.setText(habit.name)
        spinnerCategory.setSelection(habit.category.ordinal)

        when (habit.type) {
            HabitType.TIME -> chipGroupType.check(chipTime.id)
            HabitType.MOTION -> chipGroupType.check(chipMotion.id)
            HabitType.LOCATION -> chipGroupType.check(chipLocation.id)
        }
        toggleSections(habit.type)

        reminderTimes.clear()
        habit.reminderTimes?.let { reminderTimes.addAll(it) }
        renderReminderTimeChips()

        selectedDays.clear()
        habit.reminderDays?.let { selectedDays.addAll(it) }
        setDaySelections(selectedDays)

        actvMotionType.setText(habit.motionType ?: "", false)
        etDuration.setText(habit.targetDuration?.toString() ?: "")

        selectedLocation = habit.location
        tvLocationAddress.text = habit.location?.address ?: getString(R.string.no_location_selected)
        etRadius.setText(habit.geofenceRadius?.toString() ?: "")

        updatePreview()
    }

    private fun setDaySelections(days: Set<Int>) {
        val map = mapOf(
            1 to binding.chipMonday,
            2 to binding.chipTuesday,
            3 to binding.chipWednesday,
            4 to binding.chipThursday,
            5 to binding.chipFriday,
            6 to binding.chipSaturday,
            7 to binding.chipSunday
        )
        map.forEach { (key, chip) ->
            chip.isChecked = days.contains(key)
        }
    }

    private fun currentType(): HabitType = when (binding.chipGroupType.checkedChipId) {
        binding.chipMotion.id -> HabitType.MOTION
        binding.chipLocation.id -> HabitType.LOCATION
        else -> HabitType.TIME
    }

    private fun updatePreview() = with(binding) {
        val name = etHabitName.text?.toString().takeIf { !it.isNullOrBlank() } ?: getString(R.string.habit_name_placeholder)
        tvPreviewName.text = name

        val type = currentType()
        val typeLabel = when (type) {
            HabitType.TIME -> getString(R.string.time_based)
            HabitType.MOTION -> getString(R.string.motion_based)
            HabitType.LOCATION -> getString(R.string.location_based)
        }
        val category = HabitCategory.values()[spinnerCategory.selectedItemPosition].displayName()
        tvPreviewType.text = getString(R.string.preview_type_format, typeLabel, category)

        tvPreviewDetails.text = when (type) {
            HabitType.TIME -> {
                val timesText = if (reminderTimes.isEmpty()) getString(R.string.preview_no_times)
                else reminderTimes.sorted().joinToString { it.format(timeFormatter) }
                val daysText = if (selectedDays.isEmpty()) getString(R.string.preview_no_days)
                else selectedDays.sorted().joinToString { dayNameShort(it) }
                getString(R.string.preview_time_details, timesText, daysText)
            }

            HabitType.MOTION -> {
                val motion = binding.actvMotionType.text?.toString().takeIf { !it.isNullOrBlank() }
                    ?: getString(R.string.preview_no_motion)
                val duration = binding.etDuration.text?.toString()?.toIntOrNull()
                getString(R.string.preview_motion_details, motion, duration ?: 0)
            }

            HabitType.LOCATION -> {
                val address = selectedLocation?.address ?: getString(R.string.preview_no_location)
                val radius = binding.etRadius.text?.toString()?.toFloatOrNull() ?: 0f
                getString(R.string.preview_location_details, address, radius)
            }
        }
    }

    private fun dayNameShort(day: Int): String = when (day) {
        1 -> getString(R.string.monday_short)
        2 -> getString(R.string.tuesday_short)
        3 -> getString(R.string.wednesday_short)
        4 -> getString(R.string.thursday_short)
        5 -> getString(R.string.friday_short)
        6 -> getString(R.string.saturday_short)
        else -> getString(R.string.sunday_short)
    }

    private fun HabitCategory.displayName(): String =
        name.lowercase().replaceFirstChar { it.uppercase() }

    private fun attemptDelete() {
        val habitId = args.habitId
        if (habitId.isNullOrBlank()) return

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_habit)
            .setMessage(R.string.delete_habit_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteHabit(habitId)
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
