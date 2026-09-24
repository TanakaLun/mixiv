package com.mrl.pixiv.follow

//import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.mrl.pixiv.common.analytics.logEvent
import com.mrl.pixiv.common.compose.IllustGridDefaults
import com.mrl.pixiv.common.compose.layout.currentPaneLayoutInfo
import com.mrl.pixiv.common.compose.layout.isWidthAtLeastMedium
import com.mrl.pixiv.common.compose.layout.isWidthCompact
import com.mrl.pixiv.common.compose.listener.KeyEventListener
import com.mrl.pixiv.common.compose.listener.keyboardScrollerController
import com.mrl.pixiv.common.compose.rememberThrottleClick
import com.mrl.pixiv.common.compose.ui.BackToTopButton
import com.mrl.pixiv.common.compose.ui.VerticalScrollbar
import com.mrl.pixiv.common.compose.ui.illust.SquareIllustItem
import com.mrl.pixiv.common.compose.ui.image.UserAvatar
import com.mrl.pixiv.common.data.Illust
import com.mrl.pixiv.common.data.user.UserPreview
import com.mrl.pixiv.common.kts.itemIndexKey
import com.mrl.pixiv.common.kts.spaceBy
import com.mrl.pixiv.common.repository.viewmodel.bookmark.BookmarkState
import com.mrl.pixiv.common.repository.viewmodel.bookmark.isBookmark
import com.mrl.pixiv.common.repository.viewmodel.follow.FollowState
import com.mrl.pixiv.common.repository.viewmodel.follow.isFollowing
import com.mrl.pixiv.common.router.NavigateToHorizontalPictureScreen
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.follow
import com.mrl.pixiv.strings.followed
import com.mrl.pixiv.strings.word_private
import com.mrl.pixiv.strings.word_public
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back

enum class FollowingPage {
    Public,
    Private,
}

@Composable
fun FollowingScreen(
    uid: Long,
    modifier: Modifier = Modifier,
    viewModel: FollowingViewModel = koinViewModel { parametersOf(uid) },
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val scope = rememberCoroutineScope()
    val pages = viewModel.pages
    val pagerState = viewModel.pagerState
    val paneSizeClass = currentPaneLayoutInfo().sizeClass
    val isWidthAtLeastMedium = paneSizeClass.isWidthAtLeastMedium
    val lazyListStates = viewModel.lazyListState
    val lazyGridStates = viewModel.lazyGridState
    val page = pages[pagerState.currentPage]
    val followingUsers = when (page) {
        FollowingPage.Public -> viewModel.publicFollowingPageSource.collectAsLazyPagingItems()
        FollowingPage.Private -> viewModel.privateFollowingPageSource.collectAsLazyPagingItems()
    }
    val controller = remember(isWidthAtLeastMedium, pagerState.currentPage) {
        if (isWidthAtLeastMedium) {
            val lazyGridState = lazyGridStates[pagerState.currentPage]
            keyboardScrollerController(lazyGridState) {
                lazyGridState.layoutInfo.viewportSize.height.toFloat()
            }
        } else {
            val lazyListState = lazyListStates[pagerState.currentPage]
            keyboardScrollerController(lazyListState) {
                lazyListState.layoutInfo.viewportSize.height.toFloat()
            }
        }
    }

    KeyEventListener(controller)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.followed),
                navigationIcon = {
                    IconButton(
                        onClick = rememberThrottleClick {
                            navigationManager.popBackStack()
                        },
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            val scrollState = if (isWidthAtLeastMedium) {
                lazyGridStates[pagerState.currentPage]
            } else {
                lazyListStates[pagerState.currentPage]
            }
            BackToTopButton(
                visibility = scrollState.canScrollBackward,
                modifier = Modifier,
                onBackToTop = {
                    when (scrollState) {
                        is LazyListState -> scope.launch { scrollState.scrollToItem(0) }
                        is LazyGridState -> scope.launch { scrollState.scrollToItem(0) }
                    }
                },
                onRefresh = {
                    followingUsers.refresh()
                }
            )
        },
        contentWindowInsets = WindowInsets.statusBars,
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
        ) {
            if (pages.size > 1) {
                TabRow(
                    tabs = pages.map { page ->
                        stringResource(if (page == FollowingPage.Public) RStrings.word_public else RStrings.word_private)
                    },
                    selectedTabIndex = pagerState.currentPage,
                    onTabSelected = { index ->
                        scope.launch {
                            if (pagerState.currentPage == index) return@launch
                            pagerState.animateScrollToPage(index)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth(if (paneSizeClass.isWidthCompact) 1f else 0.5f)
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    minWidth = 64.dp,
                    maxWidth = 160.dp,
                )
            }
            LaunchedEffect(pagerState.currentPage) {
                logEvent("screen_view", buildMap {
                    put("screen_name", "Following")
                    put("page_name", FollowingPage.entries[pagerState.currentPage].name)
                })
            }

            HorizontalPager(
                state = pagerState,
                modifier = modifier.weight(1f),
            ) { index ->
                FollowingScreenBody(
                    followingUsers = followingUsers,
                    navToPictureScreen = navigationManager::navigateToPictureScreen,
                    navToUserProfile = navigationManager::navigateToProfileDetailScreen,
                    lazyListState = lazyListStates[index],
                    lazyGridState = lazyGridStates[index],
                )
            }
        }
    }
}

@Composable
fun FollowingScreenBody(
    followingUsers: LazyPagingItems<UserPreview>,
    navToPictureScreen: NavigateToHorizontalPictureScreen,
    navToUserProfile: (Long) -> Unit,
    modifier: Modifier = Modifier,
    lazyListState: LazyListState = rememberLazyListState(),
    lazyGridState: LazyGridState = rememberLazyGridState(),
    showIllusts: Boolean = true,
) {
    val paneSizeClass = currentPaneLayoutInfo().sizeClass

    val isRefreshing = followingUsers.loadState.refresh is LoadState.Loading
    PullToRefresh(
        isRefreshing = isRefreshing,
        onRefresh = { followingUsers.refresh() },
        modifier = modifier,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            if (paneSizeClass.isWidthAtLeastMedium) {
                val layoutParams = IllustGridDefaults.userFollowingParameters()
                LazyVerticalGrid(
                    columns = layoutParams.gridCells,
                    state = lazyGridState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 10.dp,
                        end = 16.dp,
                        bottom = 20.dp
                    ),
                    horizontalArrangement = layoutParams.horizontalArrangement,
                    verticalArrangement = layoutParams.verticalArrangement,
                ) {
                    items(
                        followingUsers.itemCount,
                        key = followingUsers.itemIndexKey { index, user -> "${index}_${user.user.id}" }
                    ) {
                        val userPreview = followingUsers[it] ?: return@items
                        FollowingUserCard(
                            illusts = userPreview.illusts.toImmutableList(),
                            userName = userPreview.user.name,
                            userId = userPreview.user.id,
                            userAvatar = userPreview.user.profileImageUrls.medium,
                            isFollowed = userPreview.user.isFollowing,
                            navToPictureScreen = navToPictureScreen,
                            navToUserProfile = {
                                navToUserProfile(userPreview.user.id)
                            },
                            showIllusts = showIllusts
                        )
                    }
                }
                VerticalScrollbar(
                    state = lazyGridState,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    state = lazyListState,
                    contentPadding = PaddingValues(
                        start = 16.dp,
                        top = 10.dp,
                        end = 16.dp,
                        bottom = 20.dp
                    ),
                    verticalArrangement = 10f.spaceBy,
                ) {
                    items(
                        count = followingUsers.itemCount,
                        key = followingUsers.itemIndexKey { index, item -> "${index}_${item.user.id}" }
                    ) {
                        val userPreview = followingUsers[it] ?: return@items
                        FollowingUserCard(
                            illusts = userPreview.illusts.toImmutableList(),
                            userName = userPreview.user.name,
                            userId = userPreview.user.id,
                            userAvatar = userPreview.user.profileImageUrls.medium,
                            isFollowed = userPreview.user.isFollowing,
                            navToPictureScreen = navToPictureScreen,
                            navToUserProfile = {
                                navToUserProfile(userPreview.user.id)
                            },
                            showIllusts = showIllusts
                        )
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

private const val PREVIEW_SIZE = 3

@Composable
fun FollowingUserCard(
    illusts: ImmutableList<Illust>,
    userName: String,
    userId: Long,
    userAvatar: String,
    isFollowed: Boolean,
    navToPictureScreen: NavigateToHorizontalPictureScreen,
    navToUserProfile: () -> Unit,
    modifier: Modifier = Modifier,
    showIllusts: Boolean = true
) {
    Card(
        onClick = navToUserProfile,
        modifier = modifier.fillMaxWidth(),
    ) {
        if (showIllusts) {
            Row {
                val preview = illusts.take(PREVIEW_SIZE)
                preview.forEachIndexed { index, it ->
                    val isBookmarked = it.isBookmark
                    SquareIllustItem(
                        illust = it,
                        isBookmarked = isBookmarked,
                        onBookmarkClick = { restrict, tags, isEdit ->
                            if (isEdit || !isBookmarked) {
                                BookmarkState.bookmarkIllust(it.id, restrict, tags)
                            } else {
                                BookmarkState.deleteBookmarkIllust(it.id)
                            }
                        },
                        navToPictureScreen = { prefix, enableTransition ->
                            navToPictureScreen(illusts, index, prefix, enableTransition)
                        },
                        modifier = Modifier.weight(1f),
                        elevation = 0.dp,
                        shape = RectangleShape
                    )
                }
                if (preview.size < PREVIEW_SIZE) {
                    Spacer(modifier = Modifier.weight((PREVIEW_SIZE - preview.size).toFloat()))
                }
            }
        }
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = 8f.spaceBy,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            UserAvatar(
                url = userAvatar,
                onClick = navToUserProfile,
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = userName,
                modifier = Modifier.weight(1f)
            )
            if (isFollowed) {
                Button(
                    onClick = {
                        FollowState.unFollowUser(userId)
                    }
                ) {
                    Text(
                        text = stringResource(RStrings.followed),
                    )
                }
            } else {
                Button(
                    onClick = {
                        FollowState.followUser(userId)
                    },
                    colors = ButtonDefaults.buttonColorsPrimary(),
                ) {
                    Text(
                        text = stringResource(RStrings.follow),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun FollowingUserCardPreview() {
    FollowingUserCard(
        illusts = persistentListOf(),
        userName = "asdasd",
        userId = 0,
        userAvatar = "http://iph.href.lu/200x200",
        isFollowed = false,
        navToPictureScreen = { _, _, _, _ -> },
        navToUserProfile = { },
    )
}
