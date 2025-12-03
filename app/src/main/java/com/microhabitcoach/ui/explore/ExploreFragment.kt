package com.microhabitcoach.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.microhabitcoach.R
import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.util.HabitTypeInferrer
import com.microhabitcoach.databinding.FragmentExploreBinding

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExploreViewModel by viewModels {
        ExploreViewModel.Factory(requireActivity().application)
    }

    private lateinit var suggestionAdapter: SuggestionAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentExploreBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupPullToRefresh()
        setupToolbar()
        observeViewModel()
        viewModel.loadSuggestions()
    }

    private fun setupToolbar() {
        // Setup FAB to navigate to Saved Articles
        binding.fabSavedArticles.setOnClickListener {
            navigateToSavedArticles()
        }
    }

    private fun navigateToSavedArticles() {
        val action = ExploreFragmentDirections.actionExploreFragmentToSavedArticlesFragment()
        findNavController().navigate(action)
    }

    private fun setupRecyclerView() {
        suggestionAdapter = SuggestionAdapter(
            onViewDetailsClick = { suggestion ->
                navigateToArticleDetail(suggestion)
            },
            onTurnIntoHabitClick = { suggestion ->
                navigateToAddEditHabit(suggestion)
            }
        )
        binding.recyclerViewSuggestions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSuggestions.adapter = suggestionAdapter
    }

    private fun navigateToArticleDetail(suggestion: ApiSuggestion) {
        val action = ExploreFragmentDirections.actionExploreFragmentToArticleDetailFragment(
            suggestionId = suggestion.id
        )
        findNavController().navigate(action)
    }

    private fun navigateToAddEditHabit(suggestion: ApiSuggestion) {
        // Infer habit type from suggestion content
        val inferredType = HabitTypeInferrer.inferType(suggestion)
        
        // Navigate to AddEditHabitFragment with suggestion data
        val action = ExploreFragmentDirections.actionExploreFragmentToAddEditHabitFragment(
            habitId = null,
            suggestionName = suggestion.title,
            suggestionCategory = suggestion.category.name,
            suggestionType = inferredType.name.lowercase()
        )
        findNavController().navigate(action)
    }

    private fun setupPullToRefresh() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            // Force refresh from API on pull-to-refresh
            viewModel.refreshSuggestions()
        }
    }

    private fun observeViewModel() {
        // Observe suggestions list
        viewModel.suggestions.observe(viewLifecycleOwner) { suggestions ->
            suggestionAdapter.submitList(suggestions)
            val isLoading = viewModel.isLoading.value ?: false
            val hasError = viewModel.error.value != null
            
            binding.recyclerViewSuggestions.isVisible = suggestions.isNotEmpty()
            
            // Show error message if no suggestions and not loading
            if (suggestions.isEmpty() && !isLoading) {
                if (!hasError) {
                    // No error but no suggestions - show helpful message
                    binding.tvError.text = "No relevant suggestions found. Try refreshing or check back later."
                }
                binding.tvError.isVisible = true
            } else if (suggestions.isNotEmpty()) {
                // Hide error if we have suggestions
                binding.tvError.isVisible = false
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            // Only update swipe refresh if it's actually refreshing (not just loading from cache)
            if (isLoading) {
                binding.swipeRefreshLayout.isRefreshing = true
            } else {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            
            // Hide error while loading
            if (isLoading) {
                binding.tvError.isVisible = false
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                binding.tvError.text = it
                binding.tvError.isVisible = true
                viewModel.clearError()
            } ?: run {
                // Only hide error if we have suggestions
                val suggestions = viewModel.suggestions.value ?: emptyList()
                if (suggestions.isNotEmpty()) {
                    binding.tvError.isVisible = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

