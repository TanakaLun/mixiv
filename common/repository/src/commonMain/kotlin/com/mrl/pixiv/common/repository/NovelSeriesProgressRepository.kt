package com.mrl.pixiv.common.repository

import com.mrl.pixiv.common.data.Novel
import com.mrl.pixiv.common.mmkv.MMKVApp
import com.mrl.pixiv.common.mmkv.asMutableStateFlow
import com.mrl.pixiv.common.mmkv.mmkvSerializable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import org.koin.core.annotation.Single

@Serializable
data class NovelSeriesReadingProgress(
    val novelId: Long,
    val title: String,
    val fraction: Float,
)

internal fun seriesProgressKey(userId: Long, seriesId: Long) = "$userId:$seriesId"

fun readingProgressFraction(progress: NovelReadingProgress, paragraphs: List<String>): Float {
    val total = paragraphs.sumOf { it.length.toLong() }
    if (total == 0L) return 0f
    val index = progress.paragraphIndex.coerceIn(paragraphs.indices)
    val read = paragraphs.take(index).sumOf { it.length.toLong() } +
        progress.charIndex.coerceIn(0, paragraphs[index].length)
    return (read.toDouble() / total).toFloat().coerceIn(0f, 1f)
}

@Single
class NovelSeriesProgressRepository : MMKVApp {
    // 以账号和系列共同索引，避免切换账号后显示其他账号的阅读记录。
    private val novelSeriesProgress by mmkvSerializable<Map<String, NovelSeriesReadingProgress>>(
        emptyMap(),
    ).asMutableStateFlow()

    fun observe(seriesId: Long): Flow<NovelSeriesReadingProgress?> = combine(
        requireUserInfoFlow, novelSeriesProgress,
    ) { userInfo, progress ->
        progress[seriesProgressKey(userInfo.user.id, seriesId)]
    }.distinctUntilChanged()

    fun record(novel: Novel, fraction: Float) {
        val seriesId = novel.series.id?.takeIf { it > 0 } ?: return
        val key = seriesProgressKey(requireUserInfoValue.user.id, seriesId)
        novelSeriesProgress.update {
            it + (key to NovelSeriesReadingProgress(novel.id, novel.title, fraction.coerceIn(0f, 1f)))
        }
    }
}
