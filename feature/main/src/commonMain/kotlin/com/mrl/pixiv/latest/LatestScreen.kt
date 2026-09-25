package com.mrl.pixiv.latest

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Public
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.collection.CollectionViewModel
import com.mrl.pixiv.common.analytics.logEvent
import com.mrl.pixiv.common.compose.layout.currentPaneLayoutInfo
import com.mrl.pixiv.common.compose.layout.isWidthAtLeastMedium
import com.mrl.pixiv.common.compose.ui.BackToTopButton
import com.mrl.pixiv.common.compose.ui.ViewModeAction
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.data.AppViewMode
import com.mrl.pixiv.common.data.Restrict
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.SettingRepository.collectAsStateWithLifecycle
import com.mrl.pixiv.common.repository.requireUserInfoFlow
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.viewmodel.asState
import com.mrl.pixiv.common.viewmodel.state
import com.mrl.pixiv.follow.FollowingViewModel
import com.mrl.pixiv.strings.all
import com.mrl.pixiv.strings.collection
import com.mrl.pixiv.strings.latest_tab_following
import com.mrl.pixiv.strings.latest_tab_trend
import com.mrl.pixiv.strings.novel_new
import com.mrl.pixiv.strings.novel_watchlist
import com.mrl.pixiv.strings.word_private
import com.mrl.pixiv.strings.word_public
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun LatestScreen(
    modifier: Modifier = Modifier,
    viewModel: LatestViewModel = koinViewModel(),
) {
    val userInfo by requireUserInfoFlow.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val paneSizeClass = currentPaneLayoutInfo().sizeClass
    val refreshFlow = remember { MutableSharedFlow<LatestPage>() }
    val isWidthAtLeastMedium = paneSizeClass.isWidthAtLeastMedium
    val appViewMode by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle { appViewMode }
    val pages = remember(appViewMode) { LatestPage.pagesFor(appViewMode) }
    val pagerState = viewModel.pagerStateFor(appViewMode)
    val page = pages[pagerState.currentPage.coerceIn(pages.indices)]
    val uid = userInfo.user.id
    var showCollectionFilterDialog by rememberSaveable { mutableStateOf(false) }
    var selectedFollowingPage by rememberSaveable(uid) { mutableIntStateOf(0) }
    val trendingFilter by viewModel.trendingFilter.collectAsStateWithLifecycle()
    val scrollState = when (page) {
        LatestPage.Trend -> when (appViewMode) {
            AppViewMode.ILLUST -> viewModel.trendingLazyGirdState
            AppViewMode.NOVEL -> viewModel.trendingNovelLazyListState
        }

        LatestPage.Collection -> when (appViewMode) {
            AppViewMode.ILLUST -> viewModel.collectionLazyGirdState
            AppViewMode.NOVEL -> viewModel.collectionNovelLazyListState
        }

        LatestPage.Following -> when (appViewMode) {
            AppViewMode.ILLUST -> if (isWidthAtLeastMedium) {
                viewModel.followingLazyGirdState
            } else {
                viewModel.followingLazyListState
            }

            AppViewMode.NOVEL -> if (isWidthAtLeastMedium) {
                viewModel.followingLazyGirdState
            } else {
                viewModel.followingLazyListState
            }
        }

        LatestPage.NovelNew -> viewModel.newNovelLazyListState
        LatestPage.NovelWatchlist -> viewModel.watchlistNovelLazyListState
    }

    LaunchedEffect(pagerState.currentPage, pages) {
        logEvent("screen_view", buildMap {
            put("screen_name", "Latest")
            put("page_name", page.name)
        })
    }

    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            Column {
                TopAppBar(
                    title = stringResource(
                        when (page) {
                            LatestPage.Trend -> RStrings.latest_tab_trend
                            LatestPage.Collection -> RStrings.collection
                            LatestPage.Following -> RStrings.latest_tab_following
                            LatestPage.NovelNew -> RStrings.novel_new
                            LatestPage.NovelWatchlist -> RStrings.novel_watchlist
                        }
                    ),
                    scrollBehavior = scrollBehavior,
                    actions = {
                        when (page) {
                            LatestPage.Trend -> {
                                val restrictLabels = listOf(
                                    stringResource(RStrings.all),
                                    stringResource(RStrings.word_public),
                                    stringResource(RStrings.word_private),
                                )
                                val restrictValues = listOf(
                                    Restrict.ALL,
                                    Restrict.PUBLIC,
                                    Restrict.PRIVATE,
                                )
                                val selectedRestrictIndex = restrictValues
                                    .indexOf(trendingFilter)
                                    .coerceAtLeast(0)
                                val restrictEntry = remember(trendingFilter, restrictLabels) {
                                    DropdownEntry(
                                        restrictLabels.mapIndexed { index, label ->
                                            DropdownItem(
                                                text = label,
                                                selected = index == selectedRestrictIndex,
                                                onClick = {
                                                    restrictValues.getOrNull(index)?.let { restrict ->
                                                        viewModel.updateRestrict(restrict)
                                                        scope.launch {
                                                            refreshFlow.emit(LatestPage.Trend)
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                    )
                                }
                                OverlayIconDropdownMenu(entry = restrictEntry) {
                                    Icon(
                                        imageVector = MiuixIcons.Filter,
                                        contentDescription = restrictLabels[selectedRestrictIndex],
                                        tint = MiuixTheme.colorScheme.onBackground,
                                    )
                                }
                            }

                            LatestPage.Collection -> {
                                val collectionViewModel = koinViewModel<CollectionViewModel> {
                                    parametersOf(uid)
                                }
                                val collectionState = collectionViewModel.asState()
                                val restrictValues = listOf(Restrict.PUBLIC, Restrict.PRIVATE)
                                val restrictLabels = listOf(
                                    stringResource(RStrings.word_public),
                                    stringResource(RStrings.word_private),
                                )
                                val currentRestrict = if (appViewMode == AppViewMode.ILLUST) {
                                    collectionState.restrict
                                } else {
                                    collectionState.novelRestrict
                                }
                                val selectedRestrictIndex = restrictValues
                                    .indexOf(currentRestrict)
                                    .coerceAtLeast(0)
                                val restrictEntry = remember(currentRestrict, restrictLabels) {
                                    DropdownEntry(
                                        restrictLabels.mapIndexed { index, label ->
                                            DropdownItem(
                                                text = label,
                                                selected = index == selectedRestrictIndex,
                                                onClick = {
                                                    restrictValues.getOrNull(index)?.let { restrict ->
                                                        if (appViewMode == AppViewMode.ILLUST) {
                                                            collectionViewModel.updateFilterTag(
                                                                restrict,
                                                                collectionViewModel.state.filterTag,
                                                            )
                                                        } else {
                                                            collectionViewModel.updateNovelFilterTag(
                                                                restrict,
                                                                collectionViewModel.state.novelFilterTag,
                                                            )
                                                        }
                                                        scope.launch {
                                                            refreshFlow.emit(LatestPage.Collection)
                                                        }
                                                    }
                                                },
                                            )
                                        }
                                    )
                                }
                                OverlayIconDropdownMenu(entry = restrictEntry) {
                                    Icon(
                                        imageVector = Icons.Rounded.Public,
                                        contentDescription = restrictLabels[selectedRestrictIndex],
                                        tint = MiuixTheme.colorScheme.onBackground,
                                    )
                                }
                                IconButton(
                                    onClick = { showCollectionFilterDialog = true },
                                    backgroundColor = MiuixTheme.colorScheme.surfaceVariant,
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.FilterList,
                                        contentDescription = null,
                                    )
                                }
                            }

                            LatestPage.Following -> {
                                val followingViewModel = koinViewModel<FollowingViewModel> {
                                    parametersOf(uid)
                                }
                                if (followingViewModel.pages.size > 1) {
                                    val restrictLabels = listOf(
                                        stringResource(RStrings.word_public),
                                        stringResource(RStrings.word_private),
                                    )
                                    val pageIndex = selectedFollowingPage
                                        .coerceIn(followingViewModel.pages.indices)
                                    val restrictEntry = remember(pageIndex, restrictLabels) {
                                        DropdownEntry(
                                            restrictLabels.mapIndexed { index, label ->
                                                DropdownItem(
                                                    text = label,
                                                    selected = index == pageIndex,
                                                    onClick = {
                                                        selectedFollowingPage = index
                                                    },
                                                )
                                            }
                                        )
                                    }
                                    OverlayIconDropdownMenu(entry = restrictEntry) {
                                        Icon(
                                            imageVector = Icons.Rounded.Public,
                                            contentDescription = restrictLabels[pageIndex],
                                            tint = MiuixTheme.colorScheme.onBackground,
                                        )
                                    }
                                }
                            }

                            LatestPage.NovelNew, LatestPage.NovelWatchlist -> Unit
                        }
                        ViewModeAction(
                            currentMode = appViewMode,
                            onModeChange = viewModel::switchViewMode,
                        )
                    },
                )
                key(appViewMode) {
                    TabRow(
                        tabs = pages.map { page ->
                            stringResource(
                                when (page) {
                                    LatestPage.Trend -> RStrings.latest_tab_trend
                                    LatestPage.Collection -> RStrings.collection
                                    LatestPage.Following -> RStrings.latest_tab_following
                                    LatestPage.NovelNew -> RStrings.novel_new
                                    LatestPage.NovelWatchlist -> RStrings.novel_watchlist
                                }
                            )
                        },
                        selectedTabIndex = pagerState.currentPage,
                        onTabSelected = { index ->
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        minWidth = 64.dp,
                        maxWidth = 160.dp,
                    )
                }
            }
        },
        floatingActionButton = {
            Column {
                BackToTopButton(
                    visibility = scrollState.canScrollBackward,
                    modifier = Modifier,
                    onBackToTop = {
                        when (scrollState) {
                            is LazyListState -> scope.launch { scrollState.scrollToItem(0) }
                            is LazyGridState -> scope.launch { scrollState.scrollToItem(0) }
                            is LazyStaggeredGridState -> scope.launch { scrollState.scrollToItem(0) }
                        }
                    },
                    onRefresh = {
                        scope.launch {
                            refreshFlow.emit(page)
                        }
                    }
                )
            }
        },
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .pageScrollModifiers(scrollBehavior)
        ) { index ->
            val page = pages[index]
            when (page) {
                LatestPage.Trend -> {
                    TrendingPage(refreshFlow = refreshFlow)
                }

                LatestPage.Collection -> {
                    CollectionPage(
                        uid = userInfo.user.id,
                        refreshFlow = refreshFlow,
                        showFilterDialog = showCollectionFilterDialog,
                        onShowFilterDialogChange = { showCollectionFilterDialog = it },
                    )
                }

                LatestPage.Following -> {
                    FollowingPage(
                        uid = userInfo.user.id,
                        refreshFlow = refreshFlow,
                        selectedPage = selectedFollowingPage,
                    )
                }

                LatestPage.NovelNew -> {
                    NewNovelPage(refreshFlow = refreshFlow)
                }

                LatestPage.NovelWatchlist -> {
                    NovelWatchlistPage(refreshFlow = refreshFlow)
                }
            }
        }
    }
}
