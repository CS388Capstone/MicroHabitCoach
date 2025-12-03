package com.microhabitcoach.ui.habitdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microhabitcoach.databinding.ItemCalendarDayBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CalendarDayAdapter : ListAdapter<CalendarDayData, CalendarDayAdapter.CalendarDayViewHolder>(
    CalendarDayDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CalendarDayViewHolder {
        val binding = ItemCalendarDayBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CalendarDayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CalendarDayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CalendarDayViewHolder(
        private val binding: ItemCalendarDayBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(dayData: CalendarDayData) {
            val calendar = Calendar.getInstance().apply {
                timeInMillis = dayData.date
            }
            
            // Display day of month
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            binding.tvDay.text = dayOfMonth.toString()
            
            // Highlight days with completions
            if (dayData.hasCompletion) {
                binding.root.setCardBackgroundColor(
                    binding.root.context.getColor(android.R.color.holo_green_light)
                )
                binding.tvDay.setTextColor(
                    binding.root.context.getColor(android.R.color.white)
                )
            } else {
                binding.root.setCardBackgroundColor(
                    binding.root.context.getColor(android.R.color.transparent)
                )
                binding.tvDay.setTextColor(
                    binding.root.context.getColor(android.R.color.black)
                )
            }
        }
    }

    private class CalendarDayDiffCallback : DiffUtil.ItemCallback<CalendarDayData>() {
        override fun areItemsTheSame(oldItem: CalendarDayData, newItem: CalendarDayData): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: CalendarDayData, newItem: CalendarDayData): Boolean {
            return oldItem == newItem
        }
    }
}

