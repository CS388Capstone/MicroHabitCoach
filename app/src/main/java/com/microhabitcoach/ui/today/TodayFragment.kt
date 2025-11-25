package com.microhabitcoach.ui.today

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.microhabitcoach.R
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.databinding.FragmentTodayBinding

class TodayFragment : Fragment() {

    private var _binding: FragmentTodayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TodayViewModel by viewModels {
        TodayViewModel.Factory(requireActivity().application)
    }

    private lateinit var adapter: HabitItemAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTodayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupFab()
        observeViewModel()
        viewModel.loadHabits()
    }

    private fun setupRecyclerView() {
        adapter = HabitItemAdapter(
            onCompleteClick = { habit ->
                viewModel.completeHabit(habit.id)
            },
            onEditClick = { habit ->
                navigateToEditHabit(habit.id)
            },
            onDeleteClick = { habit ->
                showDeleteConfirmation(habit)
            }
        )

        binding.recyclerViewHabits.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHabits.adapter = adapter
    }

    private fun setupFab() {
        binding.fabAddHabit.setOnClickListener {
            navigateToAddHabit()
        }
        binding.btnAddFirstHabit.setOnClickListener {
            navigateToAddHabit()
        }
    }

    private fun observeViewModel() {
        viewModel.habits.observe(viewLifecycleOwner) { habits ->
            adapter.submitList(habits)
            updateSummary(habits)
            binding.emptyState.isVisible = habits.isEmpty()
            binding.recyclerViewHabits.isVisible = habits.isNotEmpty()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun updateSummary(habits: List<Habit>) {
        val completedCount = 0 // TODO: Calculate based on today's completions
        val totalCount = habits.size
        binding.tvSummary.text = getString(R.string.habits_summary_format, completedCount, totalCount)
    }

    private fun showDeleteConfirmation(habit: Habit) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_habit)
            .setMessage(R.string.delete_habit_confirmation)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteHabit(habit.id)
                Snackbar.make(binding.root, R.string.habit_deleted, Snackbar.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun navigateToAddHabit() {
        findNavController().navigate(
            TodayFragmentDirections.actionTodayFragmentToAddEditHabitFragment()
        )
    }

    private fun navigateToEditHabit(habitId: String) {
        findNavController().navigate(
            TodayFragmentDirections.actionTodayFragmentToAddEditHabitFragment(habitId = habitId)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

