package com.mrl.pixiv.novel

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.AssistChip
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.compose.ui.image.UserAvatar
import com.mrl.pixiv.common.data.Novel
import com.mrl.pixiv.common.repository.viewmodel.bookmark.isBookmark
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.convertUtcStringToLocalDateTime
import com.mrl.pixiv.common.util.throttleClick
import com.mrl.pixiv.strings.bookmarked
import com.mrl.pixiv.strings.novel_work_information
import com.mrl.pixiv.strings.view_comments
import com.mrl.pixiv.strings.view_comments_count
import com.mrl.pixiv.strings.word_count
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme

internal fun validNovelSeriesId(seriesId: Long?): Long? = seriesId?.takeIf { it > 0L }

internal fun adjustedNovelBookmarkCount(
    totalBookmarks: Long,
    initiallyBookmarked: Boolean,
    currentlyBookmarked: Boolean,
): Long = (totalBookmarks + when {
    currentlyBookmarked && !initiallyBookmarked -> 1L
    !currentlyBookmarked && initiallyBookmarked -> -1L
    else -> 0L
}).coerceAtLeast(0L)

@Composable
internal fun NovelMetadataBottomSheet(
    novel: Novel,
    onDismissRequest: () -> Unit,
    onAuthorClick: (Long) -> Unit,
    onSeriesClick: (Long) -> Unit,
    onTagClick: (String) -> Unit,
    onCaptionLinkClick: (String) -> Unit,
    onCommentClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentBookmarkState = novel.isBookmark
    val totalBookmarks = adjustedNovelBookmarkCount(
        totalBookmarks = novel.totalBookmarks,
        initiallyBookmarked = novel.isBookmarked,
        currentlyBookmarked = currentBookmarkState,
    )
    val seriesId = validNovelSeriesId(novel.series.id)

    OverlayBottomSheet(
        show = true,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item(key = "metadata_heading") {
                Text(
                    text = stringResource(RStrings.novel_work_information),
                    style = MiuixTheme.textStyles.title2,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp),
                )
            }

            item(key = "metadata_title") {
                Text(
                    text = novel.title,
                    style = MiuixTheme.textStyles.title3,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }

            item(key = "metadata_author") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .throttleClick { onAuthorClick(novel.user.id) }
                        .padding(vertical = 12.dp),
                ) {
                    UserAvatar(
                        url = novel.user.profileImageUrls.medium,
                        modifier = Modifier.size(36.dp),
                        onClick = throttleClick { onAuthorClick(novel.user.id) },
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = novel.user.name,
                        style = MiuixTheme.textStyles.subtitle,
                        color = MiuixTheme.colorScheme.primary,
                        maxLines = 1,
                    )
                }
            }

            novel.series.title?.let { seriesTitle ->
                item(key = "metadata_series") {
                    Text(
                        text = seriesTitle,
                        style = MiuixTheme.textStyles.subtitle,
                        color = MiuixTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .then(
                                if (seriesId != null) {
                                    Modifier.throttleClick { onSeriesClick(seriesId) }
                                } else {
                                    Modifier
                                }
                            )
                            .padding(vertical = 8.dp),
                    )
                }
            }

            item(key = "metadata_stats") {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = stringResource(RStrings.bookmarked),
                        modifier = Modifier.size(16.dp),
                        tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                    )
                    Text(
                        text = totalBookmarks.toString(),
                        style = MiuixTheme.textStyles.body1,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Icon(
                        imageVector = Icons.Rounded.Visibility,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                    )
                    Text(
                        text = novel.totalView.toString(),
                        style = MiuixTheme.textStyles.body1,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                    Spacer(modifier = Modifier.size(16.dp))
                    Icon(
                        imageVector = Icons.Rounded.TextFields,
                        contentDescription = stringResource(RStrings.word_count),
                        modifier = Modifier.size(16.dp),
                        tint = MiuixTheme.colorScheme.onSurfaceVariantActions,
                    )
                    Text(
                        text = novel.textLength.toString(),
                        style = MiuixTheme.textStyles.body1,
                        modifier = Modifier.padding(start = 4.dp),
                    )
                }
            }

            item(key = "metadata_created_at") {
                Text(
                    text = convertUtcStringToLocalDateTime(novel.createDate),
                    style = MiuixTheme.textStyles.body1,
                    color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    textAlign = TextAlign.Center,
                )
            }

            if (novel.tags.isNotEmpty()) {
                item(key = "metadata_tags") {
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        novel.tags.forEach { tag ->
                            AssistChip(
                                onClick = throttleClick { onTagClick(tag.name) },
                                label = {
                                    Text(
                                        text = buildString {
                                            append('#')
                                            append(tag.name)
                                            if (tag.translatedName.isNotEmpty()) {
                                                append(' ')
                                                append(tag.translatedName)
                                            }
                                        }
                                    )
                                },
                            )
                        }
                    }
                }
            }

            if (novel.caption.isNotEmpty()) {
                item(key = "metadata_caption") {
                    val linkColor = MiuixTheme.colorScheme.primary
                    val caption = remember(novel.caption, linkColor, onCaptionLinkClick) {
                        novelCaptionToAnnotatedString(
                            html = novel.caption,
                            linkColor = linkColor,
                            onLinkClick = onCaptionLinkClick,
                        )
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                    ) {
                        Text(
                            text = caption,
                            style = MiuixTheme.textStyles.body1,
                            modifier = Modifier.padding(16.dp),
                        )
                    }
                }
            }

            item(key = "metadata_comments") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .throttleClick(onClick = onCommentClick)
                        .padding(vertical = 16.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = stringResource(RStrings.view_comments),
                    )
                    Text(
                        text = novel.totalComments?.let { count ->
                            stringResource(RStrings.view_comments_count, count)
                        } ?: stringResource(RStrings.view_comments),
                        style = MiuixTheme.textStyles.main,
                        modifier = Modifier.padding(start = 5.dp),
                    )
                }
            }
        }
    }
}
