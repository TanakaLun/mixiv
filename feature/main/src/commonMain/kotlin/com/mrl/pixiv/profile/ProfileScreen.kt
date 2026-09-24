package com.mrl.pixiv.profile

import com.mrl.pixiv.common.data.AppViewMode
import com.mrl.pixiv.common.repository.requireUserPreferenceValue

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Block
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.ImportExport
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Storage
import androidx.compose.material.icons.rounded.Style
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.compose.LocalSharedTransitionScope
import com.mrl.pixiv.common.compose.rememberThrottleClick
import com.mrl.pixiv.common.compose.ui.image.UserAvatar
import com.mrl.pixiv.common.data.setting.SettingTheme
import com.mrl.pixiv.common.repository.SettingRepository
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
import com.mrl.pixiv.strings.theme_dark
import com.mrl.pixiv.strings.theme_light
import com.mrl.pixiv.strings.theme_system
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.window.WindowListPopup

private val options =
    mapOf(
        SettingTheme.SYSTEM to RStrings.theme_system,
        SettingTheme.LIGHT to RStrings.theme_light,
        SettingTheme.DARK to RStrings.theme_dark,
    )

private const val KEY_USER_INFO = "user_info"
private const val KEY_DIVIDER = "divider"
private const val KEY_PREFERENCE = "preference"
private const val KEY_HISTORY = "history"
private const val KEY_READ_LATER = "read_later"
private const val KEY_COLLECTION = "collection"
private const val KEY_NOVEL_MARKERS = "novel_markers"
private const val KEY_BOOKMARK_TAGS = "bookmark_tags"
private const val KEY_BLOCK_SETTINGS = "block_settings"
private const val KEY_DOWNLOAD_MANAGER = "download_manager"
private const val KEY_APP_DATA = "app_data"
private const val KEY_EXPORT_TOKEN = "export_token"
private const val KEY_ABOUT = "about"
private const val KEY_LOGOUT = "logout"

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
    Scaffold(
        topBar = {
            ProfileAppBar(
                onChangeAppTheme = { theme ->
                    viewModel.changeAppTheme(theme)
                },
            )
        },
    ) {
        LazyColumn(
            modifier = modifier
                .padding(it)
                .fillMaxSize()
                .padding(top = 16.dp),
        ) {
            item(key = KEY_USER_INFO) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    with(LocalSharedTransitionScope.current) {
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
            }
            item(key = KEY_DIVIDER) {
                HorizontalDivider(
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                )
            }
            item(key = KEY_PREFERENCE) {
                BasicComponent(
                    title = stringResource(RStrings.preference),
                    startAction = {
                        Icon(imageVector = Icons.Rounded.Settings, contentDescription = null)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = rememberThrottleClick {
                        navigationManager.navigateToSettingScreen()
                    },
                )
            }
            item(key = KEY_HISTORY) {
                BasicComponent(
                    title = stringResource(RStrings.history),
                    startAction = {
                        Icon(imageVector = Icons.Rounded.History, contentDescription = null)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = rememberThrottleClick {
                        navigationManager.navigateToHistoryScreen()
                    },
                )
            }
            item(key = KEY_READ_LATER) {
                BasicComponent(
                    title = stringResource(RStrings.read_later),
                    startAction = {
                        Icon(imageVector = Icons.Rounded.Schedule, contentDescription = null)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = rememberThrottleClick {
                        navigationManager.navigateToNovelReadLaterScreen()
                    },
                )
            }
            item(key = KEY_COLLECTION) {
                BasicComponent(
                    title = stringResource(RStrings.collection),
                    startAction = {
                        Icon(imageVector = Icons.Rounded.Bookmarks, contentDescription = null)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = rememberThrottleClick {
                        navigationManager.navigateToCollectionScreen(
                            userInfo.user.id,
                            isNovel = requireUserPreferenceValue.collectionViewMode == AppViewMode.NOVEL,
                        )
                    },
                )
            }
            item(key = KEY_NOVEL_MARKERS) {
                BasicComponent(
                    title = stringResource(RStrings.novel_markers),
                    startAction = {
                        Icon(imageVector = Icons.Rounded.Bookmark, contentDescription = null)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = rememberThrottleClick {
                        navigationManager.navigateToNovelMarkersScreen()
                    },
                )
            }
            item(key = KEY_BOOKMARK_TAGS) {
                BasicComponent(
                    title = stringResource(RStrings.bookmark_tags),
                    startAction = {
                        Icon(imageVector = Icons.Rounded.Style, contentDescription = null)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = rememberThrottleClick {
                        navigationManager.navigateToBookmarkedTagsScreen()
                    },
                )
            }
            item(key = KEY_BLOCK_SETTINGS) {
                BasicComponent(
                    title = stringResource(RStrings.block_settings),
                    startAction = {
                        Icon(imageVector = Icons.Rounded.Block, contentDescription = null)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = rememberThrottleClick {
                        navigationManager.navigateToBlockSettings()
                    },
                )
            }
            item(key = KEY_DOWNLOAD_MANAGER) {
                BasicComponent(
                    title = stringResource(RStrings.download_manager),
                    startAction = {
                        Icon(imageVector = Icons.Rounded.Download, contentDescription = null)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = rememberThrottleClick {
                        navigationManager.navigateToDownloadScreen()
                    },
                )
            }
            item(key = KEY_APP_DATA) {
                BasicComponent(
                    title = stringResource(RStrings.app_data),
                    startAction = {
                        Icon(imageVector = Icons.Rounded.Storage, contentDescription = null)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = rememberThrottleClick {
                        navigationManager.navigateToAppDataScreen()
                    },
                )
            }
            item(key = KEY_EXPORT_TOKEN) {
                Column {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                    BasicComponent(
                        title = stringResource(RStrings.export_token),
                        startAction = {
                            Icon(imageVector = Icons.Rounded.ImportExport, contentDescription = null)
                        },
                        modifier = Modifier.padding(horizontal = 8.dp),
                        onClick = rememberThrottleClick {
                            viewModel.dispatch(ProfileAction.ExportToken)
                        },
                    )
                }
            }
            item(key = KEY_ABOUT) {
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
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = rememberThrottleClick {
                        navigationManager.navigateToAboutScreen()
                    },
                )
            }
            item(key = KEY_LOGOUT) {
                BasicComponent(
                    title = stringResource(RStrings.sign_out),
                    startAction = {
                        Icon(imageVector = Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
                    },
                    modifier = Modifier.padding(horizontal = 8.dp),
                    onClick = rememberThrottleClick {
                        viewModel.logout()
                        navigationManager.navigateToLoginOptionScreen()
                    },
                )
            }
        }
    }
}

@Composable
private fun ProfileAppBar(
    onChangeAppTheme: (SettingTheme) -> Unit = {},
) {
    val userPreference by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle()
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = "",
        actions = {
            IconButton(onClick = { expanded = true }) {
                Icon(imageVector = Icons.Rounded.Palette, contentDescription = null)
            }
            WindowListPopup(
                show = expanded,
                onDismissRequest = { expanded = false },
            ) {
                ListPopupColumn {
                    options.forEach { (theme, resId) ->
                        BasicComponent(
                            title = stringResource(resId),
                            onClick = {
                                onChangeAppTheme(theme)
                                expanded = false
                            },
                            endActions = if (userPreference.theme == theme.name) {
                                {
                                    Icon(imageVector = Icons.Rounded.Check, contentDescription = null)
                                }
                            } else {
                                null
                            },
                        )
                    }
                }
            }
        },
    )
}
