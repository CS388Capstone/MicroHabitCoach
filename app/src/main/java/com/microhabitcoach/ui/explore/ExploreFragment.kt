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
        observeViewModel()
        viewModel.loadSuggestions()
    }

    private fun setupRecyclerView() {
        suggestionAdapter = SuggestionAdapter { suggestion ->
            navigateToAddEditHabit(suggestion)
        }
        binding.recyclerViewSuggestions.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSuggestions.adapter = suggestionAdapter
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
            binding.recyclerViewSuggestions.isVisible = suggestions.isNotEmpty()
            binding.tvError.isVisible = suggestions.isEmpty() && !isLoading
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.isVisible = isLoading
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                binding.tvError.text = it
                binding.tvError.isVisible = true
                viewModel.clearError()
            } ?: run {
                binding.tvError.isVisible = false
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

