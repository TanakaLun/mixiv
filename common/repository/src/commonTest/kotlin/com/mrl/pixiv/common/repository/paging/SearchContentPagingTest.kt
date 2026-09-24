package com.mrl.pixiv.common.repository.paging

import androidx.paging.PagingSource
import com.mrl.pixiv.common.data.Illust
import com.mrl.pixiv.common.data.ImageUrls
import com.mrl.pixiv.common.data.MetaSinglePage
import com.mrl.pixiv.common.data.Type
import com.mrl.pixiv.common.data.User
import com.mrl.pixiv.common.data.search.SearchArtworkType
import com.mrl.pixiv.common.data.search.SearchContentFilter
import com.mrl.pixiv.common.data.search.SearchIllustQuery
import com.mrl.pixiv.common.repository.feed.FeedCapability
import com.mrl.pixiv.common.repository.feed.FeedKey
import com.mrl.pixiv.common.repository.feed.FeedPage
import com.mrl.pixiv.common.repository.feed.FeedPageRequest
import com.mrl.pixiv.common.repository.feed.FeedSource
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull

class SearchContentPagingTest {
    @Test
    fun filteredEmptyPagesAreSkippedAndAllConditionsSurvivePagination() = runTest {
        val offsets = mutableListOf<Int>()
        val query = SearchIllustQuery(
            word = "cat", bookmarkNumMin = 100, startDate = "2026-01-01",
            contentFilter = SearchContentFilter(SearchArtworkType.GIF_ONLY, showR18 = true, r18Only = true),
        )
        val source = object : FeedSource<Illust> {
            override val capability = FeedCapability.OFFSET
            override suspend fun load(request: FeedPageRequest): FeedPage<Illust> {
                val offset = (request.key as? FeedKey.Offset)?.value ?: 0
                offsets += offset
                return FeedPage(if (offset == 30) listOf(illust()) else emptyList(), FeedKey.Offset(offset + 30))
            }
        }
        val paging = SearchIllustPagingSource(query, true, false, source)
        val page = assertIs<PagingSource.LoadResult.Page<SearchIllustQuery, Illust>>(
            paging.load(PagingSource.LoadParams.Refresh(null, 20, false)),
        )
        assertEquals(listOf(0, 30), offsets)
        assertEquals(listOf(1L), page.data.map { it.id })
        assertEquals(query.copy(offset = 60), page.nextKey)
    }

    @Test
    fun emptyLastPageFinishesWithoutRepeatingRequests() = runTest {
        var requests = 0
        val source = object : FeedSource<Illust> {
            override val capability = FeedCapability.SINGLE_PAGE
            override suspend fun load(request: FeedPageRequest): FeedPage<Illust> {
                requests++
                return FeedPage(emptyList())
            }
        }
        val paging = SearchIllustPagingSource(SearchIllustQuery(word = "cat"), false, false, source)
        val page = assertIs<PagingSource.LoadResult.Page<SearchIllustQuery, Illust>>(
            paging.load(PagingSource.LoadParams.Refresh(null, 20, false)),
        )
        assertEquals(1, requests)
        assertNull(page.nextKey)
    }

    private fun illust() = Illust(
        id = 1, title = "", type = Type.Ugoira, imageUrls = ImageUrls(), user = User(),
        width = 100, height = 100, metaSinglePage = MetaSinglePage(), totalView = 0,
        totalBookmarks = 0, isBookmarked = false,
    )
}
