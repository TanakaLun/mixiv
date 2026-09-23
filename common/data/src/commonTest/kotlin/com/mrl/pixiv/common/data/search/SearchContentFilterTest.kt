package com.mrl.pixiv.common.data.search

import com.mrl.pixiv.common.data.*
import com.mrl.pixiv.common.data.setting.SearchSettings
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SearchContentFilterTest {
    @Test
    fun artworkTypesAreExclusiveAndMangaDoesNotIncludeMultiPageIllustrations() {
        val gif = illust(Type.Ugoira)
        val manga = illust(Type.Manga)
        val illustration = illust(Type.Illust).copy(pageCount = 20)
        assertTrue(SearchContentFilter(artworkType = SearchArtworkType.GIF_ONLY).matches(gif, false))
        assertFalse(SearchContentFilter(artworkType = SearchArtworkType.GIF_ONLY).matches(manga, false))
        assertFalse(SearchContentFilter(artworkType = SearchArtworkType.EXCLUDE_GIF).matches(gif, false))
        assertTrue(SearchContentFilter(artworkType = SearchArtworkType.EXCLUDE_GIF).matches(illustration, false))
        assertTrue(SearchContentFilter(artworkType = SearchArtworkType.MANGA_ONLY).matches(manga, false))
        assertFalse(SearchContentFilter(artworkType = SearchArtworkType.MANGA_ONLY).matches(illustration, false))
    }

    @Test
    fun disablingR18ClearsTheOnlyOptionAndAnExplicitSearchOverrideWinsOverDefaults() {
        val only = SearchContentFilter(showR18 = true, r18Only = true)
        assertFalse(only.matches(illust(Type.Illust), false))
        assertTrue(only.matches(illust(Type.Illust).copy(xRestrict = XRestrict(1)), false))
        assertTrue(only.matches(illust(Type.Illust).copy(xRestrict = XRestrict(2)), false))
        val disabled = only.withR18Enabled(false)
        assertFalse(disabled.r18Only)
        assertTrue(disabled.matches(illust(Type.Illust), true))
        assertFalse(disabled.matches(illust(Type.Illust).copy(xRestrict = XRestrict(1)), true))
    }

    @Test
    fun seriesFilterUsesSeriesMembershipAndIgnoresArtworkOnlyOptionsForNovels() {
        val filter = SearchContentFilter(artworkType = SearchArtworkType.GIF_ONLY, seriesOnly = true)
        assertTrue(filter.matches(novel(1).copy(series = Series(123, "Series")), false))
        assertFalse(filter.matches(novel(1).copy(series = Series()), false))
        assertFalse(filter.matches(novel(1).copy(series = Series(0, "")), false))
    }

    @Test
    fun savedDefaultsRoundTripAndOldSettingsKeepTheirBehavior() {
        assertEquals(SearchContentFilter(), Json.decodeFromString<SearchSettings>("{}").defaultContentFilter)
        val settings = SearchSettings(defaultContentFilter = SearchContentFilter(
            SearchArtworkType.MANGA_ONLY, seriesOnly = true, showR18 = true, r18Only = true,
        ))
        assertEquals(settings, Json.decodeFromString<SearchSettings>(Json.encodeToString(settings)))
    }

    private fun illust(type: Type) = Illust(
        id = 1, title = "", type = type, imageUrls = ImageUrls(), user = User(),
        width = 100, height = 100, metaSinglePage = MetaSinglePage(), totalView = 0,
        totalBookmarks = 0, isBookmarked = false,
    )

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
