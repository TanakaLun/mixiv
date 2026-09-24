package com.mrl.pixiv.comment.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.compose.ui.image.LoadingImage
import com.mrl.pixiv.common.compose.ui.image.UserAvatar
import com.mrl.pixiv.common.data.comment.Comment
import com.mrl.pixiv.common.data.comment.Emoji
import com.mrl.pixiv.common.kts.HSpacer
import com.mrl.pixiv.common.kts.VSpacer
import com.mrl.pixiv.common.kts.spaceBy
import com.mrl.pixiv.common.repository.CommentRepository
import com.mrl.pixiv.common.repository.isSelf
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.throttleClick
import com.mrl.pixiv.strings.block_comment
import com.mrl.pixiv.strings.cancel
import com.mrl.pixiv.strings.confirm
import com.mrl.pixiv.strings.confirm_to_delete_comment
import com.mrl.pixiv.strings.delete
import com.mrl.pixiv.strings.reply
import com.mrl.pixiv.strings.report_comment
import com.mrl.pixiv.strings.unblock_comment
import com.mrl.pixiv.strings.view_replies
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowListPopup

@Composable
fun CommentItem(
    comment: Comment,
    onReplyComment: () -> Unit,
    onBlockComment: () -> Unit,
    onReportComment: () -> Unit,
    onNavToUserProfile: () -> Unit,
    onDeleteComment: () -> Unit,
    modifier: Modifier = Modifier,
    isBlockScreen: Boolean = false,
    onViewReplies: (() -> Unit)? = null,
    onRemoveBlock: () -> Unit = {}
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirm by rememberSaveable { mutableStateOf(false) }
    val emojis by CommentRepository.emojiCacheFlow.collectAsStateWithLifecycle()

    Row(modifier = modifier) {
        UserAvatar(
            url = comment.user.profileImageUrls.medium,
            modifier = Modifier.size(70.dp),
            onClick = { onNavToUserProfile() }
        )
        10.HSpacer
        Column(
            modifier = Modifier.weight(1f),
        ) {
            Row(
                horizontalArrangement = 4.spaceBy,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = comment.user.name,
                    modifier = Modifier.weight(1f),
                    style = MiuixTheme.textStyles.subtitle,
                    maxLines = 1
                )
                if (comment.user.isSelf) {
                    Text(
                        text = stringResource(RStrings.delete),
                        modifier = Modifier
                            .throttleClick(indication = LocalIndication.current) {
                                showDeleteConfirm = true
                            }
                            .padding(4.dp),
                        color = MiuixTheme.colorScheme.primary,
                        style = MiuixTheme.textStyles.body1
                    )
                }
                if (!isBlockScreen) {
                    Text(
                        text = stringResource(RStrings.reply),
                        modifier = Modifier
                            .throttleClick(indication = LocalIndication.current) {
                                onReplyComment()
                            }
                            .padding(4.dp),
                        color = MiuixTheme.colorScheme.primary,
                        style = MiuixTheme.textStyles.body1
                    )
                }
                if (!comment.user.isSelf) {
                    Box {
                        Icon(
                            imageVector = Icons.Default.MoreHoriz,
                            contentDescription = null,
                            modifier = Modifier
                                .throttleClick(indication = LocalIndication.current) {
                                    showMenu = true
                                }
                                .padding(4.dp)
                                .size(24.dp)
                        )
                        WindowListPopup(
                            show = showMenu,
                            onDismissRequest = { showMenu = false },
                            content = {
                                ListPopupColumn {
                                    if (isBlockScreen) {
                                        BasicComponent(
                                            title = stringResource(RStrings.unblock_comment),
                                            onClick = {
                                                onRemoveBlock()
                                            },
                                        )
                                    } else {
                                        BasicComponent(
                                            title = stringResource(RStrings.block_comment),
                                            onClick = {
                                                onBlockComment()
                                                showMenu = false
                                            },
                                        )
                                        // 根据官方APP逻辑，纯stamp评论无法举报
                                        if (comment.stamp == null) {
                                            BasicComponent(
                                                title = stringResource(RStrings.report_comment),
                                                onClick = {
                                                    onReportComment()
                                                    showMenu = false
                                                },
                                            )
                                        }
                                    }
                                }
                            },
                        )
                    }
                }
            }
            if (comment.stamp != null) {
                LoadingImage(
                    model = comment.stamp!!.stampUrl,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp)
                )
            } else {
                val emojiMap = remember(emojis) {
                    emojis?.emojiDefinitions?.associateBy { it.slug } ?: emptyMap()
                }
                val annotatedString = remember(comment.comment, emojiMap) {
                    parseComment(comment.comment, emojiMap)
                }
                val inlineContent = remember(emojiMap) {
                    emojiMap.mapValues { (_, emoji) ->
                        InlineTextContent(
                            Placeholder(
                                width = 1.em,
                                height = 1.em,
                                placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                            )
                        ) {
                            LoadingImage(
                                model = emoji.imageUrlMedium,
                                contentDescription = emoji.slug,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
                Text(
                    text = annotatedString,
                    style = MiuixTheme.textStyles.body1,
                    inlineContent = inlineContent
                )
            }
            if (!isBlockScreen && comment.hasReplies && onViewReplies != null) {
                Text(
                    text = stringResource(RStrings.view_replies),
                    color = MiuixTheme.colorScheme.primary,
                    style = MiuixTheme.textStyles.footnote1,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .throttleClick { onViewReplies() }
                )
            }
            8.VSpacer
            Text(
                text = comment.dateString,
                style = MiuixTheme.textStyles.body2,
            )
        }
    }

    if (showDeleteConfirm) {
        OverlayDialog(
            show = true,
            title = stringResource(RStrings.confirm_to_delete_comment),
            onDismissRequest = { showDeleteConfirm = false },
            content = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    TextButton(
                        text = stringResource(RStrings.cancel),
                        onClick = { showDeleteConfirm = false },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TextButton(
                        text = stringResource(RStrings.confirm),
                        onClick = {
                            onDeleteComment()
                            showDeleteConfirm = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            },
        )
    }
}

private fun parseComment(comment: String, emojiMap: Map<String, Emoji>): AnnotatedString {
    return buildAnnotatedString {
        var lastIndex = 0
        val regex = "\\(([^)]+)\\)".toRegex()
        regex.findAll(comment).forEach { matchResult ->
            val (slug) = matchResult.destructured
            if (emojiMap.containsKey(slug)) {
                append(comment.substring(lastIndex, matchResult.range.first))
                appendInlineContent(slug, "($slug)")
                lastIndex = matchResult.range.last + 1
            }
        }
        if (lastIndex < comment.length) {
            append(comment.substring(lastIndex))
        }
    }
}
