package com.microhabitcoach.ui.stats

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.microhabitcoach.R
import com.microhabitcoach.databinding.FragmentStatsBinding
import com.microhabitcoach.data.model.HabitCategory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: StatsViewModel by viewModels {
        StatsViewModel.Factory(requireActivity().application)
    }

    private val dateFormat = SimpleDateFormat("MM/dd", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStatsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupCharts()
        observeViewModel()
        viewModel.loadStats()
    }

    private fun setupCharts() {
        // Setup pie chart
        binding.categoryPieChart.apply {
            description.isEnabled = false
            setUsePercentValues(true)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            setCenterText("Habits by Category")
            setCenterTextSize(14f)
            legend.isEnabled = true
            legend.verticalAlignment = com.github.mikephil.charting.components.Legend.LegendVerticalAlignment.BOTTOM
            legend.horizontalAlignment = com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment.CENTER
            legend.orientation = com.github.mikephil.charting.components.Legend.LegendOrientation.HORIZONTAL
            legend.setDrawInside(false)
        }
    }

    private fun observeViewModel() {
        // Observe aggregate statistics
        viewModel.totalHabits.observe(viewLifecycleOwner) { count ->
            binding.totalHabitsText.text = count.toString()
        }

        viewModel.longestStreak.observe(viewLifecycleOwner) { streak ->
            binding.longestStreakText.text = streak.toString()
        }

        viewModel.totalCompletions.observe(viewLifecycleOwner) { count ->
            binding.totalCompletionsText.text = count.toString()
        }

        viewModel.completionRate7Days.observe(viewLifecycleOwner) { rate ->
            val formatted = String.format(Locale.getDefault(), "%.0f%%", rate)
            binding.completionRate7DaysText.text = formatted
            // Color code based on performance
            binding.completionRate7DaysText.setTextColor(getRateColor(rate))
        }

        viewModel.completionRate30Days.observe(viewLifecycleOwner) { rate ->
            val formatted = String.format(Locale.getDefault(), "%.0f%%", rate)
            binding.completionRate30DaysText.text = formatted
            // Color code based on performance
            binding.completionRate30DaysText.setTextColor(getRateColor(rate))
        }

        viewModel.totalDaysActive.observe(viewLifecycleOwner) { days ->
            binding.totalDaysActiveText.text = "$days days"
        }

        viewModel.perfectDays.observe(viewLifecycleOwner) { days ->
            binding.perfectDaysText.text = "$days days"
        }

        viewModel.motivationalMessage.observe(viewLifecycleOwner) { message ->
            binding.motivationalMessageText.text = message
        }

        // Observe new stats
        viewModel.consistencyScore.observe(viewLifecycleOwner) { score ->
            if (score != null) {
                binding.consistencyScoreText.text = "${score.score}"
                binding.consistencyGradeText.text = score.grade
                binding.consistencyDescriptionText.text = score.description
            }
        }

        viewModel.topPerformingHabits.observe(viewLifecycleOwner) { habits ->
            updateTopHabits(habits)
        }

        viewModel.weeklyComparison.observe(viewLifecycleOwner) { comparison ->
            if (comparison != null) {
                binding.thisWeekCompletionsText.text = "${comparison.thisWeekCompletions}"
                binding.thisWeekRateText.text = String.format(Locale.getDefault(), "%.0f%%", comparison.thisWeekRate)
                binding.lastWeekCompletionsText.text = "${comparison.lastWeekCompletions}"
                binding.lastWeekRateText.text = String.format(Locale.getDefault(), "%.0f%%", comparison.lastWeekRate)
                
                val changeText = if (comparison.changePercent >= 0) {
                    "+${String.format(Locale.getDefault(), "%.0f", comparison.changePercent)}%"
                } else {
                    "${String.format(Locale.getDefault(), "%.0f", comparison.changePercent)}%"
                }
                binding.weeklyChangeText.text = changeText
                binding.weeklyChangeText.setTextColor(
                    if (comparison.changePercent >= 0) {
                        Color.parseColor("#4CAF50")
                    } else {
                        Color.parseColor("#FF5722")
                    }
                )
            }
        }

        viewModel.categoryPerformance.observe(viewLifecycleOwner) { categories ->
            updateCategoryPerformance(categories)
        }

        // Observe insights
        viewModel.mostConsistentHabit.observe(viewLifecycleOwner) { habit ->
            binding.mostConsistentHabitText.text = if (habit != null) {
                "${habit.habitName} - ${String.format(Locale.getDefault(), "%.0f%%", habit.completionRate)} completion rate"
            } else {
                "No data yet"
            }
        }

        viewModel.bestDayOfWeek.observe(viewLifecycleOwner) { day ->
            binding.bestDayText.text = if (day != null) {
                "${day.dayName} (${day.completionCount} completions)"
            } else {
                "No data yet"
            }
        }

        viewModel.currentStreakLeader.observe(viewLifecycleOwner) { habit ->
            binding.currentStreakLeaderText.text = if (habit != null) {
                "${habit.name} (${habit.streakCount} days)"
            } else {
                "No data yet"
            }
        }

        // Observe chart data
        viewModel.categoryBreakdownData.observe(viewLifecycleOwner) { data ->
            updatePieChart(data)
        }

        viewModel.weeklyHeatmapData.observe(viewLifecycleOwner) { data ->
            updateHeatmap(data)
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // Could show/hide progress indicator here
        }
    }

    private fun updateTopHabits(habits: List<HabitPerformance>) {
        binding.topHabitsContainer.removeAllViews()
        
        if (habits.isEmpty()) {
            val textView = TextView(requireContext()).apply {
                text = "No habits yet"
                setTextAppearance(android.R.style.TextAppearance_Material_Body1)
            }
            binding.topHabitsContainer.addView(textView)
            return
        }
        
        habits.forEachIndexed { index, habit ->
            val habitView = LayoutInflater.from(requireContext())
                .inflate(android.R.layout.simple_list_item_2, binding.topHabitsContainer, false)
            
            val titleView = habitView.findViewById<TextView>(android.R.id.text1)
            val subtitleView = habitView.findViewById<TextView>(android.R.id.text2)
            
            titleView.text = "${index + 1}. ${habit.habitName}"
            subtitleView.text = "${String.format(Locale.getDefault(), "%.0f%%", habit.completionRate)} completion rate • ${habit.streakCount} day streak • ${habit.totalCompletions} completions"
            
            binding.topHabitsContainer.addView(habitView)
            
            if (index < habits.size - 1) {
                val divider = View(requireContext()).apply {
                    layoutParams = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                        1
                    ).apply {
                        setMargins(0, 8, 0, 8)
                    }
                    setBackgroundColor(Color.parseColor("#E0E0E0"))
                }
                binding.topHabitsContainer.addView(divider)
            }
        }
    }
    
    private fun updateCategoryPerformance(categories: List<CategoryPerformance>) {
        binding.categoryPerformanceContainer.removeAllViews()
        
        if (categories.isEmpty()) {
            val textView = TextView(requireContext()).apply {
                text = "No category data yet"
                setTextAppearance(android.R.style.TextAppearance_Material_Body1)
            }
            binding.categoryPerformanceContainer.addView(textView)
            return
        }
        
        categories.forEach { category ->
            val categoryView = LayoutInflater.from(requireContext())
                .inflate(android.R.layout.simple_list_item_2, binding.categoryPerformanceContainer, false)
            
            val titleView = categoryView.findViewById<TextView>(android.R.id.text1)
            val subtitleView = categoryView.findViewById<TextView>(android.R.id.text2)
            
            titleView.text = category.category.name
            subtitleView.text = "${String.format(Locale.getDefault(), "%.0f%%", category.averageCompletionRate)} avg • ${category.habitCount} habit${if (category.habitCount != 1) "s" else ""} • ${category.totalCompletions} completions"
            
            binding.categoryPerformanceContainer.addView(categoryView)
        }
    }

    private fun updatePieChart(data: Map<HabitCategory, Int>) {
        if (data.isEmpty()) return

        val entries = data.map { (category, count) ->
            PieEntry(count.toFloat(), category.name)
        }

        val dataSet = PieDataSet(entries, "").apply {
            colors = getCategoryColors(data.keys.toList())
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        val pieData = PieData(dataSet)
        binding.categoryPieChart.data = pieData
        binding.categoryPieChart.invalidate()
    }

    private fun getCategoryColors(categories: List<HabitCategory>): List<Int> {
        val colorMap = mapOf(
            HabitCategory.FITNESS to Color.parseColor("#FF5722"),
            HabitCategory.WELLNESS to Color.parseColor("#4CAF50"),
            HabitCategory.PRODUCTIVITY to Color.parseColor("#2196F3"),
            HabitCategory.LEARNING to Color.parseColor("#FF9800"),
            HabitCategory.GENERAL to Color.parseColor("#9E9E9E")
        )
        return categories.map { colorMap.getOrDefault(it, Color.GRAY) }
    }

    private fun getRateColor(rate: Double): Int {
        return when {
            rate >= 80 -> Color.parseColor("#4CAF50") // Green - Excellent
            rate >= 60 -> Color.parseColor("#8BC34A") // Light Green - Good
            rate >= 40 -> Color.parseColor("#FF9800") // Orange - Fair
            rate >= 20 -> Color.parseColor("#FF5722") // Red-Orange - Needs improvement
            else -> Color.parseColor("#9E9E9E") // Gray - Low
        }
    }

    private fun updateHeatmap(data: Array<Array<Int>>) {
        // Simple text representation for now
        // Can be enhanced with a custom view later
        val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        val weekLabels = listOf("Week 1", "Week 2", "Week 3", "Week 4")
        
        val maxValue = data.flatten().maxOrNull() ?: 0
        if (maxValue == 0) {
            binding.heatmapInfoText.text = "No activity data available"
            return
        }

        val heatmapText = buildString {
            append("Activity Heatmap (Last 4 Weeks)\n\n")
            append("Day/Week")
            weekLabels.forEach { append("\t$it") }
            append("\n")
            
            for (day in 0 until 7) {
                append(dayNames[day])
                for (week in 0 until 4) {
                    val value = data[day][week]
                    append("\t$value")
                }
                append("\n")
            }
        }
        
        binding.heatmapInfoText.text = heatmapText
    }

    override fun onResume() {
        super.onResume()
        // Refresh stats when returning to screen
        viewModel.loadStats()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

