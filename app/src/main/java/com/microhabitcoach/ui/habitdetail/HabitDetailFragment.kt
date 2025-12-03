package com.microhabitcoach.ui.habitdetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.microhabitcoach.R
import com.microhabitcoach.databinding.FragmentHabitDetailBinding

class HabitDetailFragment : Fragment() {

    private var _binding: FragmentHabitDetailBinding? = null
    private val binding get() = _binding!!

    private val args: HabitDetailFragmentArgs by navArgs()

    private val viewModel: HabitDetailViewModel by viewModels {
        HabitDetailViewModel.Factory(requireActivity().application, args.habitId)
    }

    private lateinit var calendarAdapter: CalendarDayAdapter
    private lateinit var historyAdapter: CompletionHistoryAdapter

    private var isCalendarView = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHabitDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerViews()
        setupViewToggle()
        setupEditButton()
        observeViewModel()
        viewModel.loadHabit()
    }

    private fun setupRecyclerViews() {
        // Calendar view - GridLayoutManager with 7 columns (one per day of week)
        calendarAdapter = CalendarDayAdapter()
        binding.recyclerViewCalendar.layoutManager = GridLayoutManager(requireContext(), 7)
        binding.recyclerViewCalendar.adapter = calendarAdapter

        // History view - LinearLayoutManager
        historyAdapter = CompletionHistoryAdapter()
        binding.recyclerViewHistory.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewHistory.adapter = historyAdapter
    }

    private fun setupViewToggle() {
        binding.btnCalendarView.setOnClickListener {
            if (!isCalendarView) {
                isCalendarView = true
                binding.cardCalendarView.isVisible = true
                binding.cardListView.isVisible = false
                // Update button states - toggle is handled by visibility
            }
        }

        binding.btnListView.setOnClickListener {
            if (isCalendarView) {
                isCalendarView = false
                binding.cardCalendarView.isVisible = false
                binding.cardListView.isVisible = true
                // Update button states - toggle is handled by visibility
            }
        }
    }

    private fun setupEditButton() {
        binding.btnEdit.setOnClickListener {
            findNavController().navigate(
                HabitDetailFragmentDirections.actionHabitDetailFragmentToAddEditHabitFragment(
                    habitId = args.habitId
                )
            )
        }
    }

    private fun observeViewModel() {
        // Observe habit
        viewModel.habit.observe(viewLifecycleOwner) { habit ->
            habit?.let {
                binding.tvHabitName.text = it.name
            }
        }

        // Observe detail data
        viewModel.detailData.observe(viewLifecycleOwner) { detailData ->
            detailData?.let {
                // Update streak info
                binding.tvCurrentStreak.text = it.streakInfo.currentStreak.toString()
                binding.tvBestStreak.text = it.streakInfo.bestStreak.toString()

                // Update completion stats
                binding.tvSevenDayPercentage.text = String.format("%.1f%%", it.completionStats.sevenDayPercentage)
                binding.tvThirtyDayPercentage.text = String.format("%.1f%%", it.completionStats.thirtyDayPercentage)
                binding.tvTotalCompletions.text = "Total: ${it.completionStats.totalCompletions} completions"

                // Update calendar view
                calendarAdapter.submitList(it.calendarData)

                // Update history view
                historyAdapter.submitList(it.historyItems)

                // Update insights
                it.bestDayInfo?.let { bestDay ->
                    binding.tvBestDay.text = "Most consistent day: ${bestDay.dayName}"
                } ?: run {
                    binding.tvBestDay.text = "No completion data yet"
                }

                val trendText = if (it.trendAnalysis.isImproving) {
                    "Trend: Improving (+${String.format("%.1f", it.trendAnalysis.trendPercentage)}%)"
                } else {
                    "Trend: Declining (${String.format("%.1f", it.trendAnalysis.trendPercentage)}%)"
                }
                binding.tvTrend.text = trendText
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
        }

        // Observe error state
        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                binding.tvError.text = it
                binding.tvError.isVisible = true
                viewModel.clearError()
            } ?: run {
                binding.tvError.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

