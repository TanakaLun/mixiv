package com.mrl.pixiv.profile.detail.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil3.compose.AsyncImage
import com.mrl.pixiv.common.data.Novel
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.throttleClick
import com.mrl.pixiv.strings.novel_description
import com.mrl.pixiv.strings.novels
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.LocalContentColor

private const val MAX_NOVEL_WORKS_PREVIEW_COUNT = 3

internal fun shouldShowNovelWorks(novels: Collection<*>): Boolean = novels.isNotEmpty()

internal fun <T> previewNovelWorks(novels: List<T>): List<T> =
    novels.take(MAX_NOVEL_WORKS_PREVIEW_COUNT)

@Composable
fun NovelWorksWidget(
    novels: List<Novel>,
    onAllClick: () -> Unit,
    onNovelClick: (Long) -> Unit,
    onSeriesClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        SmallTitle(
            text = stringResource(RStrings.novels),
            insideMargin = PaddingValues(horizontal = 0.dp),
        )
        HorizontalDivider(modifier = Modifier.padding(top = 5.dp))
        val shownNovels = previewNovelWorks(novels)
        shownNovels.forEachIndexed { index, novel ->
            NovelWorkPreviewItem(
                novel = novel,
                onNovelClick = onNovelClick,
                onSeriesClick = onSeriesClick,
                modifier = Modifier.padding(top = 10.dp),
                moreOverlay = index == shownNovels.lastIndex,
                onMoreOverlayClick = onAllClick,
            )
        }
    }
}

@Composable
private fun NovelWorkPreviewItem(
    novel: Novel,
    onNovelClick: (Long) -> Unit,
    onSeriesClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    moreOverlay: Boolean = false,
    onMoreOverlayClick: () -> Unit = {},
) {
    val seriesId = novel.series.id?.takeIf { it > 0L }
    val seriesTitle = novel.series.title?.takeIf { it.isNotEmpty() }

    Column(modifier = modifier) {
        Row {
            Box {
                AsyncImage(
                    modifier = Modifier
                        .size(width = 64.dp, height = 90.dp)
                        .throttleClick { onNovelClick(novel.id) },
                    model = novel.imageUrls.medium,
                    contentDescription = novel.title,
                )
                if (moreOverlay) {
                    MoreOverlayCover(
                        onClick = onMoreOverlayClick,
                        modifier = Modifier.matchParentSize(),
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                if (seriesId != null && seriesTitle != null) {
                    Text(
                        text = seriesTitle,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = LocalContentColor.current.copy(alpha = 0.7f),
                        modifier = Modifier.throttleClick {
                            onSeriesClick(seriesId)
                        },
                    )
                }
                Text(
                    text = novel.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(top = if (seriesTitle == null) 0.dp else 5.dp)
                        .throttleClick { onNovelClick(novel.id) },
                )
                Text(
                    text = "by ${novel.user.name}",
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 5.dp),
                )
                Text(
                    text = stringResource(
                        RStrings.novel_description,
                        novel.textLength,
                        novel.tags.joinToString(" ") { "#${it.name}" },
                    ),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 15.dp, vertical = 15.dp),
        )
    }
}