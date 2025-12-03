package com.microhabitcoach.ui.explore

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.microhabitcoach.data.database.entity.SavedArticle
import com.microhabitcoach.databinding.ItemSavedArticleBinding
import java.text.SimpleDateFormat
import java.util.Locale

class SavedArticleAdapter(
    private val onViewClick: (SavedArticle) -> Unit,
    private val onDeleteClick: (SavedArticle) -> Unit
) : ListAdapter<SavedArticle, SavedArticleAdapter.SavedArticleViewHolder>(SavedArticleDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedArticleViewHolder {
        val binding = ItemSavedArticleBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SavedArticleViewHolder(binding, onViewClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: SavedArticleViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SavedArticleViewHolder(
        private val binding: ItemSavedArticleBinding,
        private val onViewClick: (SavedArticle) -> Unit,
        private val onDeleteClick: (SavedArticle) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(article: SavedArticle) {
            with(binding) {
                // Title
                tvArticleTitle.text = article.title

                // Source and date
                val source = when {
                    !article.sourceName.isNullOrBlank() -> article.sourceName!!
                    article.source == "hacker_news" -> "Hacker News"
                    article.source == "news_api" -> "News API"
                    else -> article.source.replace("_", " ").replaceFirstChar { it.uppercase() }
                }

                val date = formatPublishedDate(article.publishedAt)
                val sourceText = if (date != null) {
                    "$source • $date"
                } else {
                    source
                }
                tvArticleSource.text = sourceText

                // Description
                tvArticleDescription.text = article.description ?: article.content ?: article.title

                // Category
                article.category?.let { category ->
                    chipCategory.text = category.name.lowercase().replaceFirstChar { it.uppercase() }
                    chipCategory.isVisible = true
                } ?: run {
                    chipCategory.isVisible = false
                }

                // View button
                btnViewArticle.setOnClickListener {
                    onViewClick(article)
                }

                // Delete button
                btnDelete.setOnClickListener {
                    onDeleteClick(article)
                }
            }
        }

        private fun formatPublishedDate(publishedAt: String?): String? {
            if (publishedAt == null) return null

            return try {
                // Try to parse ISO 8601 format from News API
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
                val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
                val date = inputFormat.parse(publishedAt)
                date?.let { outputFormat.format(it) }
            } catch (e: Exception) {
                // If parsing fails, try alternative format
                try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US)
                    val outputFormat = SimpleDateFormat("MMM d, yyyy", Locale.US)
                    val date = inputFormat.parse(publishedAt)
                    date?.let { outputFormat.format(it) }
                } catch (e2: Exception) {
                    null
                }
            }
        }
    }

    private class SavedArticleDiffCallback : DiffUtil.ItemCallback<SavedArticle>() {
        override fun areItemsTheSame(oldItem: SavedArticle, newItem: SavedArticle): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: SavedArticle, newItem: SavedArticle): Boolean {
            return oldItem == newItem
        }
    }
}

