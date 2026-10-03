package com.mrl.pixiv.profile.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mrl.pixiv.common.compose.ui.illust.SquareIllustItem
import com.mrl.pixiv.common.data.Illust
import com.mrl.pixiv.common.kts.spaceBy
import com.mrl.pixiv.common.repository.viewmodel.bookmark.BookmarkState
import com.mrl.pixiv.common.repository.viewmodel.bookmark.isBookmark
import com.mrl.pixiv.common.router.NavigateToHorizontalPictureScreen
import com.mrl.pixiv.common.util.throttleClick
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ExpandMore

private const val SPAN_COUNT = 3
private const val MAX_SHOW_ILLUST_COUNT = 6

@Composable
fun IllustWidget(
    title: String,
    endText: String = "",
    navToPictureScreen: NavigateToHorizontalPictureScreen,
    illusts: List<Illust>,
    modifier: Modifier = Modifier,
    onAllClick: () -> Unit = {},
    moreOverlayInLastCell: Boolean = false,
) {
    Column(
        modifier = modifier
    ) {
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))
        if (moreOverlayInLastCell) {
            SmallTitle(
                text = title,
                insideMargin = PaddingValues(horizontal = 0.dp),
            )
        } else {
            Box(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.CenterStart)
                )
                Row(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .throttleClick {
                            onAllClick()
                        }
                ) {
                    Text(
                        text = endText,
                        fontSize = 12.sp,
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                        contentDescription = null,
                        modifier = Modifier
                            .align(Alignment.CenterVertically)
                            .size(12.dp),
                        tint = Color.Blue
                    )
                }
            }
        }
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
                if (moreOverlayInLastCell && index == takenIllusts.lastIndex) {
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
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black.copy(alpha = 0.35f))
                                .throttleClick(onClick = onAllClick),
                        )
                        IconButton(
                            onClick = onAllClick,
                            modifier = Modifier.align(Alignment.Center),
                        ) {
                            Icon(
                                imageVector = MiuixIcons.ExpandMore,
                                contentDescription = null,
                                tint = Color.White,
                            )
                        }
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