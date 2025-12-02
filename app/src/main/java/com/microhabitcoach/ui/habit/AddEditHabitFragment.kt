package com.microhabitcoach.ui.habit

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    private var selectedMotionType: String? = null
    private var selectedDuration: Int? = null
    private var selectedRadius: Float? = null
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

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
        
        setupToolbar()
        setupCategoryButtons()
        setupTypeButtons()
        setupTimePresets()
        setupDayChips()
        setupMotionPresets()
        setupLocationPresets()
        setupActions()
        observeViewModel()
        prefillFromArgs()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().popBackStack()
        }
    }

    private fun setupCategoryButtons() {
        binding.btnCategoryFitness.setOnClickListener {
            selectCategory(HabitCategory.FITNESS)
        }
        binding.btnCategoryWellness.setOnClickListener {
            selectCategory(HabitCategory.WELLNESS)
        }
        binding.btnCategoryProductivity.setOnClickListener {
            selectCategory(HabitCategory.PRODUCTIVITY)
        }
        binding.btnCategoryLearning.setOnClickListener {
            selectCategory(HabitCategory.LEARNING)
        }
    }
    
    private fun selectCategory(category: HabitCategory) {
        // Update button states
        binding.btnCategoryFitness.isSelected = category == HabitCategory.FITNESS
        binding.btnCategoryWellness.isSelected = category == HabitCategory.WELLNESS
        binding.btnCategoryProductivity.isSelected = category == HabitCategory.PRODUCTIVITY
        binding.btnCategoryLearning.isSelected = category == HabitCategory.LEARNING
        
        // Update button styles to show selection
        updateCategoryButtonStyle(binding.btnCategoryFitness, category == HabitCategory.FITNESS)
        updateCategoryButtonStyle(binding.btnCategoryWellness, category == HabitCategory.WELLNESS)
        updateCategoryButtonStyle(binding.btnCategoryProductivity, category == HabitCategory.PRODUCTIVITY)
        updateCategoryButtonStyle(binding.btnCategoryLearning, category == HabitCategory.LEARNING)
    }
    
    private fun updateCategoryButtonStyle(button: com.google.android.material.button.MaterialButton, isSelected: Boolean) {
        button.elevation = if (isSelected) 8f else 2f
        button.strokeWidth = if (isSelected) 3 else 1
    }

    private fun setupTypeButtons() {
        binding.btnTypeTime.setOnClickListener {
            selectType(HabitType.TIME)
        }
        binding.btnTypeMotion.setOnClickListener {
            selectType(HabitType.MOTION)
        }
        binding.btnTypeLocation.setOnClickListener {
            selectType(HabitType.LOCATION)
        }
        // Default to TIME
        selectType(HabitType.TIME)
    }

    private fun selectType(type: HabitType) {
        // Update button states - toggle between outlined and filled styles
        binding.btnTypeTime.isSelected = type == HabitType.TIME
        binding.btnTypeMotion.isSelected = type == HabitType.MOTION
        binding.btnTypeLocation.isSelected = type == HabitType.LOCATION
        
        // Update button styles to show selection
        updateTypeButtonStyle(binding.btnTypeTime, type == HabitType.TIME)
        updateTypeButtonStyle(binding.btnTypeMotion, type == HabitType.MOTION)
        updateTypeButtonStyle(binding.btnTypeLocation, type == HabitType.LOCATION)
        
        // Toggle sections
        binding.sectionTime.isVisible = type == HabitType.TIME
        binding.sectionMotion.isVisible = type == HabitType.MOTION
        binding.sectionLocation.isVisible = type == HabitType.LOCATION
    }
    
    private fun updateTypeButtonStyle(button: com.google.android.material.button.MaterialButton, isSelected: Boolean) {
        // Change elevation to show selection
        button.elevation = if (isSelected) 8f else 2f
    }

    private fun setupTimePresets() {
        // Custom time button
        binding.btnCustomTime.setOnClickListener {
            val now = LocalTime.now()
            TimePickerDialog(
                requireContext(),
                { _, hour, minute ->
                    val time = LocalTime.of(hour, minute)
                    if (!reminderTimes.contains(time)) {
                        reminderTimes.add(time)
                        reminderTimes.sort()
                        renderReminderTimeChips()
                    } else {
                        Snackbar.make(binding.root, R.string.reminder_time_exists, Snackbar.LENGTH_SHORT).show()
                    }
                },
                now.hour,
                now.minute,
                false
            ).show()
        }
    }
    
    private fun renderReminderTimeChips() {
        // Create chips dynamically for selected times
        // This will be displayed in a ChipGroup if needed
        // For now, we'll just track the times in the list
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
            }
        }
        
        // "Every Day" chip
        binding.chipEveryDay.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedDays.addAll(1..7)
                chips.forEach { (chip, _) -> chip.isChecked = true }
            } else {
                selectedDays.clear()
                chips.forEach { (chip, _) -> chip.isChecked = false }
            }
        }
    }

    private fun setupMotionPresets() {
        // Motion type chips - single selection handled by ChipGroup
        binding.chipGroupMotionType.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedChip = group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])
                selectedMotionType = when (checkedChip.id) {
                    binding.chipWalk.id -> "Walk"
                    binding.chipRun.id -> "Run"
                    binding.chipStationary.id -> "Stationary"
                    else -> null
                }
            } else {
                selectedMotionType = null
            }
        }
        
        // Duration chips - single selection handled by ChipGroup
        binding.chipGroupDuration.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val checkedChip = group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])
                selectedDuration = when (checkedChip.id) {
                    binding.chipDuration10.id -> 10
                    binding.chipDuration20.id -> 20
                    binding.chipDuration30.id -> 30
                    binding.chipDuration60.id -> 60
                    else -> null
                }
            } else {
                selectedDuration = null
            }
        }
        
        // Default to 30 min
        binding.chipDuration30.isChecked = true
    }

    private fun setupLocationPresets() {
        // Make location chips single selection
        binding.chipGroupLocationPresets.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty()) {
                selectedLocation = null
                binding.tvLocationAddress.text = getString(R.string.no_location_selected)
                binding.tilRadius.visibility = View.GONE
            } else {
                val checkedChip = group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])
                when (checkedChip.id) {
                    binding.chipLocationHome.id -> {
                        selectedLocation = LocationData(
                            latitude = 0.0, // Placeholder - would use actual home location
                            longitude = 0.0,
                            address = "Home"
                        )
                        binding.tvLocationAddress.text = "Home"
                        binding.tilRadius.visibility = View.GONE
                    }
                    binding.chipLocationGym.id -> {
                        selectedLocation = LocationData(
                            latitude = 0.0, // Placeholder - would use actual gym location
                            longitude = 0.0,
                            address = "Gym"
                        )
                        binding.tvLocationAddress.text = "Gym"
                        binding.tilRadius.visibility = View.GONE
                    }
                    binding.chipLocationWork.id -> {
                        selectedLocation = LocationData(
                            latitude = 0.0, // Placeholder - would use actual work location
                            longitude = 0.0,
                            address = "Work"
                        )
                        binding.tvLocationAddress.text = "Work"
                        binding.tilRadius.visibility = View.GONE
                    }
                }
            }
        }
        
        binding.btnPickLocation.setOnClickListener {
            // Clear preset selections
            binding.chipGroupLocationPresets.clearCheck()
            // Placeholder until map picker is wired up
            selectedLocation = LocationData(
                latitude = 37.7749,
                longitude = -122.4194,
                address = "Custom Location"
            )
            binding.tvLocationAddress.text = selectedLocation?.address ?: getString(R.string.no_location_selected)
            binding.tilRadius.visibility = View.VISIBLE
            binding.etRadius.setText("100")
            Snackbar.make(binding.root, R.string.location_mock_message, Snackbar.LENGTH_SHORT).show()
        }
        
        // Default radius (can be made configurable later)
        selectedRadius = 100f
        
        // Listen to radius changes
        binding.etRadius.doAfterTextChanged {
            selectedRadius = it?.toString()?.toFloatOrNull() ?: 100f
        }
    }

    private fun setupActions() = with(binding) {
        btnSave.setOnClickListener { attemptSave() }
        
        etHabitName.doAfterTextChanged { }
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
                }
                SaveState.Success -> {
                    binding.btnSave.isEnabled = true
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
                    showError(state.message)
                }
                else -> {
                    binding.btnSave.isEnabled = true
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
            binding.toolbar.title = getString(R.string.edit_habit)
            viewModel.loadHabit(habitId)
        } else {
            binding.toolbar.title = getString(R.string.add_habit)
            // Pre-fill from suggestion if available
            args.suggestionName?.takeIf { it.isNotBlank() }?.let {
                binding.etHabitName.setText(it)
            }
            args.suggestionCategory?.let { setCategorySelection(it) }
            args.suggestionType?.let { 
                setTypeSelection(it)
                prefillDefaultParameters(it)
            }
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
                binding.chipWalk.isChecked = true
                binding.chipDuration30.isChecked = true
            }
            HabitType.LOCATION -> {
                selectedRadius = 100f
            }
            HabitType.TIME -> {
                // Default to every day
                binding.chipEveryDay.isChecked = true
            }
        }
    }

    private fun setCategorySelection(raw: String) {
        val category = HabitCategory.values().firstOrNull { 
            it.name.equals(raw, ignoreCase = true) 
        } ?: return
        
        selectCategory(category)
    }

    private fun setTypeSelection(raw: String) {
        val type = when (raw.lowercase()) {
            "time", "time-based", "time_based" -> HabitType.TIME
            "motion", "motion-based", "motion_based" -> HabitType.MOTION
            "location", "location-based", "location_based" -> HabitType.LOCATION
            else -> HabitType.TIME
        }
        selectType(type)
    }

    private fun currentType(): HabitType {
        // Check which section is visible
        return when {
            binding.sectionTime.isVisible -> HabitType.TIME
            binding.sectionMotion.isVisible -> HabitType.MOTION
            binding.sectionLocation.isVisible -> HabitType.LOCATION
            else -> HabitType.TIME
        }
    }

    private fun currentCategory(): HabitCategory = when {
        binding.btnCategoryFitness.isSelected -> HabitCategory.FITNESS
        binding.btnCategoryWellness.isSelected -> HabitCategory.WELLNESS
        binding.btnCategoryProductivity.isSelected -> HabitCategory.PRODUCTIVITY
        binding.btnCategoryLearning.isSelected -> HabitCategory.LEARNING
        else -> HabitCategory.FITNESS // Default to Fitness
    }

    private fun attemptSave() {
        val type = currentType()
        val name = binding.etHabitName.text?.toString()?.trim().orEmpty()
        val category = currentCategory()

        binding.tilHabitName.error = if (name.isBlank()) getString(R.string.error_required) else null

        if (type == HabitType.MOTION) {
            if (selectedMotionType.isNullOrBlank()) {
                showError("Please select a motion type")
                return
            }
            if (selectedDuration == null || selectedDuration!! <= 0) {
                showError("Please select a duration")
                return
            }
        }

        if (type == HabitType.LOCATION) {
            if (selectedLocation == null) {
                showError("Please select a location")
                return
            }
            // Get radius from input if visible, otherwise use default
            val radiusInput = binding.etRadius.text?.toString()?.toFloatOrNull()
            selectedRadius = if (radiusInput != null && radiusInput > 0f) {
                radiusInput
            } else {
                100f // Default
            }
        }

        if (type == HabitType.TIME) {
            if (reminderTimes.isEmpty()) {
                showError("Please select at least one reminder time")
                return
            }
            if (selectedDays.isEmpty()) {
                showError("Please select at least one day")
                return
            }
        }

        val validation = viewModel.validateForm(
            name = name,
            type = type,
            reminderTimes = if (type == HabitType.TIME) reminderTimes.toList() else emptyList(),
            reminderDays = if (type == HabitType.TIME) selectedDays.sorted() else emptyList(),
            motionType = if (type == HabitType.MOTION) selectedMotionType else null,
            duration = if (type == HabitType.MOTION) selectedDuration else null,
            hasLocation = if (type == HabitType.LOCATION) selectedLocation != null else true,
            radius = if (type == HabitType.LOCATION) selectedRadius else null
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
            reminderDays = if (type == HabitType.TIME) selectedDays.sorted() else emptyList(),
            motionType = if (type == HabitType.MOTION) selectedMotionType else null,
            targetDuration = if (type == HabitType.MOTION) selectedDuration else null,
            location = if (type == HabitType.LOCATION) selectedLocation else null,
            geofenceRadius = if (type == HabitType.LOCATION) selectedRadius else null
        )
    }

    private fun populateForm(habit: Habit) = with(binding) {
        toolbar.title = getString(R.string.edit_habit)
        etHabitName.setText(habit.name)
        
        // Set category
        selectCategory(habit.category)
        
        // Set type
        selectType(habit.type)

        // Populate time-based fields
        if (habit.type == HabitType.TIME) {
            reminderTimes.clear()
            habit.reminderTimes?.let { reminderTimes.addAll(it) }
            
            selectedDays.clear()
            habit.reminderDays?.let { selectedDays.addAll(it) }
            setDaySelections(selectedDays)
            
            if (selectedDays.size == 7) {
                binding.chipEveryDay.isChecked = true
            }
        }

        // Populate motion-based fields
        if (habit.type == HabitType.MOTION) {
            when (habit.motionType?.lowercase()) {
                "walk" -> binding.chipWalk.isChecked = true
                "run" -> binding.chipRun.isChecked = true
                "stationary" -> binding.chipStationary.isChecked = true
            }
            
            habit.targetDuration?.let { duration ->
                selectedDuration = duration
                when (duration) {
                    10 -> binding.chipDuration10.isChecked = true
                    20 -> binding.chipDuration20.isChecked = true
                    30 -> binding.chipDuration30.isChecked = true
                    60 -> binding.chipDuration60.isChecked = true
                }
            }
        }

        // Populate location-based fields
        if (habit.type == HabitType.LOCATION) {
            selectedLocation = habit.location
            binding.tvLocationAddress.text = habit.location?.address ?: getString(R.string.no_location_selected)
            selectedRadius = habit.geofenceRadius ?: 100f
            
            // Check preset chips if location matches
            when (habit.location?.address?.lowercase()) {
                "home" -> binding.chipLocationHome.isChecked = true
                "gym" -> binding.chipLocationGym.isChecked = true
                "work" -> binding.chipLocationWork.isChecked = true
                else -> {
                    // Custom location - show radius input
                    binding.tilRadius.visibility = View.VISIBLE
                    binding.etRadius.setText(selectedRadius?.toInt().toString())
                }
            }
        }
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

    private fun updatePreview() {
        // Preview removed for minimalistic design
        // Can be added back if needed
    }

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
