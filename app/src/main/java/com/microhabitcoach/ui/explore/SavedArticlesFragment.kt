package com.microhabitcoach.ui.explore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.microhabitcoach.databinding.FragmentSavedArticlesBinding

class SavedArticlesFragment : Fragment() {

    private var _binding: FragmentSavedArticlesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SavedArticlesViewModel by viewModels {
        SavedArticlesViewModel.Factory(requireActivity().application)
    }

    private lateinit var savedArticleAdapter: SavedArticleAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSavedArticlesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        // Setup toolbar with Navigation Component for automatic back button handling
        binding.toolbar.setupWithNavController(findNavController())
    }

    private fun setupRecyclerView() {
        savedArticleAdapter = SavedArticleAdapter(
            onViewClick = { article ->
                navigateToArticleDetail(article)
            },
            onDeleteClick = { article ->
                viewModel.deleteSavedArticle(article)
            }
        )
        binding.recyclerViewSavedArticles.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewSavedArticles.adapter = savedArticleAdapter
    }

    private fun observeViewModel() {
        viewModel.savedArticles.observe(viewLifecycleOwner) { articles ->
            savedArticleAdapter.submitList(articles)
            
            // Show/hide empty state
            binding.tvEmptyState.isVisible = articles.isEmpty()
            binding.recyclerViewSavedArticles.isVisible = articles.isNotEmpty()
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                android.util.Log.e("SavedArticlesFragment", "Error: $it")
                viewModel.clearError()
            }
        }
    }

    private fun navigateToArticleDetail(article: com.microhabitcoach.data.database.entity.SavedArticle) {
        // Navigate to ArticleDetailFragment using the article ID
        // Note: We need to check if the article exists in ApiSuggestion cache
        // If not, we might need to create a temporary ApiSuggestion or handle differently
        val action = SavedArticlesFragmentDirections.actionSavedArticlesFragmentToArticleDetailFragment(
            suggestionId = article.id
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

