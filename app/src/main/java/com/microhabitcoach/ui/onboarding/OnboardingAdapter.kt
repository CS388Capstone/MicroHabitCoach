package com.microhabitcoach.ui.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microhabitcoach.R
import com.microhabitcoach.databinding.ItemOnboardingStepBinding

class OnboardingAdapter(
    private val fragment: androidx.fragment.app.Fragment,
    private val onActionClick: (OnboardingStep) -> Unit
) : ListAdapter<OnboardingStep, OnboardingAdapter.OnboardingViewHolder>(OnboardingDiffCallback()) {

    init {
        submitList(OnboardingStep.values().toList())
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val binding = ItemOnboardingStepBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OnboardingViewHolder(binding, fragment, onActionClick)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class OnboardingViewHolder(
        private val binding: ItemOnboardingStepBinding,
        private val fragment: androidx.fragment.app.Fragment,
        private val onActionClick: (OnboardingStep) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(step: OnboardingStep) {
            when (step) {
                OnboardingStep.WELCOME -> {
                    binding.tvTitle.text = fragment.getString(R.string.onboarding_welcome_title)
                    binding.tvDescription.text = fragment.getString(R.string.onboarding_welcome_description)
                    binding.ivIcon.setImageResource(android.R.drawable.ic_dialog_info)
                    binding.btnAction.text = fragment.getString(R.string.get_started)
                    binding.btnAction.setOnClickListener { onActionClick(step) }
                }
            }
        }
    }

    private class OnboardingDiffCallback : DiffUtil.ItemCallback<OnboardingStep>() {
        override fun areItemsTheSame(oldItem: OnboardingStep, newItem: OnboardingStep): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: OnboardingStep, newItem: OnboardingStep): Boolean {
            return oldItem == newItem
        }
    }
}

