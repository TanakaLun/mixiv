package com.mrl.pixiv.setting.block

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.compose.rememberThrottleClick
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.router.Destination
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.block_comments
import com.mrl.pixiv.strings.block_illust
import com.mrl.pixiv.strings.block_novel
import com.mrl.pixiv.strings.block_settings
import com.mrl.pixiv.strings.block_tags
import com.mrl.pixiv.strings.block_user
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference

@Composable
fun BlockSettingsScreen(
    modifier: Modifier = Modifier,
) {
    val navigationManager = currentNavigationManager()

    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = modifier.pageScrollModifiers(scrollBehavior),
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.block_settings),
                navigationIcon = {
                    IconButton(onClick = navigationManager::popBackStack) {
                        Icon(MiuixIcons.Back, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            item(key = "block_settings") {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    BlockEntry(
                        title = RStrings.block_illust,
                        onClick = { navigationManager.navigate(Destination.BlockIllust) }
                    )
                    BlockEntry(
                        title = RStrings.block_novel,
                        onClick = { navigationManager.navigate(Destination.BlockNovel) }
                    )
                    BlockEntry(
                        title = RStrings.block_user,
                        onClick = { navigationManager.navigate(Destination.BlockUser) }
                    )
                    BlockEntry(
                        title = RStrings.block_tags,
                        onClick = { navigationManager.navigate(Destination.BlockTag) }
                    )
                    BlockEntry(
                        title = RStrings.block_comments,
                        onClick = { navigationManager.navigate(Destination.BlockComments) }
                    )
                }
            }
        }
    }
}

@Composable
private fun BlockEntry(
    title: StringResource,
    onClick: () -> Unit,
) {
    ArrowPreference(
        title = stringResource(title),
        onClick = rememberThrottleClick(onClick = onClick),
    )
}
