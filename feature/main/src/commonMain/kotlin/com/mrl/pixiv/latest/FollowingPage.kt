package com.mrl.pixiv.latest

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.compose.collectAsLazyPagingItems
import com.mrl.pixiv.common.compose.layout.currentPaneLayoutInfo
import com.mrl.pixiv.common.compose.layout.isWidthAtLeastMedium
import com.mrl.pixiv.common.compose.listener.KeyEventListener
import com.mrl.pixiv.common.compose.listener.keyboardScrollerController
import com.mrl.pixiv.common.data.AppViewMode
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.SettingRepository.collectAsStateWithLifecycle
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.follow.FollowingScreenBody
import com.mrl.pixiv.follow.FollowingViewModel
import kotlinx.coroutines.flow.SharedFlow
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun FollowingPage(
    uid: Long,
    refreshFlow: SharedFlow<LatestPage>,
    modifier: Modifier = Modifier,
    selectedPage: Int = 0,
    viewModel: FollowingViewModel = koinViewModel { parametersOf(uid) },
) {
    val navigationManager = currentNavigationManager()
    val paneSizeClass = currentPaneLayoutInfo().sizeClass
    val isWidthAtLeastMedium = paneSizeClass.isWidthAtLeastMedium
    val appViewMode by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle { appViewMode }
    val pageIndex = selectedPage.coerceIn(viewModel.pages.indices)
    val followingUsers = (if (pageIndex == 0) viewModel.publicFollowingPageSource
        else viewModel.privateFollowingPageSource).collectAsLazyPagingItems()
    val listState = viewModel.lazyListState[pageIndex]
    val gridState = viewModel.lazyGridState[pageIndex]
    val controller = remember(isWidthAtLeastMedium, pageIndex) {
        if (isWidthAtLeastMedium) {
            keyboardScrollerController(gridState) {
                gridState.layoutInfo.viewportSize.height.toFloat()
            }
        } else {
            keyboardScrollerController(listState) {
                listState.layoutInfo.viewportSize.height.toFloat()
            }
        }
    }

    KeyEventListener(controller)
    LaunchedEffect(refreshFlow, followingUsers) {
        refreshFlow.collect { refreshedPage ->
            if (refreshedPage == LatestPage.Following) {
                followingUsers.refresh()
            }
        }
    }
    FollowingScreenBody(
        followingUsers = followingUsers,
        navToPictureScreen = navigationManager::navigateToPictureScreen,
        navToUserProfile = navigationManager::navigateToProfileDetailScreen,
        modifier = modifier.fillMaxSize(),
        lazyListState = listState,
        lazyGridState = gridState,
        showIllusts = appViewMode == AppViewMode.ILLUST,
    )
}
