package com.mrl.pixiv.novel.series

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import com.mrl.pixiv.common.compose.ui.VerticalScrollbar
import com.mrl.pixiv.common.compose.ui.image.UserAvatar
import com.mrl.pixiv.common.compose.ui.novel.NovelItem
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.kts.HSpacer
import com.mrl.pixiv.common.repository.viewmodel.bookmark.BookmarkState
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.back
import com.mrl.pixiv.strings.load_failed
import com.mrl.pixiv.strings.novel_series_chapter_count
import com.mrl.pixiv.strings.novel_series_continue_reading
import com.mrl.pixiv.strings.novel_series_last_read
import com.mrl.pixiv.strings.novel_watchlist_add
import com.mrl.pixiv.strings.novel_watchlist_added
import com.mrl.pixiv.strings.retry
import com.mrl.pixiv.strings.series
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun NovelSeriesScreen(
    seriesId: Long,
    modifier: Modifier = Modifier,
    viewModel: NovelSeriesViewModel = koinViewModel { parametersOf(seriesId) },
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val novels = viewModel.novels.collectAsLazyPagingItems()
    val listState = rememberLazyListState()
    val isRefreshing = novels.loadState.refresh is LoadState.Loading

    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        modifier = modifier.fillMaxSize().pageScrollModifiers(scrollBehavior),
        topBar = {
            TopAppBar(
                title = state.detail?.title ?: stringResource(RStrings.series),
                scrollBehavior = scrollBehavior,
                navigationIcon = {
                    IconButton(
                        onClick = navigationManager::popBackStack,
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = stringResource(RStrings.back),
                        )
                    }
                },
            )
        },
    ) { paddingValues ->
        PullToRefresh(
            isRefreshing = isRefreshing,
            onRefresh = novels::refresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = listState,
                    contentPadding = PaddingValues(bottom = 24.dp),
                ) {
                    state.lastRead?.let { progress ->
                        item(key = "continue_reading") {
                            Button(
                                onClick = { navigationManager.navigateToNovelDetailScreen(progress.novelId) },
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                            ) {
                                Text(stringResource(
                                    RStrings.novel_series_continue_reading,
                                    progress.title,
                                    (progress.fraction * 100).toInt(),
                                ))
                            }
                        }
                    }
                    state.detail?.let { detail ->
                        item(key = "series_header") {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                            ) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp),
                                ) {
                                    Text(
                                        text = detail.title,
                                        style = MiuixTheme.textStyles.title3,
                                    )
                                    if (detail.caption.isNotBlank()) {
                                        Text(
                                            text = detail.caption,
                                            style = MiuixTheme.textStyles.body1,
                                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                                        )
                                    }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        UserAvatar(
                                            url = detail.user.profileImageUrls.medium,
                                            modifier = Modifier.size(40.dp),
                                            onClick = {
                                                navigationManager.navigateToProfileDetailScreen(
                                                    detail.user.id,
                                                )
                                            },
                                        )
                                        8.HSpacer
                                        TextButton(
                                            text = detail.user.name,
                                            onClick = {
                                                navigationManager.navigateToProfileDetailScreen(
                                                    detail.user.id,
                                                )
                                            },
                                            modifier = Modifier.weight(1f),
                                        )
                                        Text(
                                            text = stringResource(
                                                RStrings.novel_series_chapter_count,
                                                detail.contentCount,
                                            ),
                                            style = MiuixTheme.textStyles.footnote1,
                                        )
                                    }
                                    Button(
                                        onClick = viewModel::toggleWatchlist,
                                        enabled = !state.isUpdatingWatchlist,
                                        modifier = Modifier.fillMaxWidth(),
                                    ) {
                                        if (state.isUpdatingWatchlist) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                            )
                                        } else {
                                            Text(
                                                text = stringResource(
                                                    if (detail.watchlistAdded) {
                                                        RStrings.novel_watchlist_added
                                                    } else {
                                                        RStrings.novel_watchlist_add
                                                    },
                                                ),
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (
                        novels.itemCount == 0 &&
                        novels.loadState.refresh is LoadState.Loading
                    ) {
                        item(key = "loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(48.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    val refreshError = novels.loadState.refresh as? LoadState.Error
                    if (novels.itemCount == 0 && refreshError != null) {
                        item(key = "refresh_error") {
                            SeriesLoadError(
                                message = refreshError.error.message.orEmpty(),
                                onRetry = novels::retry,
                            )
                        }
                    }

                    items(
                        count = novels.itemCount,
                        key = novels.itemKey { it.id },
                    ) { index ->
                        val novel = novels[index] ?: return@items
                        if (state.lastRead?.novelId == novel.id) {
                            Text(
                                stringResource(RStrings.novel_series_last_read),
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MiuixTheme.colorScheme.primary,
                                style = MiuixTheme.textStyles.footnote1,
                            )
                        }
                        NovelItem(
                            novel = novel,
                            onNovelClick = navigationManager::navigateToNovelDetailScreen,
                            onSeriesClick = navigationManager::navigateToNovelSeriesScreen,
                            onBookmarkClick = { isBookmarked, restrict, tags ->
                                if (isBookmarked) {
                                    BookmarkState.deleteBookmarkNovel(novel.id)
                                } else {
                                    BookmarkState.bookmarkNovel(novel.id, restrict, tags)
                                }
                            },
                        )
                    }

                    when (val appendState = novels.loadState.append) {
                        is LoadState.Loading -> item(key = "append_loading") {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator()
                            }
                        }

                        is LoadState.Error -> item(key = "append_error") {
                            SeriesLoadError(
                                message = appendState.error.message.orEmpty(),
                                onRetry = novels::retry,
                            )
                        }

                        else -> Unit
                    }
                }
                VerticalScrollbar(
                    state = listState,
                    modifier = Modifier.align(Alignment.CenterEnd),
                )
            }
        }
    }
}

@Composable
private fun SeriesLoadError(
    message: String,
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = stringResource(RStrings.load_failed, message),
            textAlign = TextAlign.Center,
        )
        Button(onClick = onRetry) {
            Text(text = stringResource(RStrings.retry))
        }
    }
}
