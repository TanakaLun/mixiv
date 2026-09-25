package com.mrl.pixiv.comment

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.comment.components.CommentItem
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.kts.VSpacer
import com.mrl.pixiv.common.kts.hPadding
import com.mrl.pixiv.common.kts.spaceBy
import com.mrl.pixiv.common.repository.BlockingRepositoryV2
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.block_comments
import com.mrl.pixiv.strings.no_blocked_items
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BlockCommentsScreen(
    modifier: Modifier = Modifier,
) {
    val navigationManager = currentNavigationManager()
    val blockedComments by BlockingRepositoryV2.blockCommentsFlow
        .collectAsStateWithLifecycle(emptyList())

    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.block_comments),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = navigationManager::popBackStack
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        if (blockedComments.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(RStrings.no_blocked_items),
                    style = MiuixTheme.textStyles.main,
                )
            }
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .pageScrollModifiers(scrollBehavior),
            contentPadding = 8.hPadding,
            verticalArrangement = 8.spaceBy
        ) {
            itemsIndexed(
                items = blockedComments,
                key = { _, item -> item.id }
            ) { index, comment ->
                CommentItem(
                    comment = comment,
                    onReplyComment = {},
                    onBlockComment = {},
                    onReportComment = {},
                    onNavToUserProfile = {
                        navigationManager.navigateToProfileDetailScreen(comment.user.id)
                    },
                    onDeleteComment = {},
                    isBlockScreen = true,
                    onRemoveBlock = {
                        BlockingRepositoryV2.removeBlockComment(comment.id)
                    }
                )
                if (index != blockedComments.size - 1) {
                    8.VSpacer
                    HorizontalDivider()
                }
            }
        }
    }
}
