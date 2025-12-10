package com.microhabitcoach.data.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HackerNewsItemTest {

    @Test
    fun hackerNewsItem_creation_setsAllFields() {
        val item = HackerNewsItem(
            id = 12345,
            title = "Test Title",
            text = "Test text",
            url = "https://example.com",
            by = "testuser",
            time = 1234567890L,
            type = "story",
            score = 100,
            descendants = 50,
            kids = listOf(1, 2, 3)
        )

        assertEquals(12345, item.id)
        assertEquals("Test Title", item.title)
        assertEquals("Test text", item.text)
        assertEquals("https://example.com", item.url)
        assertEquals("testuser", item.by)
        assertEquals(1234567890L, item.time)
        assertEquals("story", item.type)
        assertEquals(100, item.score)
        assertEquals(50, item.descendants)
        assertEquals(listOf(1, 2, 3), item.kids)
    }

    @Test
    fun getContentForClassification_withTitleAndText_combinesBoth() {
        val item = HackerNewsItem(
            id = 1,
            title = "Test Title",
            text = "Test content",
            url = null,
            by = "user",
            time = 0L,
            type = "story"
        )

        val content = item.getContentForClassification()

        assertTrue(content.contains("Test Title"))
        assertTrue(content.contains("Test content"))
    }

    @Test
    fun getContentForClassification_onlyTitle_returnsTitle() {
        val item = HackerNewsItem(
            id = 1,
            title = "Test Title",
            text = null,
            url = null,
            by = "user",
            time = 0L,
            type = "story"
        )

        val content = item.getContentForClassification()

        assertEquals("Test Title ", content)
    }

    @Test
    fun getContentForClassification_onlyText_returnsText() {
        val item = HackerNewsItem(
            id = 1,
            title = null,
            text = "Test content",
            url = null,
            by = "user",
            time = 0L,
            type = "story"
        )

        val content = item.getContentForClassification()

        assertEquals(" Test content", content)
    }

    @Test
    fun isStory_withStoryTypeAndTitle_returnsTrue() {
        val item = HackerNewsItem(
            id = 1,
            title = "Test Title",
            text = null,
            url = null,
            by = "user",
            time = 0L,
            type = "story"
        )

        assertTrue(item.isStory())
    }

    @Test
    fun isStory_withCommentType_returnsFalse() {
        val item = HackerNewsItem(
            id = 1,
            title = "Test Title",
            text = null,
            url = null,
            by = "user",
            time = 0L,
            type = "comment"
        )

        assertFalse(item.isStory())
    }

    @Test
    fun isStory_withNullTitle_returnsFalse() {
        val item = HackerNewsItem(
            id = 1,
            title = null,
            text = "Test",
            url = null,
            by = "user",
            time = 0L,
            type = "story"
        )

        assertFalse(item.isStory())
    }

    @Test
    fun isStory_withBlankTitle_returnsFalse() {
        val item = HackerNewsItem(
            id = 1,
            title = "   ",
            text = "Test",
            url = null,
            by = "user",
            time = 0L,
            type = "story"
        )

        assertFalse(item.isStory())
    }
}

