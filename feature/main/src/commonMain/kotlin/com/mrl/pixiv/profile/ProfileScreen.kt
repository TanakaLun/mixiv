package com.mrl.pixiv.profile

import com.mrl.pixiv.common.data.AppViewMode
import com.mrl.pixiv.common.repository.requireUserPreferenceValue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.compose.rememberThrottleClick
import com.mrl.pixiv.common.compose.ui.image.UserAvatar
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.repository.VersionManager
import com.mrl.pixiv.common.repository.requireUserInfoFlow
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.about
import com.mrl.pixiv.strings.app_data
import com.mrl.pixiv.strings.block_settings
import com.mrl.pixiv.strings.bookmark_tags
import com.mrl.pixiv.strings.collection
import com.mrl.pixiv.strings.download_manager
import com.mrl.pixiv.strings.export_token
import com.mrl.pixiv.strings.history
import com.mrl.pixiv.strings.new_version_available
import com.mrl.pixiv.strings.novel_markers
import com.mrl.pixiv.strings.preference
import com.mrl.pixiv.strings.read_later
import com.mrl.pixiv.strings.sign_out
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar

private const val KEY_USER_INFO = "user_info"
private const val KEY_MAIN_PREFS = "main_prefs"
private const val KEY_ACCOUNT_PREFS = "account_prefs"

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = koinViewModel(),
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val userInfo by requireUserInfoFlow.collectAsStateWithLifecycle()
    val hasNewVersion by VersionManager.hasNewVersion.collectAsStateWithLifecycle()
    LifecycleResumeEffect(Unit) {
        viewModel.dispatch(ProfileAction.GetUserInfo)
        onPauseOrDispose {}
    }
    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        topBar = {
            ProfileAppBar(scrollBehavior = scrollBehavior)
        },
    ) {
        LazyColumn(
            modifier = modifier
                .padding(it)
                .fillMaxSize()
                .padding(top = 16.dp)
                .pageScrollModifiers(scrollBehavior),
        ) {
            item(key = KEY_USER_INFO) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    UserAvatar(
                        url = userInfo.user.profileImageUrls.medium,
                        modifier = Modifier.size(80.dp),
                        onClick = {
                            navigationManager.navigateToProfileDetailScreen(userInfo.user.id)
                        },
                    )
                    Column {
                        Text(text = userInfo.user.name)
                        Text(text = "ID: ${userInfo.user.id}")
                    }
                }
            }
            item(key = KEY_MAIN_PREFS) {
                Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    BasicComponent(
                        title = stringResource(RStrings.preference),
                        startAction = {
                            Icon(imageVector = Icons.Rounded.Settings, contentDescription = null)
                        },
                        onClick = rememberThrottleClick {
                            navigationManager.navigateToSettingScreen()
                        },
                    )
                    BasicComponent(
                        title = stringResource(RStrings.history),
                        startAction = {
                            Icon(imageVector = Icons.Rounded.History, contentDescription = null)
                        },
                        onClick = rememberThrottleClick {
                            navigationManager.navigateToHistoryScreen()
                        },
                    )
                    BasicComponent(
                        title = stringResource(RStrings.read_later),
                        startAction = {
                            Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null)
                        },
                        onClick = rememberThrottleClick {
                            navigationManager.navigateToNovelReadLaterScreen()
                        },
                    )
                    BasicComponent(
                        title = stringResource(RStrings.collection),
                        startAction = {
                            Icon(imageVector = Icons.Rounded.Bookmarks, contentDescription = null)
                        },
                        onClick = rememberThrottleClick {
                            navigationManager.navigateToCollectionScreen(
                                userInfo.user.id,
                                isNovel = requireUserPreferenceValue.collectionViewMode == AppViewMode.NOVEL,
                            )
                        },
                    )
                    BasicComponent(
                        title = stringResource(RStrings.novel_markers),
                        startAction = {
                            Icon(imageVector = Icons.Rounded.Bookmark, contentDescription = null)
                        },
                        onClick = rememberThrottleClick {
                            navigationManager.navigateToNovelMarkersScreen()
                        },
                    )
                    BasicComponent(
                        title = stringResource(RStrings.bookmark_tags),
                        startAction = {
                            Icon(imageVector = Icons.Rounded.Style, contentDescription = null)
                        },
                        onClick = rememberThrottleClick {
                            navigationManager.navigateToBookmarkedTagsScreen()
                        },
                    )
                    BasicComponent(
                        title = stringResource(RStrings.block_settings),
                        startAction = {
                            Icon(imageVector = Icons.Rounded.Block, contentDescription = null)
                        },
                        onClick = rememberThrottleClick {
                            navigationManager.navigateToBlockSettings()
                        },
                    )
                    BasicComponent(
                        title = stringResource(RStrings.download_manager),
                        startAction = {
                            Icon(imageVector = Icons.Rounded.Download, contentDescription = null)
                        },
                        onClick = rememberThrottleClick {
                            navigationManager.navigateToDownloadScreen()
                        },
                    )
                    BasicComponent(
                        title = stringResource(RStrings.app_data),
                        startAction = {
                            Icon(imageVector = Icons.Rounded.Storage, contentDescription = null)
                        },
                        onClick = rememberThrottleClick {
                            navigationManager.navigateToAppDataScreen()
                        },
                    )
                }
            }
            item(key = KEY_ACCOUNT_PREFS) {
                Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                    BasicComponent(
                        title = stringResource(RStrings.export_token),
                        startAction = {
                            Icon(imageVector = Icons.Rounded.ImportExport, contentDescription = null)
                        },
                        onClick = rememberThrottleClick {
                            viewModel.dispatch(ProfileAction.ExportToken)
                        },
                    )
                    BasicComponent(
                        title = stringResource(RStrings.about),
                        startAction = {
                            Icon(imageVector = Icons.Rounded.Info, contentDescription = null)
                        },
                        endActions = {
                            if (hasNewVersion) {
                                Badge {
                                    Text(text = stringResource(RStrings.new_version_available))
                                }
                            }
                        },
                        onClick = rememberThrottleClick {
                            navigationManager.navigateToAboutScreen()
                        },
                    )
                    BasicComponent(
                        title = stringResource(RStrings.sign_out),
                        startAction = {
                            Icon(imageVector = Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                        },
                        onClick = rememberThrottleClick {
                            viewModel.logout()
                            navigationManager.navigateToLoginOptionScreen()
                        },
                    )
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun ProfileAppBar(scrollBehavior: ScrollBehavior) {
    TopAppBar(
        title = "",
        scrollBehavior = scrollBehavior,
    )
}
