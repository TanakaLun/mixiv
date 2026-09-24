package com.mrl.pixiv.setting.block

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.state.ToggleableState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.compose.lightBlue
import com.mrl.pixiv.common.repository.BlockingRepositoryV2
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.add_tags
import com.mrl.pixiv.strings.block_illust
import com.mrl.pixiv.strings.block_novel
import com.mrl.pixiv.strings.block_tag_as_regex
import com.mrl.pixiv.strings.block_tags
import com.mrl.pixiv.strings.block_user
import com.mrl.pixiv.strings.cancel
import com.mrl.pixiv.strings.confirm
import com.mrl.pixiv.strings.no_blocked_items
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Checkbox
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Spacer
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BlockIllustScreen(
    modifier: Modifier = Modifier,
) {
    val blockedIllusts by BlockingRepositoryV2.blockIllustItemsFlow
        .collectAsStateWithLifecycle(emptyList())
    BlockTextScreen(
        title = stringResource(RStrings.block_illust),
        items = blockedIllusts,
        onRemove = { item ->
            BlockingRepositoryV2.removeBlockIllust(item.illustId)
        },
        modifier = modifier,
        itemContent = {
            Text(
                text = it.title.ifBlank { it.illustId.toString() },
                style = MiuixTheme.textStyles.body1,
                modifier = Modifier.weight(1f),
            )
        }
    )
}

@Composable
fun BlockNovelScreen(
    modifier: Modifier = Modifier,
) {
    val blockedNovels by BlockingRepositoryV2.blockNovelItemsFlow
        .collectAsStateWithLifecycle(emptyList())
    BlockTextScreen(
        title = stringResource(RStrings.block_novel),
        items = blockedNovels,
        onRemove = { item ->
            BlockingRepositoryV2.removeBlockNovel(item.novelId)
        },
        modifier = modifier,
        itemContent = {
            Text(
                text = it.title.ifBlank { it.novelId.toString() },
                style = MiuixTheme.textStyles.body1,
                modifier = Modifier.weight(1f),
            )
        }
    )
}

@Composable
fun BlockUserScreen(
    modifier: Modifier = Modifier,
) {
    val blockedUsers by BlockingRepositoryV2.blockUserItemsFlow
        .collectAsStateWithLifecycle(emptyList())
    BlockTextScreen(
        title = stringResource(RStrings.block_user),
        items = blockedUsers,
        onRemove = { item ->
            BlockingRepositoryV2.removeBlockUser(item.userId)
        },
        modifier = modifier,
        itemContent = {
            Text(
                text = it.name.ifBlank { it.userId.toString() },
                style = MiuixTheme.textStyles.body1,
                modifier = Modifier.weight(1f),
            )
        }
    )
}

@Composable
fun BlockTagScreen(
    modifier: Modifier = Modifier,
) {
    val blockedTags by BlockingRepositoryV2.blockTagItemsFlow.collectAsStateWithLifecycle(emptyList())
    val tags = blockedTags.sortedBy { it.tag.lowercase() }
    var showAddDialog by remember { mutableStateOf(false) }
    var inputTag by remember { mutableStateOf("") }
    var isRegex by remember { mutableStateOf(false) }
    val normalizedTag = inputTag.trim()
    val regexValid = !isRegex || normalizedTag.isBlank() || runCatching { Regex(normalizedTag) }.isSuccess
    val canConfirm = normalizedTag.isNotEmpty() && regexValid

    BlockTextScreen(
        title = stringResource(RStrings.block_tags),
        items = tags,
        onRemove = { item -> BlockingRepositoryV2.removeBlockTag(item.tag) },
        modifier = modifier,
        topBarActions = {
            IconButton(
                onClick = {
                    inputTag = ""
                    isRegex = false
                    showAddDialog = true
                }
            ) {
                Icon(
                    imageVector = Icons.Rounded.Add,
                    contentDescription = stringResource(RStrings.add_tags),
                )
            }
        },
        itemContent = {
            Text(
                text = it.tag,
                modifier = Modifier.weight(1f),
                color = if (it.isRegex) lightBlue else Color.Unspecified,
                style = MiuixTheme.textStyles.body1,
            )
        }
    )

    if (showAddDialog) {
        OverlayDialog(
            show = true,
            title = stringResource(RStrings.add_tags),
            onDismissRequest = { showAddDialog = false },
            content = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    TextField(
                        value = inputTag,
                        onValueChange = { inputTag = it },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        label = stringResource(RStrings.add_tags),
                    )
                    if (!regexValid && normalizedTag.isNotBlank()) {
                        Text(
                            text = " ",
                            color = MiuixTheme.colorScheme.error,
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            state = ToggleableState(isRegex),
                            onClick = { isRegex = !isRegex },
                        )
                        Text(text = stringResource(RStrings.block_tag_as_regex))
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(
                            text = stringResource(RStrings.cancel),
                            onClick = { showAddDialog = false },
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(16.dp))
                        TextButton(
                            text = stringResource(RStrings.confirm),
                            onClick = {
                                if (!canConfirm) return@TextButton
                                BlockingRepositoryV2.blockTag(normalizedTag, isRegex = isRegex)
                                showAddDialog = false
                            },
                            modifier = Modifier.weight(1f),
                            enabled = canConfirm,
                            colors = ButtonDefaults.textButtonColorsPrimary(),
                        )
                    }
                }
            },
        )
    }
}

@Composable
private fun <T> BlockTextScreen(
    title: String,
    items: List<T>,
    onRemove: (T) -> Unit,
    topBarActions: @Composable RowScope.() -> Unit = {},
    modifier: Modifier = Modifier,
    itemContent: @Composable RowScope.(T) -> Unit = {},
) {
    val navigationManager = currentNavigationManager()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = title,
                navigationIcon = {
                    IconButton(onClick = navigationManager::popBackStack) {
                        Icon(MiuixIcons.Back, contentDescription = null)
                    }
                },
                actions = topBarActions,
            )
        }
    ) { innerPadding ->
        if (items.isEmpty()) {
            Box(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(RStrings.no_blocked_items),
                    style = MiuixTheme.textStyles.body1,
                )
            }
            return@Scaffold
        }

        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        ) {
            item(key = "block_list_card") {
                Card {
                    items.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            itemContent(item)
                            IconButton(onClick = { onRemove(item) }) {
                                Icon(
                                    imageVector = Icons.Rounded.Delete,
                                    contentDescription = null
                                )
                            }
                        }
                        if (index != items.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}
