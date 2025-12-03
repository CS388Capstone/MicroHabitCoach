package com.microhabitcoach.ui.explore

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.microhabitcoach.R
import com.microhabitcoach.data.model.HabitCategory
import com.microhabitcoach.data.util.HabitTypeInferrer
import com.microhabitcoach.databinding.FragmentArticleDetailBinding
import kotlinx.coroutines.launch

class ArticleDetailFragment : Fragment() {

    private var _binding: FragmentArticleDetailBinding? = null
    private val binding get() = _binding!!

    private val args: ArticleDetailFragmentArgs by navArgs()
    
    private val viewModel: ArticleDetailViewModel by viewModels {
        ArticleDetailViewModel.Factory(
            requireActivity().application,
            args.suggestionId
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentArticleDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupButtons()
        observeViewModel()
    }

    private fun setupButtons() {
        binding.btnOpenInBrowser.setOnClickListener {
            openInBrowser()
        }

        binding.btnSaveArticle.setOnClickListener {
            viewModel.toggleSave()
        }

        binding.btnTurnIntoHabit.setOnClickListener {
            navigateToAddEditHabit()
        }
    }

    private fun observeViewModel() {
        viewModel.suggestion.observe(viewLifecycleOwner) { suggestion ->
            suggestion?.let {
                displaySuggestion(it)
            }
        }

        viewModel.isSaved.observe(viewLifecycleOwner) { isSaved ->
            updateSaveButton(isSaved)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                // Show error (could use Snackbar or Toast)
                android.util.Log.e("ArticleDetailFragment", "Error: $it")
            }
        }
    }

    private fun displaySuggestion(suggestion: com.microhabitcoach.data.database.entity.ApiSuggestion) {
        with(binding) {
            // Title
            tvArticleTitle.text = suggestion.title

            // Source, Date, Author
            val source = viewModel.getDisplaySource(suggestion)
            tvArticleSource.text = source

            val date = viewModel.formatPublishedDate(suggestion.publishedAt)
            if (date != null) {
                tvArticleDate.text = "• $date"
                tvArticleDate.isVisible = true
            } else {
                tvArticleDate.isVisible = false
            }

            if (!suggestion.author.isNullOrBlank()) {
                tvArticleAuthor.text = "• by ${suggestion.author}"
                tvArticleAuthor.isVisible = true
            } else {
                tvArticleAuthor.isVisible = false
            }

            // Category (handle null as "General")
            chipCategory.text = suggestion.category?.displayName() ?: "General"

            // Content
            val content = suggestion.content ?: suggestion.title
            tvArticleContent.text = content

            // Image
            if (!suggestion.imageUrl.isNullOrBlank()) {
                Glide.with(this@ArticleDetailFragment)
                    .load(suggestion.imageUrl)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.ic_menu_report_image)
                    .into(ivArticleImage)
                ivArticleImage.isVisible = true
            } else {
                ivArticleImage.isVisible = false
            }

            // Enable/disable buttons based on URL availability
            binding.btnOpenInBrowser.isEnabled = !suggestion.sourceUrl.isNullOrBlank()
        }
    }

    private fun updateSaveButton(isSaved: Boolean) {
        with(binding.btnSaveArticle) {
            if (isSaved) {
                text = getString(R.string.unsave_article)
                setIconResource(android.R.drawable.ic_menu_delete)
            } else {
                text = getString(R.string.save_article)
                setIconResource(android.R.drawable.ic_menu_save)
            }
        }
    }

    private fun openInBrowser() {
        val suggestion = viewModel.suggestion.value
        val url = suggestion?.sourceUrl

        if (url.isNullOrBlank()) {
            android.util.Log.w("ArticleDetailFragment", "No URL available to open")
            return
        }

        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("ArticleDetailFragment", "Error opening browser: ${e.message}", e)
        }
    }

    private fun navigateToAddEditHabit() {
        val suggestion = viewModel.suggestion.value ?: return

        // Infer habit type from suggestion content
        val inferredType = HabitTypeInferrer.inferType(suggestion)

        // Navigate to AddEditHabitFragment with suggestion data
        val category = suggestion.category ?: HabitCategory.GENERAL
        val action = ArticleDetailFragmentDirections.actionArticleDetailFragmentToAddEditHabitFragment(
            habitId = null,
            suggestionName = suggestion.title,
            suggestionCategory = category.name,
            suggestionType = inferredType.name.lowercase()
        )
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

