package com.mrl.pixiv.common.data.search

import com.mrl.pixiv.common.data.Illust
import com.mrl.pixiv.common.data.Novel
import com.mrl.pixiv.common.data.Type
import com.mrl.pixiv.common.data.XRestrict
import kotlinx.serialization.Serializable

@Serializable
enum class SearchArtworkType { ALL, GIF_ONLY, EXCLUDE_GIF, MANGA_ONLY }

@Serializable
data class SearchContentFilter(
    val artworkType: SearchArtworkType = SearchArtworkType.ALL,
    val seriesOnly: Boolean = false,
    val showR18: Boolean? = null,
    val r18Only: Boolean = false,
) {
    fun withR18Enabled(enabled: Boolean) = copy(showR18 = enabled, r18Only = enabled && r18Only)

    fun matches(illust: Illust, defaultShowR18: Boolean): Boolean =
        matchesRating(illust.xRestrict, defaultShowR18) && when (artworkType) {
            SearchArtworkType.ALL -> true
            SearchArtworkType.GIF_ONLY -> illust.type == Type.Ugoira
            SearchArtworkType.EXCLUDE_GIF -> illust.type != Type.Ugoira
            SearchArtworkType.MANGA_ONLY -> illust.type == Type.Manga
        }

    fun matches(novel: Novel, defaultShowR18: Boolean): Boolean =
        matchesRating(novel.xRestrict, defaultShowR18) &&
            (!seriesOnly || (novel.series.id ?: 0) > 0)

    private fun matchesRating(rating: XRestrict, defaultShowR18: Boolean): Boolean =
        if (!(showR18 ?: defaultShowR18)) rating == XRestrict.Normal
        else !r18Only || rating != XRestrict.Normal
}
