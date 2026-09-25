package com.mrl.pixiv.setting.download

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.datasource.local.entity.DownloadEntity
import com.mrl.pixiv.common.datasource.local.entity.DownloadStatus
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.throttleClick
import com.mrl.pixiv.common.viewmodel.asState
import com.mrl.pixiv.strings.delete
import com.mrl.pixiv.strings.download_manager
import com.mrl.pixiv.strings.retry
import com.mrl.pixiv.strings.status_all
import com.mrl.pixiv.strings.status_completed
import com.mrl.pixiv.strings.status_failed
import com.mrl.pixiv.strings.status_running
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DownloadScreen(
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = currentNavigationManager(),
    viewModel: DownloadViewModel = koinViewModel(),
) {
    val state = viewModel.asState()
    val downloads by viewModel.currentDownloads.collectAsStateWithLifecycle()
    val tabs = remember {
        listOf(
            DownloadState.FILTER_ALL to RStrings.status_all,
            DownloadStatus.SUCCESS.value to RStrings.status_completed,
            DownloadStatus.FAILED.value to RStrings.status_failed,
            DownloadStatus.RUNNING.value to RStrings.status_running,
        )
    }

    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.download_manager),
                navigationIcon = {
                    IconButton(onClick = navigationManager::popBackStack) {
                        Icon(MiuixIcons.Back, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .pageScrollModifiers(scrollBehavior)
        ) {
            val selectedIndex =
                tabs.indexOfFirst { it.first == state.filterStatus }.coerceAtLeast(0)
            TabRow(
                tabs = tabs.map { stringResource(it.second) },
                selectedTabIndex = selectedIndex,
                onTabSelected = { index ->
                    val (status, _) = tabs[index]
                    viewModel.changeFilterStatus(status)
                },
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                minWidth = 64.dp,
                maxWidth = 160.dp,
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(items = downloads, key = { "${it.illustId}_${it.index}" }) { item ->
                    DownloadItem(
                        item = item,
                        onRetry = { viewModel.retryDownload(item) },
                        onDelete = { viewModel.deleteDownload(item) },
                        modifier = Modifier
                            .animateItem()
                            .throttleClick {
                                navigationManager.navigateToSinglePictureScreen(item.illustId)
                            }
                    )
                }
            }
        }
    }
}

@Composable
fun DownloadItem(
    item: DownloadEntity,
    onRetry: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier.size(100.dp),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
            ) {
                Text(
                    text = item.title,
                    style = MiuixTheme.textStyles.subtitle,
                    maxLines = 1
                )
                Text(
                    text = item.userName,
                    style = MiuixTheme.textStyles.body1,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(4.dp))
                if (item.status == DownloadStatus.RUNNING.value ||
                    item.status == DownloadStatus.PENDING.value
                ) {
                    LinearProgressIndicator(
                        progress = item.progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val (icon, tint) = when (item.status) {
                            DownloadStatus.SUCCESS.value -> Icons.Rounded.CheckCircle to MiuixTheme.colorScheme.primary
                            DownloadStatus.FAILED.value -> Icons.Rounded.Error to MiuixTheme.colorScheme.error
                            else -> Icons.Rounded.CheckCircle to MiuixTheme.colorScheme.onSurface
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = stringResource(
                                when (item.status) {
                                    DownloadStatus.SUCCESS.value -> RStrings.status_completed
                                    DownloadStatus.FAILED.value -> RStrings.status_failed
                                    else -> RStrings.status_running
                                }
                            ),
                            style = MiuixTheme.textStyles.footnote2,
                            color = tint
                        )
                    }
                }
            }
            Column {
                if (item.status == DownloadStatus.FAILED.value) {
                    IconButton(onClick = onRetry) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = stringResource(RStrings.retry)
                        )
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Rounded.Delete,
                        contentDescription = stringResource(RStrings.delete)
                    )
                }
            }
        }
    }
}
