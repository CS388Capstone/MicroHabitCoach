package com.microhabitcoach.ui.today

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microhabitcoach.R
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.databinding.ItemHabitBinding
import com.microhabitcoach.ui.today.HabitWithCompletion
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar

class HabitItemAdapter(
    private val onCompleteClick: (Habit) -> Unit,
    private val onEditClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit
) : ListAdapter<HabitWithCompletion, HabitItemAdapter.HabitViewHolder>(HabitDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val binding = ItemHabitBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return HabitViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(
        private val binding: ItemHabitBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(habitWithCompletion: HabitWithCompletion) {
            val habit = habitWithCompletion.habit
            binding.tvHabitName.text = habit.name
            binding.tvHabitType.text = "${habit.type.displayName()} • ${habit.category.displayName()}"
            binding.tvStreak.text = "🔥 ${habit.streakCount} day streak"
            binding.checkboxComplete.isChecked = habitWithCompletion.isCompletedToday

            // Set habit icon
            binding.ivHabitIcon.setImageResource(getIconForCategory(habit.category))

            // Set progress (based on streak, max 30 days for 100%)
            val maxStreak = 30
            val progress = ((habit.streakCount.coerceAtMost(maxStreak).toFloat() / maxStreak) * 100).toInt()
            binding.progressIndicator.progress = progress

            // Set next reminder time for time-based habits
            if (habit.type == HabitType.TIME && habit.reminderTimes != null && habit.reminderTimes.isNotEmpty()) {
                val nextReminder = calculateNextReminder(habit.reminderTimes, habit.reminderDays)
                if (nextReminder != null) {
                    val formatter = DateTimeFormatter.ofPattern("h:mm a")
                    binding.tvNextReminder.text = binding.root.context.getString(R.string.next_reminder, nextReminder.format(formatter))
                    binding.tvNextReminder.isVisible = true
                } else {
                    binding.tvNextReminder.isVisible = false
                }
            } else {
                binding.tvNextReminder.isVisible = false
            }

            binding.checkboxComplete.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked && !habitWithCompletion.isCompletedToday) {
                    onCompleteClick(habit)
                }
            }

            binding.btnEdit.setOnClickListener {
                onEditClick(habit)
            }

            binding.btnDelete.setOnClickListener {
                onDeleteClick(habit)
            }
        }

        private fun getIconForCategory(category: HabitCategory): Int {
            return when (category) {
                HabitCategory.FITNESS -> android.R.drawable.ic_menu_compass
                HabitCategory.WELLNESS -> android.R.drawable.ic_menu_agenda
                HabitCategory.PRODUCTIVITY -> android.R.drawable.ic_menu_edit
                HabitCategory.LEARNING -> android.R.drawable.ic_menu_view
                HabitCategory.GENERAL -> android.R.drawable.ic_menu_agenda
            }
        }

        private fun calculateNextReminder(reminderTimes: List<LocalTime>, reminderDays: List<Int>?): LocalTime? {
            val now = LocalTime.now()
            val currentDayOfWeek = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
            // Convert Calendar day (1=Sunday) to our format (1=Monday)
            val today = if (currentDayOfWeek == Calendar.SUNDAY) 7 else currentDayOfWeek - 1

            // Filter times that are today and after now, or future days
            val sortedTimes = reminderTimes.sorted()
            val validDays = reminderDays ?: listOf(1, 2, 3, 4, 5, 6, 7)

            // Check if today is a valid day
            if (validDays.contains(today)) {
                // Find next time today
                val nextToday = sortedTimes.firstOrNull { it.isAfter(now) }
                if (nextToday != null) return nextToday
            }

            // Find next day with reminders
            val sortedDays = validDays.sorted()
            val nextDayIndex = sortedDays.indexOfFirst { it > today }
            val nextDay = if (nextDayIndex >= 0) {
                sortedDays[nextDayIndex]
            } else {
                sortedDays.firstOrNull()
            }

            return nextDay?.let { sortedTimes.firstOrNull() }
        }
    }

    private class HabitDiffCallback : DiffUtil.ItemCallback<HabitWithCompletion>() {
        override fun areItemsTheSame(oldItem: HabitWithCompletion, newItem: HabitWithCompletion): Boolean {
            return oldItem.habit.id == newItem.habit.id
        }

        override fun areContentsTheSame(oldItem: HabitWithCompletion, newItem: HabitWithCompletion): Boolean {
            return oldItem.habit == newItem.habit && oldItem.isCompletedToday == newItem.isCompletedToday
        }
    }
}

private fun HabitType.displayName(): String = when (this) {
    HabitType.TIME -> "Time-based"
    HabitType.MOTION -> "Motion-based"
    HabitType.LOCATION -> "Location-based"
}

private fun HabitCategory.displayName(): String =
    name.lowercase().replaceFirstChar { it.uppercase() }

