package com.mrl.pixiv

import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.analytics.logEvent
import com.mrl.pixiv.common.compose.LocalSnackbarHostState
import com.mrl.pixiv.common.repository.VersionManager
import com.mrl.pixiv.common.router.Destination
import com.mrl.pixiv.common.router.MainPage
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.home.HomeScreen
import com.mrl.pixiv.latest.LatestScreen
import com.mrl.pixiv.profile.ProfileScreen
import com.mrl.pixiv.ranking.RankingScreen
import com.mrl.pixiv.search.preview.SearchPreviewScreen
import com.mrl.pixiv.strings.home
import com.mrl.pixiv.strings.my
import com.mrl.pixiv.strings.new_artworks
import com.mrl.pixiv.strings.ranking
import com.mrl.pixiv.strings.search
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import kotlin.math.sqrt
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.core.annotation.KoinExperimentalAPI
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.SnackbarHost

/** Spring spec shared by pager tab navigation and snap fling (miuix example). */
private val PagerNavigationSpringSpec: SpringSpec<Float> = spring(
    stiffness = 322.2f,
    dampingRatio = 32.31f / (2f * sqrt(322.2f)),
    visibilityThreshold = 0.5f,
)

private fun NavigationSuiteType.isRail(): Boolean = when (this) {
    NavigationSuiteType.NavigationRail,
    NavigationSuiteType.WideNavigationRailCollapsed,
    NavigationSuiteType.WideNavigationRailExpanded,
    -> true

    else -> false
}

@Composable
private fun rememberMainScreens(): List<Pair<MainPage, StringResource>> = remember {
    listOf(
        MainPage.Home to RStrings.home,
        MainPage.Ranking to RStrings.ranking,
        MainPage.Latest to RStrings.new_artworks,
        MainPage.Search to RStrings.search,
        MainPage.Profile to RStrings.my,
    )
}

/**
 * Adaptive chrome around [NavDisplay]. The bottom bar lives inside [MainScreen] (the Main entry),
 * so pushed pages cover it with the normal stack transition; only the wide-screen rail is a
 * persistent sibling of the nav host, matching the miuix example layout.
 */
@Composable
fun MainNavigationScaffold(
    navigationManager: NavigationManager,
    content: @Composable () -> Unit,
) {
    val page = navigationManager.currentMainPage
    val hasNewVersion by VersionManager.hasNewVersion.collectAsStateWithLifecycle()
    val screens = rememberMainScreens()
    val layoutType = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfoV2())
    val snackbarHostState = LocalSnackbarHostState.current
    val navigationBarsBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    val snackbarBottomPadding =
        navigationBarsBottom + if (!layoutType.isRail() && navigationManager.currentDestination is Destination.Main) {
            64.dp + 12.dp
        } else {
            12.dp
        }

    Row(modifier = Modifier.fillMaxSize()) {
        if (layoutType.isRail()) {
            NavigationRail {
                screens.forEach { (screen, title) ->
                    NavigationRailItem(
                        selected = page == screen,
                        onClick = {
                            if (page != screen) {
                                navigationManager.switchMainPage(screen)
                            }
                        },
                        icon = screen.icon,
                        label = stringResource(title),
                        badge = if (screen == MainPage.Profile && hasNewVersion) {
                            { Badge() }
                        } else {
                            null
                        },
                    )
                }
            }
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
        ) {
            content()
            SnackbarHost(
                state = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = snackbarBottomPadding),
            )
        }
    }
}

@OptIn(KoinExperimentalAPI::class, InternalSerializationApi::class)
@Composable
fun MainScreen(
    modifier: Modifier = Modifier,
) {
    val navigationManager = currentNavigationManager()
    val page = navigationManager.currentMainPage
    val hasNewVersion by VersionManager.hasNewVersion.collectAsStateWithLifecycle()
    val screens = rememberMainScreens()
    val pagerState = rememberPagerState(
        initialPage = screens.indexOfFirst { it.first == page }.coerceAtLeast(0),
    ) { screens.size }
    val layoutType = NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfoV2())
    val showBottomBar = !layoutType.isRail()

    // Tab item / rail click -> pager.
    LaunchedEffect(page) {
        val target = screens.indexOfFirst { it.first == page }
        if (target >= 0 && target != pagerState.settledPage && !pagerState.isScrollInProgress) {
            pagerState.animateScrollToPage(target, animationSpec = PagerNavigationSpringSpec)
        }
    }
    // User swipe -> navigation state.
    LaunchedEffect(pagerState.settledPage) {
        val target = screens.getOrNull(pagerState.settledPage)?.first
        if (target != null && target != navigationManager.currentMainPage) {
            navigationManager.switchMainPage(target)
        }
    }

    val tabContent: @Composable (Modifier) -> Unit = { contentModifier ->
        HorizontalPager(
            state = pagerState,
            modifier = contentModifier,
            verticalAlignment = Alignment.Top,
            flingBehavior = PagerDefaults.flingBehavior(
                state = pagerState,
                snapAnimationSpec = PagerNavigationSpringSpec,
            ),
        ) { index ->
            when (screens[index].first) {
                MainPage.Home -> HomeScreen()
                MainPage.Ranking -> RankingScreen()
                MainPage.Latest -> LatestScreen()
                MainPage.Search -> SearchPreviewScreen()
                MainPage.Profile -> ProfileScreen()
            }
        }
    }

    if (showBottomBar) {
        Scaffold(
            modifier = modifier,
            contentWindowInsets = WindowInsets(0.dp),
            bottomBar = {
                NavigationBar {
                    screens.forEach { (screen, title) ->
                        NavigationBarItem(
                            selected = page == screen,
                            onClick = {
                                if (page != screen) {
                                    navigationManager.switchMainPage(screen)
                                }
                            },
                            icon = screen.icon,
                            label = stringResource(title),
                            badge = if (screen == MainPage.Profile && hasNewVersion) {
                                { Badge() }
                            } else {
                                null
                            },
                        )
                    }
                }
            },
        ) { padding ->
            tabContent(Modifier.padding(padding))
        }
    } else {
        tabContent(modifier)
    }

    LaunchedEffect(navigationManager.currentMainPage) {
        logEvent("screen_view", buildMap {
            val screenName =
                navigationManager.currentMainPage::class.serializer().descriptor.serialName
                    .split(".")
                    .lastOrNull()
                    .orEmpty()
            put("screen_name", screenName)
            put("screen_class", screenName)
        })
    }
}
