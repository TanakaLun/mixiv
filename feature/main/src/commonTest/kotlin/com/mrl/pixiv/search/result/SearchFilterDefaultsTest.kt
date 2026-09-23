package com.mrl.pixiv.search.result

import com.mrl.pixiv.common.data.AppViewMode
import com.mrl.pixiv.common.data.search.SearchAiType
import com.mrl.pixiv.common.data.search.SearchArtworkType
import com.mrl.pixiv.common.data.search.SearchContentFilter
import com.mrl.pixiv.common.data.search.SearchNovelQuery
import com.mrl.pixiv.common.data.search.SearchSort
import com.mrl.pixiv.common.data.search.SearchTarget
import com.mrl.pixiv.common.data.setting.SearchSettings
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

class SearchFilterDefaultsTest {
    @Test
    fun contentDefaultsAreInheritedOnceAndTemporaryChangesRemainLocal() {
        val defaults = SearchSettings(defaultContentFilter = SearchContentFilter(
            artworkType = SearchArtworkType.GIF_ONLY, seriesOnly = true,
        ))
        val initial = resolveInitialSearchFilter(defaults, AppViewMode.ILLUST, defaultShowR18 = true)
        assertEquals(true, initial.contentFilter.showR18)
        assertEquals(SearchArtworkType.GIF_ONLY, initial.contentFilter.artworkType)
        assertEquals(true, initial.contentFilter.seriesOnly)
        val temporary = initial.copy(contentFilter = initial.contentFilter.withR18Enabled(false))
        assertNotEquals(initial, temporary)
        assertEquals(initial, resolveInitialSearchFilter(defaults, AppViewMode.ILLUST, true))
        assertEquals(null, defaults.defaultContentFilter.showR18)
    }

    @Test
    fun illustrationSearchUsesEveryConfiguredDefault() {
        val settings = SearchSettings(
            defaultSearchTarget = SearchTarget.TITLE_AND_CAPTION,
            defaultSearchSort = SearchSort.POPULAR_FEMALE_DESC,
            defaultSearchAiType = SearchAiType.SHOW_AI,
        )

        val filter = resolveInitialSearchFilter(settings, AppViewMode.ILLUST)

        assertEquals(SearchTarget.TITLE_AND_CAPTION, filter.searchTarget)
        assertEquals(SearchSort.POPULAR_FEMALE_DESC, filter.sort)
        assertEquals(SearchAiType.SHOW_AI, filter.searchAiType)
    }

    @Test
    fun novelSearchNormalizesIllustrationOnlyDefaults() {
        val settings = SearchSettings(
            defaultSearchTarget = SearchTarget.TITLE_AND_CAPTION,
            defaultSearchSort = SearchSort.POPULAR_MALE_DESC,
            defaultSearchAiType = SearchAiType.SHOW_AI,
        )

        val filter = resolveInitialSearchFilter(settings, AppViewMode.NOVEL)

        assertEquals(SearchTarget.KEYWORD, filter.searchTarget)
        assertEquals(SearchSort.POPULAR_DESC, filter.sort)
        assertEquals(SearchAiType.SHOW_AI, filter.searchAiType)
    }

    @Test
    fun temporaryFilterChangesDoNotChangeConfiguredDefaults() {
        val settings = SearchSettings(
            defaultSearchTarget = SearchTarget.EXACT_MATCH_FOR_TAGS,
            defaultSearchSort = SearchSort.DATE_ASC,
            defaultSearchAiType = SearchAiType.HIDE_AI,
        )
        val initialFilter = resolveInitialSearchFilter(settings, AppViewMode.ILLUST)

        val temporaryFilter = initialFilter.copy(
            searchTarget = SearchTarget.PARTIAL_MATCH_FOR_TAGS,
            sort = SearchSort.DATE_DESC,
            searchAiType = SearchAiType.SHOW_AI,
        )

        assertNotEquals(initialFilter, temporaryFilter)
        assertEquals(
            initialFilter,
            resolveInitialSearchFilter(settings, AppViewMode.ILLUST),
        )
    }

    @Test
    fun novelQuerySendsConfiguredAiFilter() {
        val query = SearchNovelQuery(
            word = "test",
            searchAiType = SearchAiType.SHOW_AI,
        )

        assertEquals(
            SearchAiType.SHOW_AI.value.toString(),
            query.toMap()["search_ai_type"],
        )
    }
}
