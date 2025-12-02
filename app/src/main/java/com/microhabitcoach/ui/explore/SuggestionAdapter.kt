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
    private val onTurnIntoHabitClick: (ApiSuggestion) -> Unit
) : ListAdapter<ApiSuggestion, SuggestionAdapter.SuggestionViewHolder>(SuggestionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SuggestionViewHolder {
        val binding = ItemSuggestionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SuggestionViewHolder(binding, onTurnIntoHabitClick)
    }

    override fun onBindViewHolder(holder: SuggestionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SuggestionViewHolder(
        private val binding: ItemSuggestionBinding,
        private val onTurnIntoHabitClick: (ApiSuggestion) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(suggestion: ApiSuggestion) {
            with(binding) {
                tvSuggestionTitle.text = suggestion.title
                tvSuggestionSource.text = suggestion.source.replace("_", " ").replaceFirstChar { it.uppercase() }
                tvSuggestionContent.text = suggestion.content ?: ""
                tvFitScore.text = suggestion.fitScore.toString()
                
                // Set category chip
                chipCategory.text = suggestion.category.name.lowercase().replaceFirstChar { it.uppercase() }
                
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

