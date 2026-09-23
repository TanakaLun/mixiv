package com.mrl.pixiv.novel

import com.mrl.pixiv.common.data.AiType
import com.mrl.pixiv.common.data.ImageUrls
import com.mrl.pixiv.common.data.Novel
import com.mrl.pixiv.common.data.Series
import com.mrl.pixiv.common.data.User
import com.mrl.pixiv.common.data.XRestrict
import com.mrl.pixiv.common.repository.NovelReadingProgress
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class NovelReadingPositionTest {
    @Test
    fun returningFromCommentsRestoresTheLatestPositionWithoutRequestingAnotherJump() {
        val old = NovelReadingProgress(5, 10, 123)
        val latest = NovelReadingProgress(10, 30, 456)
        val loaded = NovelState(novel = novel(1), restoreProgress = old, restoreVersion = 1)
        val afterReading = loaded.withLatestReadingProgress(1, latest)
        assertEquals(latest, afterReading.restoreProgress)
        assertEquals(1, afterReading.restoreVersion)
    }

    @Test
    fun scrollingBackwardsBeforeRotationKeepsTheEarlierPosition() {
        val loaded = NovelState(novel = novel(1), restoreProgress = NovelReadingProgress(80, 80, 123))
        val latest = NovelReadingProgress(70, 10, 456)
        assertEquals(latest, loaded.withLatestReadingProgress(1, latest).restoreProgress)
        assertNull(loaded.withLatestReadingProgress(1, null).restoreProgress)
    }

    @Test
    fun disposingThePreviousChapterCannotOverwriteTheNewChapter() {
        val loaded = NovelState(novel = novel(2), restoreProgress = NovelReadingProgress(2, 0, 123))
        assertEquals(loaded, loaded.withLatestReadingProgress(1, NovelReadingProgress(80, 0, 456)))
        assertEquals(loaded, loaded.withLatestReadingProgress(1, null))
    }

    private fun novel(id: Long) = Novel(
        id = id,
        title = "Novel $id",
        caption = "",
        restrict = 0,
        xRestrict = XRestrict.Normal,
        isOriginal = true,
        imageUrls = ImageUrls(),
        createDate = "",
        tags = emptyList(),
        pageCount = 1,
        textLength = 1,
        user = User(id = 1, name = "Author"),
        series = Series(id = 0, title = ""),
        isBookmarked = false,
        totalBookmarks = 0,
        totalView = 0,
        visible = true,
        isMuted = false,
        isMypixivOnly = false,
        isXRestricted = false,
        novelAiType = AiType.NotAiGeneratedWork,
    )
}
