package com.mrl.pixiv.common.repository

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class NovelSeriesProgressTest {
    @Test
    fun chapterProgressUsesTextPositionAndHandlesEmptyChapters() {
        assertEquals(0.75f, readingProgressFraction(NovelReadingProgress(1, 2, 0), listOf("abcd", "efgh")))
        assertEquals(0f, readingProgressFraction(NovelReadingProgress(0, 0, 0), emptyList()))
        assertEquals(1f, readingProgressFraction(NovelReadingProgress(9, 99, 0), listOf("abcd")))
    }

    @Test
    fun seriesRecordsAreIsolatedByAccountAndSeries() {
        assertNotEquals(seriesProgressKey(1, 23), seriesProgressKey(12, 3))
        assertNotEquals(seriesProgressKey(1, 23), seriesProgressKey(2, 23))
    }
}
