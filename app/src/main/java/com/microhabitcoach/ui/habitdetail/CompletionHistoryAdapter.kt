package com.microhabitcoach.ui.habitdetail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microhabitcoach.databinding.ItemCompletionHistoryBinding

class CompletionHistoryAdapter : ListAdapter<CompletionHistoryItem, CompletionHistoryAdapter.CompletionHistoryViewHolder>(
    CompletionHistoryDiffCallback()
) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompletionHistoryViewHolder {
        val binding = ItemCompletionHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CompletionHistoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CompletionHistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class CompletionHistoryViewHolder(
        private val binding: ItemCompletionHistoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CompletionHistoryItem) {
            binding.tvDate.text = item.formattedDate
            binding.tvTime.text = item.formattedTime
            
            // Show auto-completed chip if applicable
            binding.chipAutoCompleted.visibility = 
                if (item.completion.autoCompleted) {
                    android.view.View.VISIBLE
                } else {
                    android.view.View.GONE
                }
        }
    }

    private class CompletionHistoryDiffCallback : DiffUtil.ItemCallback<CompletionHistoryItem>() {
        override fun areItemsTheSame(oldItem: CompletionHistoryItem, newItem: CompletionHistoryItem): Boolean {
            return oldItem.completion.id == newItem.completion.id
        }

        override fun areContentsTheSame(oldItem: CompletionHistoryItem, newItem: CompletionHistoryItem): Boolean {
            return oldItem == newItem
        }
    }
}

