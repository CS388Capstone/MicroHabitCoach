package com.microhabitcoach.ui.explore

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.microhabitcoach.R
import com.microhabitcoach.data.database.entity.ApiSuggestion
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.util.HabitTypeInferrer
import com.microhabitcoach.databinding.FragmentExploreBinding

class ExploreFragment : Fragment() {

    private var _binding: FragmentExploreBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ExploreViewModel by viewModels {
        ExploreViewModel.Factory(requireActivity().application)
    }

    private lateinit var suggestionAdapter: SuggestionAdapter
    private var visibleCount: Int = 10

    private var selectedCategory: HabitCategory? = null

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
        setupCategoryFilter()
        setupFitScoreInfo()
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
        val category = suggestion.category ?: HabitCategory.GENERAL
        
        // Navigate to AddEditHabitFragment with suggestion data
        val action = ExploreFragmentDirections.actionExploreFragmentToAddEditHabitFragment(
            habitId = null,
            suggestionName = suggestion.title,
            suggestionCategory = category.name,
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
            // Reset visible count when new suggestions arrive
            visibleCount = 10
            applyFilterAndSubmit(suggestions)
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

        viewModel.weatherState.observe(viewLifecycleOwner) { state ->
            renderWeatherState(state)
        }
    }

    private fun setupCategoryFilter() {
        binding.chipFilterAll.setOnClickListener {
            selectedCategory = null
            visibleCount = 10
            applyFilterAndSubmit(viewModel.suggestions.value ?: emptyList())
        }
        binding.chipFilterFitness.setOnClickListener {
            selectedCategory = HabitCategory.FITNESS
            visibleCount = 10
            applyFilterAndSubmit(viewModel.suggestions.value ?: emptyList())
        }
        binding.chipFilterHealthyEating.setOnClickListener {
            selectedCategory = HabitCategory.HEALTHY_EATING
            visibleCount = 10
            applyFilterAndSubmit(viewModel.suggestions.value ?: emptyList())
        }
        binding.chipFilterWellness.setOnClickListener {
            selectedCategory = HabitCategory.WELLNESS
            visibleCount = 10
            applyFilterAndSubmit(viewModel.suggestions.value ?: emptyList())
        }
        binding.chipFilterProductivity.setOnClickListener {
            selectedCategory = HabitCategory.PRODUCTIVITY
            visibleCount = 10
            applyFilterAndSubmit(viewModel.suggestions.value ?: emptyList())
        }
        binding.chipFilterLearning.setOnClickListener {
            selectedCategory = HabitCategory.LEARNING
            visibleCount = 10
            applyFilterAndSubmit(viewModel.suggestions.value ?: emptyList())
        }
    }

    private fun setupFitScoreInfo() {
        binding.btnFitScoreInfo.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.fit_score_info_title)
                .setMessage(R.string.fit_score_info_message)
                .setPositiveButton(android.R.string.ok, null)
                .show()
        }

        binding.btnLoadMore.setOnClickListener {
            val currentSuggestions = viewModel.suggestions.value ?: emptyList()
            val filtered = selectedCategory?.let { category ->
                currentSuggestions.filter { it.category == category }
            } ?: currentSuggestions

            if (visibleCount < filtered.size) {
                visibleCount += 10
                applyFilterAndSubmit(currentSuggestions)
            }
        }
    }

    private fun applyFilterAndSubmit(suggestions: List<ApiSuggestion>) {
        val filtered = selectedCategory?.let { category ->
            suggestions.filter { it.category == category }
        } ?: suggestions

        val toShow = if (filtered.size > visibleCount) {
            binding.btnLoadMore.isVisible = true
            filtered.take(visibleCount)
        } else {
            binding.btnLoadMore.isVisible = false
            filtered
        }

        suggestionAdapter.submitList(toShow)
    }

    private fun renderWeatherState(state: WeatherUiState?) {
        if (state == null) {
            binding.cardWeather.isVisible = false
            return
        }
        binding.cardWeather.isVisible = true
        binding.tvWeatherCondition.text = state.conditionText
        binding.tvWeatherTemperature.isVisible = !state.temperatureText.isNullOrEmpty()
        binding.tvWeatherTemperature.text = state.temperatureText
        binding.tvWeatherImpact.text = state.impactMessage

        val (backgroundColorRes, textColorRes, iconRes) = when (state.impactType) {
            WeatherImpactType.POSITIVE -> Triple(
                R.color.weather_positive_bg,
                R.color.weather_positive_text,
                android.R.drawable.ic_menu_compass
            )
            WeatherImpactType.NEGATIVE -> Triple(
                R.color.weather_negative_bg,
                R.color.weather_negative_text,
                android.R.drawable.ic_dialog_alert
            )
            WeatherImpactType.NEUTRAL -> Triple(
                R.color.weather_neutral_bg,
                R.color.weather_neutral_text,
                android.R.drawable.ic_menu_info_details
            )
        }

        val textColor = ContextCompat.getColor(requireContext(), textColorRes)
        binding.cardWeather.setCardBackgroundColor(ContextCompat.getColor(requireContext(), backgroundColorRes))
        binding.tvWeatherCondition.setTextColor(textColor)
        binding.tvWeatherTemperature.setTextColor(textColor)
        binding.tvWeatherImpact.setTextColor(textColor)
        binding.ivWeatherImpact.setImageResource(iconRes)
        binding.ivWeatherImpact.imageTintList = ColorStateList.valueOf(textColor)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

