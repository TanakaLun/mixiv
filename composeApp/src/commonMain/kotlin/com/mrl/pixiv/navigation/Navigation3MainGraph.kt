package com.mrl.pixiv.navigation

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dokar.sonner.LocalToastContentColor
import com.dokar.sonner.Toaster
import com.dokar.sonner.ToasterState
import com.dokar.sonner.rememberToasterState
import com.mrl.pixiv.MainNavigationScaffold
import com.mrl.pixiv.MainScreen
import com.mrl.pixiv.artwork.ArtworkScreen
import com.mrl.pixiv.collection.CollectionScreen
import com.mrl.pixiv.collection.tags.BookmarkedTagsScreen
import com.mrl.pixiv.comment.BlockCommentsScreen
import com.mrl.pixiv.comment.CommentScreen
import com.mrl.pixiv.common.analytics.logEvent
import com.mrl.pixiv.common.compose.LocalToaster
import com.mrl.pixiv.common.compose.layout.PaneInputScope
import com.mrl.pixiv.common.compose.layout.PaneInputState
import com.mrl.pixiv.common.repository.IllustCacheRepo
import com.mrl.pixiv.common.router.Destination
import com.mrl.pixiv.common.router.LocalNavigationManager
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.NavigationRecord
import com.mrl.pixiv.common.router.paneSpec
import com.mrl.pixiv.common.router.rememberNavigationState
import com.mrl.pixiv.common.toast.ToastMessage
import com.mrl.pixiv.common.util.ToastUtil
import com.mrl.pixiv.common.util.result.LocalResultEventBus
import com.mrl.pixiv.common.util.result.ResultEventBus
import com.mrl.pixiv.follow.FollowingScreen
import com.mrl.pixiv.history.HistoryScreen
import com.mrl.pixiv.image.preview.ImagePreviewScreen
import com.mrl.pixiv.login.LoginOptionScreen
import com.mrl.pixiv.login.LoginScreen
import com.mrl.pixiv.login.oauth.OAuthLoginScreen
import com.mrl.pixiv.login.oauth.WebCookieLoginScreen
import com.mrl.pixiv.novel.NovelScreen
import com.mrl.pixiv.novel.readlater.NovelReadLaterScreen
import com.mrl.pixiv.novel.series.NovelSeriesScreen
import com.mrl.pixiv.picture.HorizontalSwipePictureScreen
import com.mrl.pixiv.picture.PictureDeeplinkScreen
import com.mrl.pixiv.profile.detail.ProfileDetailScreen
import com.mrl.pixiv.profile.marker.NovelMarkersScreen
import com.mrl.pixiv.report.ReportScreen
import com.mrl.pixiv.search.SearchScreen
import com.mrl.pixiv.search.result.SearchResultsScreen
import com.mrl.pixiv.setting.BrowsingSettingScreen
import com.mrl.pixiv.setting.FileNameFormatScreen
import com.mrl.pixiv.setting.HistorySettingScreen
import com.mrl.pixiv.setting.PrivacySettingScreen
import com.mrl.pixiv.setting.SearchSettingScreen
import com.mrl.pixiv.setting.SettingScreen
import com.mrl.pixiv.setting.about.AboutScreen
import com.mrl.pixiv.setting.ai.AiTranslationSettingScreen
import com.mrl.pixiv.setting.appdata.AppDataScreen
import com.mrl.pixiv.setting.block.BlockIllustScreen
import com.mrl.pixiv.setting.block.BlockNovelScreen
import com.mrl.pixiv.setting.block.BlockSettingsScreen
import com.mrl.pixiv.setting.block.BlockTagScreen
import com.mrl.pixiv.setting.block.BlockUserScreen
import com.mrl.pixiv.setting.download.DownloadScreen
import com.mrl.pixiv.setting.network.NetworkSettingScreen
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.serializer
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.transition.NavTransitions

@Composable
fun Navigation3MainGraph(
    startDestination: Destination,
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = koinInject { parametersOf(arrayOf(startDestination)) }
) {
    val toastState = rememberToasterState()
    val resultBus = remember { ResultEventBus() }

    rememberNavigationState(navigationManager)
    val inputState = remember { PaneInputState() }
    val topRecord = navigationManager.backStack.last()
    val showMainNavigation = topRecord.destination == Destination.Main &&
        !topRecord.destination.paneSpec.preferredFullWidth
    LaunchedEffect(topRecord.entryId) {
        inputState.activeEntryId = topRecord.entryId
        inputState.dividerFocused = false
    }

    HandleDeeplink(navigationManager)
    LogScreen(navigationManager)

    CompositionLocalProvider(
        LocalToaster provides toastState,
        LocalResultEventBus provides resultBus,
    ) {
        ToastMessage(toastState = toastState)
        MainNavigationScaffold(showMainNavigation, navigationManager) {
            BoxWithConstraints(modifier.fillMaxSize()) {
                NavDisplay(
                    backStack = navigationManager.navBackStack,
                    modifier = Modifier.fillMaxSize(),
                    onBack = navigationManager::popBackStack,
                    transition = NavTransitions.MiuixDefault,
                ) {
                    entry<NavigationRecord> { record ->
                        val destination = record.destination
                        val scopedNavigation = remember(navigationManager, record.entryId) {
                            navigationManager.forEntry(record.entryId)
                        }
                        CompositionLocalProvider(LocalNavigationManager provides scopedNavigation) {
                            PaneInputScope(record.entryId, inputState) {
                                when (destination) {
                                    is Destination.LoginOption -> LoginOptionScreen()
                                    is Destination.Login -> LoginScreen(startUrl = destination.startUrl)
                                    is Destination.OAuthLogin -> OAuthLoginScreen()
                                    is Destination.WebCookieLogin -> WebCookieLoginScreen()
                                    is Destination.Main -> MainScreen()
                                    is Destination.ProfileDetail -> ProfileDetailScreen(uid = destination.userId)
                                    is Destination.PictureDeeplink -> PictureDeeplinkScreen(
                                        illustId = destination.illustId,
                                    )
                                    is Destination.ImagePreview -> ImagePreviewScreen(
                                        imageUrls = destination.imageUrls,
                                        initialIndex = destination.initialIndex,
                                        onBack = navigationManager::popBackStack,
                                    )
                                    is Destination.Search -> SearchScreen()
                                    is Destination.SearchResults -> SearchResultsScreen(
                                        searchWords = destination.searchWords,
                                        searchMode = destination.searchMode,
                                        isIdSearch = destination.isIdSearch,
                                    )
                                    is Destination.Setting -> SettingScreen()
                                    is Destination.NetworkSetting -> NetworkSettingScreen()
                                    is Destination.BrowsingSetting -> BrowsingSettingScreen()
                                    is Destination.SearchSetting -> SearchSettingScreen()
                                    is Destination.HistorySetting -> HistorySettingScreen()
                                    is Destination.PrivacySetting -> PrivacySettingScreen()
                                    is Destination.FileNameFormat -> FileNameFormatScreen()
                                    is Destination.AiTranslationSetting -> AiTranslationSettingScreen()
                                    is Destination.History -> HistoryScreen()
                                    is Destination.NovelReadLater -> NovelReadLaterScreen()
                                    is Destination.Collection -> CollectionScreen(
                                        uid = destination.userId,
                                        isNovel = destination.isNovel,
                                    )
                                    is Destination.BookmarkedTags -> BookmarkedTagsScreen()
                                    is Destination.NovelMarkers -> NovelMarkersScreen()
                                    is Destination.Following -> FollowingScreen(uid = destination.userId)
                                    is Destination.Picture -> {
                                        DisposableEffect(record.entryId) {
                                            val prefix = destination.prefix
                                            onDispose {
                                                val stillUsed = navigationManager.backStack.any {
                                                    (it.destination as? Destination.Picture)?.prefix == prefix
                                                }
                                                if (!stillUsed) IllustCacheRepo.removeList(prefix)
                                            }
                                        }
                                        val illusts = remember { IllustCacheRepo[destination.prefix] }
                                        HorizontalSwipePictureScreen(
                                            illusts = illusts.toImmutableList(),
                                            index = destination.index,
                                            enableTransition = destination.enableTransition,
                                        )
                                    }
                                    is Destination.UserArtwork -> ArtworkScreen(
                                        userId = destination.userId,
                                        initialType = destination.initialType,
                                    )
                                    is Destination.UserNovels -> ArtworkScreen(
                                        userId = destination.userId,
                                        initialNovel = true,
                                    )
                                    is Destination.BlockSettings -> BlockSettingsScreen()
                                    is Destination.BlockIllust -> BlockIllustScreen()
                                    is Destination.BlockNovel -> BlockNovelScreen()
                                    is Destination.BlockUser -> BlockUserScreen()
                                    is Destination.BlockTag -> BlockTagScreen()
                                    is Destination.BlockComments -> BlockCommentsScreen()
                                    is Destination.AppData -> AppDataScreen()
                                    is Destination.Download -> DownloadScreen()
                                    is Destination.About -> AboutScreen()
                                    is Destination.Comment -> CommentScreen(
                                        id = destination.id,
                                        type = destination.type,
                                    )
                                    is Destination.Report -> ReportScreen(
                                        id = destination.id,
                                        type = destination.type,
                                    )
                                    is Destination.NovelDetail -> NovelScreen(
                                        novelId = destination.novelId,
                                        markerPage = destination.markerPage,
                                        readLaterTargetLanguage = destination.readLaterTargetLanguage,
                                    )
                                    is Destination.NovelSeries -> NovelSeriesScreen(
                                        seriesId = destination.seriesId,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ToastMessage(toastState: ToasterState) {
    LaunchedEffect(Unit) {
        ToastUtil.toastFlow.collect {
            toastState.show(it)
        }
    }
    Toaster(
        state = toastState,
        darkTheme = isSystemInDarkTheme(),
        richColors = true,
        alignment = Alignment.TopCenter,
        showCloseButton = true,
        messageSlot = {
            val contentColor = LocalToastContentColor.current
            when (val message = it.message) {
                is String -> BasicText(text = message, color = { contentColor })
                is StringResource -> BasicText(
                    text = stringResource(message),
                    color = { contentColor })

                is ToastMessage.Compose -> message.content()
                else -> BasicText(text = it.message.toString(), color = { contentColor })
            }
        }
    )
}

@Composable
internal expect fun HandleDeeplink(
    navigationManager: NavigationManager,
)

@OptIn(InternalSerializationApi::class)
@Composable
private fun LogScreen(
    navigationManager: NavigationManager,
) {
    LaunchedEffect(navigationManager.currentDestination) {
        // Get current destination
        val currentDestination = navigationManager.currentDestination

        // Log screen view event
        logEvent("screen_view", buildMap {
            val screenName = currentDestination::class.serializer().descriptor.serialName
                .split(".")
                .lastOrNull()
                .orEmpty()
            put("screen_name", screenName)
            put("screen_class", screenName)

            // Add additional parameters for specific destinations
            when (currentDestination) {
                is Destination.Login -> {
                    put("start_url", currentDestination.startUrl)
                }

                is Destination.Main -> {
                    val screenName =
                        navigationManager.currentMainPage::class.serializer().descriptor.serialName
                            .split(".")
                            .lastOrNull()
                            .orEmpty()
                    put("current_main_page", screenName)
                }

                is Destination.ProfileDetail -> {
                    put("user_id", currentDestination.userId.toString())
                }

                is Destination.PictureDeeplink -> {
                    put("illust_id", currentDestination.illustId.toString())
                }

                is Destination.SearchResults -> {
                    put("search_words", currentDestination.searchWords)
                    put("is_id_search", currentDestination.isIdSearch.toString())
                    put("search_mode", currentDestination.searchMode.name)
                }

                is Destination.Picture -> {
                    put("index", currentDestination.index.toString())
                    put("prefix", currentDestination.prefix)
                    put("enable_transition", currentDestination.enableTransition.toString())
                }

                is Destination.Collection -> {
                    put("user_id", currentDestination.userId.toString())
                    put("is_novel", currentDestination.isNovel.toString())
                }

                is Destination.Following -> {
                    put("user_id", currentDestination.userId.toString())
                }

                is Destination.UserArtwork -> {
                    put("user_id", currentDestination.userId.toString())
                }

                is Destination.UserNovels -> {
                    put("user_id", currentDestination.userId.toString())
                }

                is Destination.Comment -> {
                    put("id", currentDestination.id.toString())
                    put("type", currentDestination.type.toString())
                }

                is Destination.Report -> {
                    put("id", currentDestination.id.toString())
                    put("type", currentDestination.type.toString())
                }

                is Destination.NovelDetail -> {
                    put("novel_id", currentDestination.novelId.toString())
                }

                is Destination.NovelSeries -> {
                    put("series_id", currentDestination.seriesId.toString())
                }

                else -> Unit
            }
        })
    }
}
