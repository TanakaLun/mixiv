package com.mrl.pixiv

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffoldDefaults
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.analytics.logEvent
import com.mrl.pixiv.common.repository.VersionManager
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
import org.jetbrains.compose.resources.stringResource
import org.koin.core.annotation.KoinExperimentalAPI
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem

@Composable
fun MainNavigationScaffold(
    showNavigation: Boolean,
    navigationManager: NavigationManager,
    content: @Composable () -> Unit,
) {
    val page = navigationManager.currentMainPage
    val hasNewVersion by VersionManager.hasNewVersion.collectAsStateWithLifecycle()
    val screens = remember {
        listOf(
            MainPage.Home to RStrings.home,
            MainPage.Ranking to RStrings.ranking,
            MainPage.Latest to RStrings.new_artworks,
            MainPage.Search to RStrings.search,
            MainPage.Profile to RStrings.my,
        )
    }
    val layoutType = if (showNavigation) {
        NavigationSuiteScaffoldDefaults.navigationSuiteType(currentWindowAdaptiveInfoV2())
    } else {
        NavigationSuiteType.None
    }

    when (layoutType) {
        NavigationSuiteType.None -> content()
        NavigationSuiteType.BottomBar -> Column(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
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
        }

        else -> Row(modifier = Modifier.fillMaxSize()) {
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
            Box(modifier = Modifier.weight(1f)) {
                content()
            }
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
    AnimatedContent(
        targetState = page,
        modifier = modifier,
        transitionSpec = {
            fadeIn(animationSpec = tween(220, delayMillis = 90))
                .togetherWith(fadeOut(animationSpec = tween(90)))
        }
    ) {
        when (it) {
            MainPage.Home -> HomeScreen()
            MainPage.Ranking -> RankingScreen()
            MainPage.Latest -> LatestScreen()
            MainPage.Search -> SearchPreviewScreen()
            MainPage.Profile -> ProfileScreen()
        }
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
