package com.microhabitcoach.ui.today

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microhabitcoach.data.database.entity.Habit
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.model.HabitType
import com.microhabitcoach.databinding.ItemHabitBinding

class HabitItemAdapter(
    private val onCompleteClick: (Habit) -> Unit,
    private val onEditClick: (Habit) -> Unit,
    private val onDeleteClick: (Habit) -> Unit
) : ListAdapter<Habit, HabitItemAdapter.HabitViewHolder>(HabitDiffCallback()) {

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

        fun bind(habit: Habit) {
            binding.tvHabitName.text = habit.name
            binding.tvHabitType.text = "${habit.type.displayName()} • ${habit.category.displayName()}"
            binding.tvStreak.text = "🔥 ${habit.streakCount} day streak"
            binding.checkboxComplete.isChecked = false // TODO: Check completion status for today

            binding.checkboxComplete.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
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
    }

    private class HabitDiffCallback : DiffUtil.ItemCallback<Habit>() {
        override fun areItemsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Habit, newItem: Habit): Boolean {
            return oldItem == newItem
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

