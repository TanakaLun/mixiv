package com.mrl.pixiv.search.preview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.compose.ui.ViewModeAction
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.SettingRepository.collectAsStateWithLifecycle
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.throttleClick
import com.mrl.pixiv.common.viewmodel.asState
import com.mrl.pixiv.search.preview.components.TrendingItem
import com.mrl.pixiv.strings.enter_keywords
import com.mrl.pixiv.strings.popular_tags
import com.mrl.pixiv.strings.search
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SearchPreviewScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchPreviewViewModel = koinViewModel(),
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val state = viewModel.asState()
    val lazyGridState = viewModel.lazyGridState
    val pullRefreshState = rememberPullToRefreshState()
    val scope = rememberCoroutineScope()
    val appViewMode by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle { appViewMode }
    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.search),
                scrollBehavior = scrollBehavior,
                actions = {
                    ViewModeAction(
                        currentMode = appViewMode,
                        onModeChange = { mode ->
                            viewModel.switchViewMode(mode)
                            scope.launch { lazyGridState.scrollToItem(0) }
                        },
                    )
                },
                bottomContent = {
                    InputField(
                        query = "",
                        onQueryChange = {},
                        onSearch = {},
                        expanded = false,
                        onExpandedChange = {},
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .padding(bottom = 8.dp)
                            .throttleClick {
                                navigationManager.navigateToSearchScreen()
                            },
                        label = stringResource(RStrings.enter_keywords),
                        enabled = false,
                    )
                },
            )
        },
    ) {
        PullToRefresh(
            isRefreshing = state.refreshing,
            onRefresh = { viewModel.dispatch(SearchPreviewAction.LoadTrendingTags) },
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .pageScrollModifiers(scrollBehavior),
            pullToRefreshState = pullRefreshState,
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
            LazyVerticalGrid(
                modifier = Modifier.fillMaxSize(),
                state = lazyGridState,
                columns = GridCells.Fixed(3),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item(
                    span = { GridItemSpan(3) },
                ) {
                    Text(
                        text = stringResource(RStrings.popular_tags),
                        style = MiuixTheme.textStyles.title2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                items(
                    items = state.trendingTags,
                    key = { it.tag }
                ) { tag ->
                    TrendingItem(
                        trendingTag = tag,
                        onSearch = {
                            navigationManager.navigateToSearchResultScreen(
                                searchWord = it,
                                searchMode = appViewMode
                            )
                            viewModel.dispatch(SearchPreviewAction.AddSearchHistory(it))
                        }
                    )
                }
                item(span = { GridItemSpan(3) }) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
            }
        }
    }
}
