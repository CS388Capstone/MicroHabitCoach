package com.microhabitcoach.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NewsApiResponseTest {

    @Test
    fun newsApiResponse_creation_setsAllFields() {
        val article = NewsArticle(
            title = "Test Title",
            description = "Test Description",
            content = "Test Content",
            url = "https://example.com",
            urlToImage = "https://example.com/image.jpg",
            publishedAt = "2024-01-01T00:00:00Z",
            source = NewsSource(id = "test-id", name = "Test News")
        )

        val response = NewsApiResponse(
            status = "ok",
            totalResults = 1,
            articles = listOf(article)
        )

        assertEquals("ok", response.status)
        assertEquals(1, response.totalResults)
        assertEquals(1, response.articles.size)
        assertEquals(article, response.articles[0])
    }

    @Test
    fun newsArticle_withAllFields_setsCorrectly() {
        val article = NewsArticle(
            title = "Test Title",
            description = "Test Description",
            content = "Test Content",
            url = "https://example.com",
            urlToImage = "https://example.com/image.jpg",
            publishedAt = "2024-01-01T00:00:00Z",
            source = NewsSource(id = "test-id", name = "Test News")
        )

        assertEquals("Test Title", article.title)
        assertEquals("Test Description", article.description)
        assertEquals("Test Content", article.content)
        assertEquals("https://example.com", article.url)
        assertEquals("https://example.com/image.jpg", article.urlToImage)
        assertEquals("2024-01-01T00:00:00Z", article.publishedAt)
        assertEquals("test-id", article.source?.id)
        assertEquals("Test News", article.source?.name)
    }

    @Test
    fun newsArticle_withNullFields_handlesCorrectly() {
        val article = NewsArticle(
            title = null,
            description = null,
            content = null,
            url = null,
            urlToImage = null,
            publishedAt = null,
            source = null
        )

        assertNull(article.title)
        assertNull(article.description)
        assertNull(article.content)
        assertNull(article.url)
        assertNull(article.urlToImage)
        assertNull(article.publishedAt)
        assertNull(article.source)
    }

    @Test
    fun newsSource_creation_setsFields() {
        val source = NewsSource(
            id = "test-id",
            name = "Test News"
        )

        assertEquals("test-id", source.id)
        assertEquals("Test News", source.name)
    }

    @Test
    fun newsSource_withNullFields_handlesCorrectly() {
        val source = NewsSource(
            id = null,
            name = null
        )

        assertNull(source.id)
        assertNull(source.name)
    }
}

