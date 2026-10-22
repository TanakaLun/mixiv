package com.mrl.pixiv.ranking

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.mrl.pixiv.collection.components.FilterAction
import com.mrl.pixiv.common.analytics.logEvent
import com.mrl.pixiv.common.compose.RecommendGridDefaults
import com.mrl.pixiv.common.compose.layout.AdaptiveVerticalStaggeredGrid
import com.mrl.pixiv.common.compose.listener.KeyEventListener
import com.mrl.pixiv.common.compose.listener.keyboardScrollerController
import com.mrl.pixiv.common.compose.ui.BackToTopButton
import com.mrl.pixiv.common.compose.ui.illust.illustGrid
import com.mrl.pixiv.common.compose.ui.novel.NovelItem
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.compose.ui.rememberRefreshTexts
import com.mrl.pixiv.common.compose.ui.viewModeDropdownEntry
import com.mrl.pixiv.common.data.AppViewMode
import com.mrl.pixiv.common.kts.HSpacer
import com.mrl.pixiv.common.kts.itemIndexKey
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.SettingRepository.collectAsStateWithLifecycle
import com.mrl.pixiv.common.repository.requireUserPreferenceFlow
import com.mrl.pixiv.common.repository.viewmodel.bookmark.BookmarkState
import com.mrl.pixiv.common.router.NavigateToHorizontalPictureScreen
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.viewmodel.asState
import com.mrl.pixiv.components.date.MiuixDatePickerSheet
import com.mrl.pixiv.strings.r18
import com.mrl.pixiv.strings.ranking
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Switch
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Months

@Composable
fun RankingScreen(
    modifier: Modifier = Modifier,
    viewModel: RankingViewModel = koinViewModel(),
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val state = viewModel.asState()
    val appViewMode by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle { appViewMode }
    val availableModes = state.availableModes(appViewMode)

    var showDatePicker by rememberSaveable { mutableStateOf(false) }

    MiuixDatePickerSheet(
        show = showDatePicker,
        initialDate = when (appViewMode) {
            AppViewMode.ILLUST -> viewModel.getIllustRankingDate(state.currentMode)
            AppViewMode.NOVEL -> viewModel.getNovelRankingDate(state.currentMode)
        },
        onDismissRequest = { showDatePicker = false },
        onConfirm = { date ->
            viewModel.changeDate(date, appViewMode)
            showDatePicker = false
        },
    )

    val pagerState = key(availableModes) {
        rememberPagerState(
            initialPage = 0,
            pageCount = { availableModes.size }
        )
    }

    // Sync external mode selection (if any) with pager
    LaunchedEffect(state.currentMode, availableModes) {
        val index = availableModes.indexOf(state.currentMode)
        if (index >= 0 && pagerState.currentPage != index) {
            pagerState.scrollToPage(index)
        }
    }

    // Sync pager scroll with mode selection
    LaunchedEffect(pagerState, availableModes) {
        snapshotFlow { pagerState.currentPage }.collectLatest { page ->
            if (page in availableModes.indices) {
                val mode = availableModes[page]
                if (state.currentMode != mode) {
                    viewModel.selectMode(mode)
                }
                logEvent("screen_view", buildMap {
                    put("screen_name", "Ranking")
                    put("mode", mode.value)
                })
            }
        }
    }

    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.ranking),
                scrollBehavior = scrollBehavior,
                actions = {
                    val r18Enabled by requireUserPreferenceFlow.collectAsStateWithLifecycle { isR18Enabled }
                    LaunchedEffect(Unit) {
                        requireUserPreferenceFlow.map { it.isR18Enabled }.distinctUntilChanged()
                            .collect { r18Enabled ->
                                if (!r18Enabled && state.showR18) {
                                    viewModel.toggleR18()
                                }
                            }
                    }
                    if (r18Enabled) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(text = stringResource(RStrings.r18))
                            5.HSpacer
                            Switch(
                                checked = state.showR18,
                                onCheckedChange = { viewModel.toggleR18() }
                            )
                            8.HSpacer
                        }
                    }
                    IconButton(
                        onClick = { showDatePicker = true }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Months,
                            contentDescription = null
                        )
                    }
                    val modeEntry = DropdownEntry(
                        availableModes.map { mode ->
                            DropdownItem(
                                text = stringResource(mode.title),
                                selected = mode == state.currentMode,
                                onClick = { viewModel.selectMode(mode) },
                            )
                        }
                    )
                    FilterAction(
                        entries = listOf(
                            modeEntry,
                            viewModeDropdownEntry(
                                currentMode = appViewMode,
                                onModeChange = viewModel::switchViewMode,
                            ),
                        ),
                    )
                }
            )
        },
        floatingActionButton = {
            Column {
                val mode = availableModes.getOrNull(pagerState.currentPage)
                if (mode != null) {
                    val scrollToTop = suspend {
                        when (appViewMode) {
                            AppViewMode.ILLUST -> viewModel.getLazyStaggeredGridState(mode)
                                .scrollToItem(0)

                            AppViewMode.NOVEL -> viewModel.getLazyListState(mode).scrollToItem(0)
                        }
                    }
                    BackToTopButton(
                        visibility = when (appViewMode) {
                            AppViewMode.ILLUST -> viewModel.getLazyStaggeredGridState(mode).canScrollBackward
                            AppViewMode.NOVEL -> viewModel.getLazyListState(mode).canScrollBackward
                        },
                        modifier = Modifier,
                        onBackToTop = scrollToTop,
                        onRefresh = { viewModel.refresh(mode) }
                    )
                }
            }
        },
    ) { paddingValues ->
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .pageScrollModifiers(scrollBehavior)
        ) { page ->
            val mode = availableModes.getOrNull(page) ?: return@HorizontalPager

            when (appViewMode) {
                AppViewMode.ILLUST -> {
                    IllustMode(
                        mode = mode,
                        navigateToPictureScreen = navigationManager::navigateToPictureScreen,
                        viewModel = viewModel,
                    )
                }

                AppViewMode.NOVEL -> {
                    NovelMode(
                        mode = mode,
                        navigateToNovelDetailScreen = navigationManager::navigateToNovelDetailScreen,
                        navigateToNovelSeriesScreen = navigationManager::navigateToNovelSeriesScreen,
                        viewModel = viewModel,
                    )
                }
            }
        }
    }
}

@Composable
private fun IllustMode(
    mode: RankingMode,
    navigateToPictureScreen: NavigateToHorizontalPictureScreen,
    modifier: Modifier = Modifier,
    viewModel: RankingViewModel = koinViewModel(),
) {
    val lazyStaggeredGridState = viewModel.getLazyStaggeredGridState(mode)
    val rankingList = viewModel.getIllustRankingFlow(mode).collectAsLazyPagingItems()
    val pullRefreshState = rememberPullToRefreshState()
    val onRefresh = rankingList::refresh
    val isRefreshing = rankingList.loadState.refresh is LoadState.Loading
    val controller = remember {
        keyboardScrollerController(lazyStaggeredGridState) {
            lazyStaggeredGridState.layoutInfo.viewportSize.height.toFloat()
        }
    }

    KeyEventListener(controller)

    LaunchedEffect(mode) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is RankingSideEffect.Refresh -> {
                    if (sideEffect.mode == mode) {
                        rankingList.refresh()
                    }
                }
            }
        }
    }

    PullToRefresh(
        refreshTexts = rememberRefreshTexts(),
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
        pullToRefreshState = pullRefreshState,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        val layoutParams = RecommendGridDefaults.coverLayoutParameters()
        AdaptiveVerticalStaggeredGrid(
            state = lazyStaggeredGridState,
            contentPadding = PaddingValues(5.dp),
            columns = layoutParams.gridCells,
            verticalItemSpacing = layoutParams.verticalArrangement.spacing,
            horizontalArrangement = layoutParams.horizontalArrangement,
            modifier = Modifier.fillMaxSize()
        ) {
            illustGrid(
                illusts = rankingList,
                navToPictureScreen = navigateToPictureScreen
            )
        }
        }
    }
}

@Composable
private fun NovelMode(
    mode: RankingMode,
    navigateToNovelDetailScreen: (Long) -> Unit,
    navigateToNovelSeriesScreen: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RankingViewModel = koinViewModel(),
) {
    val lazyListState = viewModel.getLazyListState(mode)
    val rankingList = viewModel.getNovelRankingFlow(mode).collectAsLazyPagingItems()
    val pullRefreshState = rememberPullToRefreshState()
    val onRefresh = rankingList::refresh
    val isRefreshing = rankingList.loadState.refresh is LoadState.Loading
    val controller = remember {
        keyboardScrollerController(lazyListState) {
            lazyListState.layoutInfo.viewportSize.height.toFloat()
        }
    }

    KeyEventListener(controller)

    LaunchedEffect(mode) {
        viewModel.sideEffect.collect { sideEffect ->
            when (sideEffect) {
                is RankingSideEffect.Refresh -> {
                    if (sideEffect.mode == mode) {
                        rankingList.refresh()
                    }
                }
            }
        }
    }

    PullToRefresh(
        refreshTexts = rememberRefreshTexts(),
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
        pullToRefreshState = pullRefreshState,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = lazyListState,
        ) {
            items(
                count = rankingList.itemCount,
                key = rankingList.itemIndexKey { index, item -> "${index}_${item.id}" }
            ) { index ->
                rankingList[index]?.let { novel ->
                    NovelItem(
                        novel = novel,
                        onNovelClick = { novelId ->
                            navigateToNovelDetailScreen(novelId)
                        },
                        onSeriesClick = navigateToNovelSeriesScreen,
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
        }
    }
}
