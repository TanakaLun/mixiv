package com.mrl.pixiv.collection

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.mrl.pixiv.collection.components.FilterDialog
import com.mrl.pixiv.common.compose.IllustGridDefaults
import com.mrl.pixiv.common.compose.layout.currentPaneLayoutInfo
import com.mrl.pixiv.common.compose.layout.isWidthAtLeastMedium
import com.mrl.pixiv.common.compose.listener.KeyEventListener
import com.mrl.pixiv.common.compose.listener.keyboardScrollerController
import com.mrl.pixiv.common.compose.ui.BackToTopButton
import com.mrl.pixiv.common.compose.ui.VerticalScrollbar
import com.mrl.pixiv.common.compose.ui.ViewModeToggleButton
import com.mrl.pixiv.common.compose.ui.illust.illustGrid
import com.mrl.pixiv.common.compose.ui.novel.NovelItem
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.data.AppViewMode
import com.mrl.pixiv.common.kts.VSpacer
import com.mrl.pixiv.common.kts.itemIndexKey
import com.mrl.pixiv.common.repository.isSelf
import com.mrl.pixiv.common.repository.SettingRepository
import kotlinx.coroutines.flow.drop
import com.mrl.pixiv.common.repository.viewmodel.bookmark.BookmarkState
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.viewmodel.asState
import com.mrl.pixiv.strings.collection
import com.mrl.pixiv.strings.illusts
import com.mrl.pixiv.strings.novels
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

@Composable
fun CollectionScreen(
    uid: Long,
    isNovel: Boolean,
    modifier: Modifier = Modifier,
    viewModel: CollectionViewModel = koinViewModel { parametersOf(uid) },
    navigationManager: NavigationManager = currentNavigationManager()
) {
    val state = viewModel.asState()
    val userBookmarksIllusts = viewModel.userBookmarksIllusts.collectAsLazyPagingItems()
    val userBookmarksNovels = viewModel.userBookmarksNovels.collectAsLazyPagingItems()
    val dispatch = viewModel::dispatch
    var showFilterDialog by rememberSaveable { mutableStateOf(false) }
    val lazyGridState = rememberLazyGridState()
    val lazyListState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val pagerState = rememberCollectionPagerState(if (isNovel) 1 else 0, navigationManager)
    LaunchedEffect(pagerState, uid) {
        if (uid.isSelf) {
            snapshotFlow { pagerState.settledPage }.drop(1).collect { page ->
                SettingRepository.updateSettings {
                    copy(collectionViewMode = if (page == 1) AppViewMode.NOVEL else AppViewMode.ILLUST)
                }
            }
        }
    }
    val isIllustPage = pagerState.currentPage == 0
    val useViewModeFab = currentPaneLayoutInfo().sizeClass.isWidthAtLeastMedium

    val illustController = remember {
        keyboardScrollerController(lazyGridState) {
            lazyGridState.layoutInfo.viewportSize.height.toFloat()
        }
    }
    val novelController = remember {
        keyboardScrollerController(lazyListState) {
            lazyListState.layoutInfo.viewportSize.height.toFloat()
        }
    }
    val activeController = if (isIllustPage) illustController else novelController
    KeyEventListener(activeController)

    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                CollectionTopAppBar(
                    scrollBehavior = scrollBehavior,
                    uid = uid,
                    showFilterDialog = { showFilterDialog = true },
                    onBack = { navigationManager.popBackStack() }
                )
                if (!useViewModeFab) {
                    TabRow(
                        tabs = listOf(
                            stringResource(RStrings.illusts),
                            stringResource(RStrings.novels),
                        ),
                        selectedTabIndex = pagerState.currentPage,
                        onTabSelected = { index ->
                            scope.launch { pagerState.animateScrollToPage(index) }
                        },
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        minWidth = 64.dp,
                        maxWidth = 160.dp,
                    )
                }
            }
        },
        floatingActionButton = {
            val canScrollBackward = if (isIllustPage)
                lazyGridState.canScrollBackward
            else
                lazyListState.canScrollBackward
            Column {
                BackToTopButton(
                    visibility = canScrollBackward,
                    modifier = Modifier,
                    onBackToTop = {
                        scope.launch {
                            if (isIllustPage) lazyGridState.scrollToItem(0)
                            else lazyListState.scrollToItem(0)
                        }
                    },
                    onRefresh = {
                        if (isIllustPage) userBookmarksIllusts.refresh()
                        else userBookmarksNovels.refresh()
                    }
                )
                if (useViewModeFab) {
                    8.VSpacer
                    ViewModeToggleButton(
                        currentMode = if (isIllustPage) AppViewMode.ILLUST else AppViewMode.NOVEL,
                        onModeChange = { mode ->
                            scope.launch {
                                pagerState.scrollToPage(if (mode == AppViewMode.ILLUST) 0 else 1)
                            }
                        }
                    )
                }
            }
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(paddingValues)
                .pageScrollModifiers(scrollBehavior),
        ) { page ->
            when (page) {
                0 -> {
                    val layoutParams = IllustGridDefaults.relatedLayoutParameters()
                    val isRefreshing = userBookmarksIllusts.loadState.refresh is LoadState.Loading
                    PullToRefresh(
                        isRefreshing = isRefreshing,
                        onRefresh = { userBookmarksIllusts.refresh() },
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyVerticalGrid(
                                state = lazyGridState,
                                modifier = Modifier.fillMaxSize(),
                                columns = layoutParams.gridCells,
                                verticalArrangement = layoutParams.verticalArrangement,
                                horizontalArrangement = layoutParams.horizontalArrangement,
                                contentPadding = PaddingValues(
                                    start = 8.dp,
                                    top = 8.dp,
                                    end = 8.dp,
                                    bottom = WindowInsets.navigationBars.asPaddingValues()
                                        .calculateBottomPadding()
                                ),
                            ) {
                                illustGrid(
                                    illusts = userBookmarksIllusts,
                                    navToPictureScreen = navigationManager::navigateToPictureScreen,
                                )
                            }
                            VerticalScrollbar(
                                state = lazyGridState,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                }

                1 -> {
                    val isNovelRefreshing =
                        userBookmarksNovels.loadState.refresh is LoadState.Loading
                    PullToRefresh(
                        isRefreshing = isNovelRefreshing,
                        onRefresh = { userBookmarksNovels.refresh() },
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                state = lazyListState,
                                contentPadding = PaddingValues(
                                    top = 8.dp,
                                    bottom = WindowInsets.navigationBars.asPaddingValues()
                                        .calculateBottomPadding()
                                ),
                            ) {
                                items(
                                    count = userBookmarksNovels.itemCount,
                                    key = userBookmarksNovels.itemIndexKey { index, item ->
                                        "${index}_${item.id}"
                                    }
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
                                                    BookmarkState.bookmarkNovel(
                                                        novel.id,
                                                        restrict,
                                                        tags
                                                    )
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
                        }
                    }
                }
            }
        }

        if (showFilterDialog) {
            if (isIllustPage) {
                FilterDialog(
                    onDismissRequest = { showFilterDialog = false },
                    userBookmarkTags = state.userBookmarkTagsIllust,
                    privateBookmarkTags = state.privateBookmarkTagsIllust,
                    restrict = state.restrict,
                    filterTag = state.filterTag,
                    onLoadUserBookmarksTags = {
                        dispatch(CollectionAction.LoadUserBookmarksTagsIllust(it))
                    },
                    onSelected = { restrict, tag ->
                        viewModel.updateFilterTag(restrict, tag)
                        userBookmarksIllusts.refresh()
                    }
                )
            } else {
                FilterDialog(
                    onDismissRequest = { showFilterDialog = false },
                    userBookmarkTags = state.userBookmarkTagsNovel,
                    privateBookmarkTags = state.privateBookmarkTagsNovel,
                    restrict = state.novelRestrict,
                    filterTag = state.novelFilterTag,
                    onLoadUserBookmarksTags = {
                        dispatch(CollectionAction.LoadUserBookmarksTagsNovel(it))
                    },
                    onSelected = { restrict, tag ->
                        viewModel.updateNovelFilterTag(restrict, tag)
                        userBookmarksNovels.refresh()
                    }
                )
            }
        }
    }
}

@Composable
private fun CollectionTopAppBar(
    scrollBehavior: ScrollBehavior,
    uid: Long,
    showFilterDialog: () -> Unit = {},
    onBack: () -> Unit = {},
) {
    TopAppBar(
        modifier = Modifier.shadow(4.dp),
        title = stringResource(RStrings.collection),
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(
                onClick = onBack,
            ) {
                Icon(MiuixIcons.Back, contentDescription = null)
            }
        },
        actions = {
            if (uid.isSelf) {
                IconButton(
                    onClick = showFilterDialog,
                ) {
                    Icon(Icons.Rounded.FilterList, contentDescription = null)
                }
            }
        }
    )
}
