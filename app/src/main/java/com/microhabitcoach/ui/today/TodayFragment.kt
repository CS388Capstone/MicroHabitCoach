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
import com.microhabitcoach.ui.today.HabitWithCompletion

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
        setupPullToRefresh()
        observeViewModel()
        viewModel.loadHabits()
    }

    private fun setupRecyclerView() {
        adapter = HabitItemAdapter(
            onCompleteClick = { habit ->
                viewModel.completeHabit(habit.id)
                showCompletionFeedback(habit.name)
            },
            onEditClick = { habit ->
                navigateToEditHabit(habit.id)
            },
            onDeleteClick = { habit ->
                showDeleteConfirmation(habit)
            },
            onHabitClick = { habit ->
                navigateToHabitDetail(habit.id)
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

    private fun setupPullToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Manually refresh completion status without restarting the Flow
            viewModel.refreshHabitsCompletionStatus()
            binding.swipeRefreshLayout.isRefreshing = false
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
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.clearError()
            }
        }
    }

    private fun updateSummary(habits: List<HabitWithCompletion>) {
        val completedCount = habits.count { it.isCompletedToday }
        val totalCount = habits.size
        binding.tvSummary.text = getString(R.string.habits_summary_format, completedCount, totalCount)
    }

    private fun showCompletionFeedback(habitName: String) {
        // Show snackbar
        val snackbar = Snackbar.make(
            binding.root,
            getString(R.string.habit_completed_message, habitName),
            Snackbar.LENGTH_SHORT
        )
        snackbar.show()
        
        // Add confetti-like visual feedback
        showConfettiAnimation()
    }
    
    private fun showConfettiAnimation() {
        // Create floating emoji particles for confetti effect
        val rootView = binding.root
        val width = rootView.width
        val height = rootView.height
        
        if (width > 0 && height > 0) {
            for (i in 0..10) {
                val emoji = android.widget.TextView(requireContext()).apply {
                    text = "🎉"
                    textSize = 24f
                    x = (Math.random() * width).toFloat()
                    y = height.toFloat()
                    alpha = 1f
                    layoutParams = ViewGroup.MarginLayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                
                // Add to CoordinatorLayout if available, otherwise to root
                val parent = if (rootView is androidx.coordinatorlayout.widget.CoordinatorLayout) {
                    rootView
                } else {
                    rootView as? ViewGroup ?: return
                }
                
                parent.addView(emoji)
                
                emoji.animate()
                    .translationY(-height.toFloat())
                    .translationX((Math.random() * 200 - 100).toFloat())
                    .alpha(0f)
                    .setDuration(1000)
                    .setStartDelay((i * 50).toLong())
                    .withEndAction {
                        parent.removeView(emoji)
                    }
                    .start()
            }
        }
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

    private fun navigateToHabitDetail(habitId: String) {
        findNavController().navigate(
            TodayFragmentDirections.actionTodayFragmentToHabitDetailFragment(habitId = habitId)
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

