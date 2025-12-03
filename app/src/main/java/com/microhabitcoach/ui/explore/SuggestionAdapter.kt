package com.microhabitcoach.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.util.HabitTypeInferrer
import com.microhabitcoach.databinding.ItemSuggestionBinding

class SuggestionAdapter(
    private val onViewDetailsClick: (ApiSuggestion) -> Unit,
    private val onTurnIntoHabitClick: (ApiSuggestion) -> Unit
) : ListAdapter<ApiSuggestion, SuggestionAdapter.SuggestionViewHolder>(SuggestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SuggestionViewHolder(binding, onViewDetailsClick, onTurnIntoHabitClick)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SuggestionViewHolder(
        private val binding: ItemSuggestionBinding,
        private val onViewDetailsClick: (ApiSuggestion) -> Unit,
        private val onTurnIntoHabitClick: (ApiSuggestion) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(suggestion: ApiSuggestion) {
            with(binding) {
                tvSuggestionTitle.text = suggestion.title
                tvSuggestionSource.text = suggestion.source.replace("_", " ").replaceFirstChar { it.uppercase() }
                tvSuggestionContent.text = suggestion.content ?: ""
                
                // Display FitScore with color coding
                val fitScore = suggestion.fitScore
                tvFitScore.text = fitScore.toString()
                
                // Color code FitScore: Green (70+), Yellow (40-69), Red (<40)
                val context = binding.root.context
                val color = when {
                    fitScore >= 70 -> android.graphics.Color.parseColor("#4CAF50") // Green
                    fitScore >= 40 -> android.graphics.Color.parseColor("#FF9800") // Orange
                    else -> android.graphics.Color.parseColor("#F44336") // Red
                }
                tvFitScore.setTextColor(color)
                
                // Set category chip
                chipCategory.text = suggestion.category.name.lowercase().replaceFirstChar { it.uppercase() }
                
                // Set click listener for card (view details)
                root.setOnClickListener {
                    onViewDetailsClick(suggestion)
                }
                
                // Set click listener for "Turn into Habit" button
                btnTurnIntoHabit.setOnClickListener {
                    onTurnIntoHabitClick(suggestion)
                }
            }
        }
    }

    private class SuggestionDiffCallback : DiffUtil.ItemCallback<ApiSuggestion>() {
        override fun areItemsTheSame(oldItem: ApiSuggestion, newItem: ApiSuggestion): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ApiSuggestion, newItem: ApiSuggestion): Boolean {
            return oldItem == newItem
        }
    }
}

