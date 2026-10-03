package com.mrl.pixiv.profile.detail.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.compose.ui.illust.SquareIllustItem
import com.mrl.pixiv.common.data.Illust
import com.mrl.pixiv.common.kts.spaceBy
import com.mrl.pixiv.common.repository.viewmodel.bookmark.BookmarkState
import com.mrl.pixiv.common.repository.viewmodel.bookmark.isBookmark
import com.mrl.pixiv.common.router.NavigateToHorizontalPictureScreen
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.SmallTitle

private const val SPAN_COUNT = 3
private const val MAX_SHOW_ILLUST_COUNT = 6

@Composable
fun IllustWidget(
    title: String,
    navToPictureScreen: NavigateToHorizontalPictureScreen,
    illusts: List<Illust>,
    modifier: Modifier = Modifier,
    onAllClick: () -> Unit = {},
) {
    Column(
        modifier = modifier
    ) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
        SmallTitle(
            text = title,
            insideMargin = PaddingValues(horizontal = 0.dp),
        )
        FlowRow(
            modifier = Modifier.padding(top = 10.dp),
            horizontalArrangement = 5f.spaceBy,
            verticalArrangement = 5f.spaceBy,
            maxItemsInEachRow = SPAN_COUNT,
        ) {
            val takenIllusts = illusts.take(MAX_SHOW_ILLUST_COUNT)
            val remainder = takenIllusts.size % SPAN_COUNT
            val spacerCount = if (remainder == 0) 0 else SPAN_COUNT - remainder
            takenIllusts.forEachIndexed { index, illust ->
                val isBookmarked = illust.isBookmark
                val cellModifier = Modifier.weight(1f)
                if (index == takenIllusts.lastIndex) {
                    Box(
                        modifier = cellModifier.aspectRatio(1f)
                    ) {
                        SquareIllustItem(
                            illust = illust,
                            isBookmarked = isBookmarked,
                            onBookmarkClick = { restrict, tags, isEdit ->
                                if (isEdit || !isBookmarked) {
                                    BookmarkState.bookmarkIllust(illust.id, restrict, tags)
                                } else {
                                    BookmarkState.deleteBookmarkIllust(illust.id)
                                }
                            },
                            navToPictureScreen = { prefix, enableTransition ->
                                navToPictureScreen(takenIllusts, index, prefix, enableTransition)
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                        MoreOverlayCover(
                            onClick = onAllClick,
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp)),
                        )
                    }
                } else {
                    SquareIllustItem(
                        illust = illust,
                        isBookmarked = isBookmarked,
                        onBookmarkClick = { restrict, tags, isEdit ->
                            if (isEdit || !isBookmarked) {
                                BookmarkState.bookmarkIllust(illust.id, restrict, tags)
                            } else {
                                BookmarkState.deleteBookmarkIllust(illust.id)
                            }
                        },
                        navToPictureScreen = { prefix, enableTransition ->
                            navToPictureScreen(takenIllusts, index, prefix, enableTransition)
                        },
                        modifier = cellModifier
                    )
                }
            }
            if (spacerCount > 0) {
                Spacer(modifier = Modifier.weight(spacerCount.toFloat()))
            }
        }
    }
}