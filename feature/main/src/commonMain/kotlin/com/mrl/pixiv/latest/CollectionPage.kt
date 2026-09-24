package com.mrl.pixiv.latest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.mrl.pixiv.collection.CollectionAction
import com.mrl.pixiv.collection.CollectionViewModel
import com.mrl.pixiv.collection.components.FilterDialog
import com.mrl.pixiv.common.compose.RecommendGridDefaults
import com.mrl.pixiv.common.compose.layout.AdaptiveVerticalStaggeredGrid
import com.mrl.pixiv.common.compose.listener.KeyEventListener
import com.mrl.pixiv.common.compose.listener.keyboardScrollerController
import com.mrl.pixiv.common.compose.ui.VerticalScrollbar
import com.mrl.pixiv.common.compose.ui.illust.RectangleIllustItem
import com.mrl.pixiv.common.compose.ui.novel.NovelItem
import com.mrl.pixiv.common.data.AppViewMode
import com.mrl.pixiv.common.data.Restrict
import com.mrl.pixiv.common.kts.itemIndexKey
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.SettingRepository.collectAsStateWithLifecycle
import com.mrl.pixiv.common.repository.viewmodel.bookmark.BookmarkState
import com.mrl.pixiv.common.repository.viewmodel.bookmark.isBookmark
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.viewmodel.asState
import com.mrl.pixiv.strings.word_private
import com.mrl.pixiv.strings.word_public
import kotlinx.coroutines.flow.SharedFlow
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.theme.MiuixTheme

private const val KEY_TOP_SPACE = "top_space"

@Composable
fun CollectionPage(
    uid: Long,
    refreshFlow: SharedFlow<LatestPage>,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = koinViewModel { parametersOf(uid) },
    latestViewModel: LatestViewModel = koinViewModel(),
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val appViewMode by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle { appViewMode }

    when (appViewMode) {
        AppViewMode.ILLUST -> {
            CollectionIllustPage(
                uid = uid,
                refreshFlow = refreshFlow,
                modifier = modifier,
                viewModel = viewModel,
                latestViewModel = latestViewModel,
                navigationManager = navigationManager
            )
        }

        AppViewMode.NOVEL -> {
            CollectionNovelPage(
                uid = uid,
                refreshFlow = refreshFlow,
                modifier = modifier,
                viewModel = viewModel,
                latestViewModel = latestViewModel,
                navigationManager = navigationManager
            )
        }
    }
}

@Composable
private fun CollectionIllustPage(
    uid: Long,
    refreshFlow: SharedFlow<LatestPage>,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = koinViewModel { parametersOf(uid) },
    latestViewModel: LatestViewModel = koinViewModel(),
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val userBookmarksIllusts = viewModel.userBookmarksIllusts.collectAsLazyPagingItems()
    val pullRefreshState = rememberPullToRefreshState()
    val lazyGridState = latestViewModel.collectionLazyGirdState
    val state = viewModel.asState()
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    val layoutParams = RecommendGridDefaults.coverLayoutParameters()
    val isRefreshing = userBookmarksIllusts.loadState.refresh is LoadState.Loading
    val controller = remember {
        keyboardScrollerController(lazyGridState) {
            lazyGridState.layoutInfo.viewportSize.height.toFloat()
        }
    }

    KeyEventListener(controller)

    LaunchedEffect(Unit) {
        refreshFlow.collect {
            userBookmarksIllusts.refresh()
        }
    }

    PullToRefresh(
        isRefreshing = isRefreshing,
        onRefresh = { userBookmarksIllusts.refresh() },
        modifier = modifier.fillMaxSize(),
        pullToRefreshState = pullRefreshState,
    ) {
        Box {
            AdaptiveVerticalStaggeredGrid(
                columns = layoutParams.gridCells,
                modifier = Modifier.fillMaxSize(),
                state = lazyGridState,
                contentPadding = PaddingValues(horizontal = 5.dp, vertical = 10.dp),
                verticalItemSpacing = layoutParams.verticalArrangement.spacing,
                horizontalArrangement = layoutParams.horizontalArrangement,
            ) {
                item(
                    key = KEY_TOP_SPACE,
                    span = StaggeredGridItemSpan.FullLine
                ) {
                    Spacer(modifier = Modifier.height(40.dp))
                }
                items(
                    count = userBookmarksIllusts.itemCount,
                    key = userBookmarksIllusts.itemIndexKey { index, item -> "${index}_${item.id}" }
                ) { index ->
                    val illust = userBookmarksIllusts[index] ?: return@items
                    val isBookmarked = illust.isBookmark
                    RectangleIllustItem(
                        illust = illust,
                        isBookmarked = isBookmarked,
                        navToPictureScreen = { prefix, enableTransition ->
                            navigationManager.navigateToPictureScreen(
                                userBookmarksIllusts.itemSnapshotList.items,
                                index,
                                prefix,
                                enableTransition
                            )
                        },
                        onBookmarkClick = { restrict, tags, isEdit ->
                            if (isEdit || !isBookmarked) {
                                BookmarkState.bookmarkIllust(illust.id, restrict, tags)
                            } else {
                                BookmarkState.deleteBookmarkIllust(illust.id)
                            }
                        }
                    )
                }
            }
            VerticalScrollbar(
                state = lazyGridState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
            val options = listOf(
                RStrings.word_public to Restrict.PUBLIC,
                RStrings.word_private to Restrict.PRIVATE,
            )
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TabRow(
                    tabs = options.map { stringResource(it.first) },
                    selectedTabIndex = options.indexOfFirst { it.second == state.restrict }
                        .coerceAtLeast(0),
                    onTabSelected = { index ->
                        val restrict = options.getOrNull(index)?.second ?: return@TabRow
                        viewModel.updateFilterTag(restrict, state.filterTag)
                        userBookmarksIllusts.refresh()
                    },
                    modifier = Modifier.weight(1f),
                    listState = null,
                )
                IconButton(
                    onClick = { showFilterDialog = true },
                    backgroundColor = MiuixTheme.colorScheme.surfaceVariant,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.FilterList,
                        contentDescription = null
                    )
                }
            }
        }
    }
    if (showFilterDialog) {
        FilterDialog(
            onDismissRequest = { showFilterDialog = false },
            userBookmarkTags = state.userBookmarkTagsIllust,
            privateBookmarkTags = state.privateBookmarkTagsIllust,
            restrict = state.restrict,
            filterTag = state.filterTag,
            onLoadUserBookmarksTags = {
                viewModel.dispatch(CollectionAction.LoadUserBookmarksTagsIllust(it))
            },
            onSelected = { restrict, tag ->
                viewModel.updateFilterTag(restrict, tag)
                userBookmarksIllusts.refresh()
            }
        )
    }
}

@Composable
private fun CollectionNovelPage(
    uid: Long,
    refreshFlow: SharedFlow<LatestPage>,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = koinViewModel { parametersOf(uid) },
    latestViewModel: LatestViewModel = koinViewModel(),
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val userBookmarksNovels = viewModel.userBookmarksNovels.collectAsLazyPagingItems()
    val pullRefreshState = rememberPullToRefreshState()
    val lazyListState = latestViewModel.collectionNovelLazyListState
    val state = viewModel.asState()
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    val isRefreshing = userBookmarksNovels.loadState.refresh is LoadState.Loading
    val controller = remember {
        keyboardScrollerController(lazyListState) {
            lazyListState.layoutInfo.viewportSize.height.toFloat()
        }
    }

    KeyEventListener(controller)

    LaunchedEffect(Unit) {
        refreshFlow.collect {
            userBookmarksNovels.refresh()
        }
    }

    PullToRefresh(
        isRefreshing = isRefreshing,
        onRefresh = { userBookmarksNovels.refresh() },
        modifier = modifier.fillMaxSize(),
        pullToRefreshState = pullRefreshState,
    ) {
        Box {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = lazyListState,
                contentPadding = PaddingValues(vertical = 50.dp),
            ) {
                items(
                    count = userBookmarksNovels.itemCount,
                    key = userBookmarksNovels.itemIndexKey { index, item -> "${index}_${item.id}" }
                ) { index ->
                    userBookmarksNovels[index]?.let { novel ->
                        NovelItem(
                            novel = novel,
                            onNovelClick = { novelId ->
                                navigationManager.navigateToNovelDetailScreen(novelId)
                            },
                            onSeriesClick = navigationManager::navigateToNovelSeriesScreen,
                            onBookmarkClick = { isBookmarked, restrict, tags ->
                                if (isBookmarked) {
                                    BookmarkState.deleteBookmarkNovel(novel.id)
                                } else {
                                    BookmarkState.bookmarkNovel(novel.id, restrict, tags)
                                }
                            }
                        )
                    }
                }
            }
            VerticalScrollbar(
                state = lazyListState,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
            val options = listOf(
                RStrings.word_public to Restrict.PUBLIC,
                RStrings.word_private to Restrict.PRIVATE,
            )
            TabRow(
                tabs = options.map { stringResource(it.first) },
                selectedTabIndex = options.indexOfFirst { it.second == state.novelRestrict }
                    .coerceAtLeast(0),
                onTabSelected = { index ->
                    val restrict = options.getOrNull(index)?.second ?: return@TabRow
                    viewModel.updateNovelFilterTag(restrict, state.novelFilterTag)
                    userBookmarksNovels.refresh()
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 48.dp),
                listState = null,
            )
        }
    }

    if (showFilterDialog) {
        FilterDialog(
            onDismissRequest = { showFilterDialog = false },
            userBookmarkTags = state.userBookmarkTagsNovel,
            privateBookmarkTags = state.privateBookmarkTagsNovel,
            restrict = state.novelRestrict,
            filterTag = state.novelFilterTag,
            onLoadUserBookmarksTags = {
                viewModel.dispatch(CollectionAction.LoadUserBookmarksTagsNovel(it))
            },
            onSelected = { restrict, tag ->
                viewModel.updateNovelFilterTag(restrict, tag)
                userBookmarksNovels.refresh()
            }
        )
    }
}
