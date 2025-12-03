package com.microhabitcoach.ui.habitdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microhabitcoach.R
import com.microhabitcoach.databinding.ItemCalendarDayBinding
import java.util.Calendar

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
            val context = binding.root.context

            if (dayData.isPlaceholder) {
                binding.tvDay.text = ""
                binding.root.setCardBackgroundColor(
                    ContextCompat.getColor(context, android.R.color.transparent)
                )
                binding.tvDay.setTextColor(
                    ContextCompat.getColor(context, android.R.color.darker_gray)
                )
                return
            }

            val calendar = Calendar.getInstance().apply {
                timeInMillis = dayData.date
            }
            
            // Display day of month
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            binding.tvDay.text = dayOfMonth.toString()
            
            val completedBg = ContextCompat.getColor(context, R.color.weather_positive_bg)
            val completedText = ContextCompat.getColor(context, android.R.color.white)
            val defaultBg = ContextCompat.getColor(context, android.R.color.transparent)
            val defaultText = ContextCompat.getColor(context, android.R.color.black)

            if (dayData.hasCompletion) {
                binding.root.setCardBackgroundColor(completedBg)
                binding.tvDay.setTextColor(completedText)
            } else {
                binding.root.setCardBackgroundColor(defaultBg)
                binding.tvDay.setTextColor(defaultText)
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

