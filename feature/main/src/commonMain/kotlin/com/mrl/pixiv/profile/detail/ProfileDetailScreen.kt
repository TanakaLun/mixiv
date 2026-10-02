package com.mrl.pixiv.profile.detail

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material.icons.rounded.PersonOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.mrl.pixiv.common.compose.ui.BlockSurface
import com.mrl.pixiv.common.compose.ui.image.UserAvatar
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.data.Restrict
import com.mrl.pixiv.common.data.Type
import com.mrl.pixiv.common.data.user.UserDetailResp
import com.mrl.pixiv.common.repository.BlockingRepositoryV2
import com.mrl.pixiv.common.repository.isSelf
import com.mrl.pixiv.common.repository.viewmodel.follow.isFollowing
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.allowRgb565
import com.mrl.pixiv.common.util.copyToClipboard
import com.mrl.pixiv.common.util.throttleClick
import com.mrl.pixiv.common.viewmodel.asState
import com.mrl.pixiv.profile.detail.components.IllustWidget
import com.mrl.pixiv.profile.detail.components.NovelBookmarkWidget
import com.mrl.pixiv.profile.detail.components.NovelWorksWidget
import com.mrl.pixiv.profile.detail.components.shouldShowNovelWorks
import com.mrl.pixiv.strings.block_user
import com.mrl.pixiv.strings.cancel_user_blocked
import com.mrl.pixiv.strings.followed
import com.mrl.pixiv.strings.illust_and_manga_liked
import com.mrl.pixiv.strings.illustration_count
import com.mrl.pixiv.strings.illustration_works
import com.mrl.pixiv.strings.manga
import com.mrl.pixiv.strings.private_follow
import com.mrl.pixiv.strings.profile_account
import com.mrl.pixiv.strings.profile_birthday
import com.mrl.pixiv.strings.profile_chair
import com.mrl.pixiv.strings.profile_comment
import com.mrl.pixiv.strings.profile_desk
import com.mrl.pixiv.strings.profile_desktop
import com.mrl.pixiv.strings.profile_detail_title
import com.mrl.pixiv.strings.profile_details
import com.mrl.pixiv.strings.profile_job
import com.mrl.pixiv.strings.profile_monitor
import com.mrl.pixiv.strings.profile_mouse
import com.mrl.pixiv.strings.profile_music
import com.mrl.pixiv.strings.profile_pawoo
import com.mrl.pixiv.strings.profile_pc
import com.mrl.pixiv.strings.profile_printer
import com.mrl.pixiv.strings.profile_region
import com.mrl.pixiv.strings.profile_scanner
import com.mrl.pixiv.strings.profile_tablet
import com.mrl.pixiv.strings.profile_tool
import com.mrl.pixiv.strings.profile_twitter
import com.mrl.pixiv.strings.profile_twitter_url
import com.mrl.pixiv.strings.profile_webpage
import com.mrl.pixiv.strings.profile_workspace
import com.mrl.pixiv.strings.profile_workspace_comment
import com.mrl.pixiv.strings.report_user
import com.mrl.pixiv.strings.user_blocked
import com.mrl.pixiv.strings.view_all
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.HorizontalDivider
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowListPopup

private const val KEY_USER_INFO = "user_info"
private const val KEY_USER_DETAILS = "user_details"
private const val KEY_USER_ILLUSTS = "user_illusts"
private const val KEY_USER_MANGAS = "user_mangas"
private const val KEY_USER_NOVELS = "user_novels"
private const val KEY_USER_BOOKMARKS_ILLUSTS = "user_bookmarks_illusts"
private const val KEY_USER_BOOKMARKS_NOVELS = "user_bookmarks_novels"
private const val KEY_SPACE = "space"

@Composable
fun ProfileDetailScreen(
    uid: Long,
    modifier: Modifier = Modifier,
    viewModel: ProfileDetailViewModel = koinViewModel { parametersOf(uid) },
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val state = viewModel.asState()
    val coroutineScope = rememberCoroutineScope()
    val userInfo = state.userInfo
    val scrollBehavior = MiuixScrollBehavior()
    val lazyListState = rememberLazyListState()
    val isBlocked = BlockingRepositoryV2.collectUserBlockAsState(uid)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            if (isBlocked) {
                TopAppBar(
                    title = stringResource(RStrings.profile_detail_title),
                    navigationIcon = {
                        IconButton(
                            onClick = { navigationManager.popBackStack() },
                        ) {
                            Icon(
                                imageVector = MiuixIcons.Back,
                                contentDescription = null,
                            )
                        }
                    },
                )
            } else {
                ProfileDetailAppBar(
                    userInfo = userInfo,
                    scrollBehavior = scrollBehavior,
                    isBlocked = isBlocked,
                    onBack = navigationManager::popBackStack,
                    onPrivateFollow = { userId ->
                        viewModel.followUser(userId, Restrict.PRIVATE)
                    },
                    onBlockUser = { userId ->
                        viewModel.blockUser(userId)
                    }
                )
            }
        },
    ) {
        if (isBlocked) {
            BlockSurface(
                modifier = Modifier.fillMaxSize(),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.PersonOff,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp)
                    )
                },
                title = {
                    Text(
                        text = stringResource(RStrings.user_blocked),
                        style = MiuixTheme.textStyles.body1,
                    )
                },
                button = {
                    Button(
                        onClick = {
                            viewModel.removeBlockUser(uid)
                        },
                    ) {
                        Text(text = stringResource(RStrings.cancel_user_blocked))
                    }
                }
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .padding(it)
                    .fillMaxWidth()
                    .pageScrollModifiers(scrollBehavior),
                state = lazyListState,
                contentPadding = PaddingValues(horizontal = 15.dp)
            ) {
                item(key = KEY_USER_INFO) {
                    Card {
                        BasicComponent(
                            title = userInfo.user.name,
                            summary = "${userInfo.profile.totalFollowUsers} ${stringResource(RStrings.followed)} • ID: ${userInfo.user.id}",
                            startAction = {
                                UserAvatar(
                                    url = userInfo.user.profileImageUrls.medium,
                                    modifier = Modifier.size(48.dp),
                                )
                            },
                            modifier = Modifier.throttleClick(
                                onClick = {
                                    navigationManager.navigateToFollowingScreen(userInfo.user.id)
                                },
                                onLongClick = {
                                    coroutineScope.launch {
                                        copyToClipboard(userInfo.user.id.toString())
                                    }
                                },
                            ),
                        )
                    }
                }
                item(key = KEY_USER_DETAILS) {
                    ProfileDetails(userInfo = userInfo)
                    Spacer(modifier = Modifier.height(20.dp))
                }
                if (state.userIllusts.isNotEmpty()) {
                    item(key = KEY_USER_ILLUSTS) {
                        // 插画、漫画网格组件
                        IllustWidget(
                            title = stringResource(RStrings.illustration_works),
                            endText = stringResource(
                                RStrings.illustration_count,
                                userInfo.profile.totalIllusts
                            ),
                            navToPictureScreen = navigationManager::navigateToPictureScreen,
                            illusts = state.userIllusts,
                            modifier = Modifier.fillMaxWidth(),
                            onAllClick = {
                                navigationManager.navigateToUserIllustScreen(uid)
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                if (state.userMangas.isNotEmpty()) {
                    item(key = KEY_USER_MANGAS) {
                        IllustWidget(
                            title = stringResource(RStrings.manga),
                            endText = stringResource(RStrings.view_all),
                            navToPictureScreen = navigationManager::navigateToPictureScreen,
                            illusts = state.userMangas,
                            modifier = Modifier.fillMaxWidth(),
                            onAllClick = {
                                navigationManager.navigateToUserIllustScreen(uid, Type.Manga)
                            }
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                if (shouldShowNovelWorks(state.userNovels)) {
                    item(key = KEY_USER_NOVELS) {
                        NovelWorksWidget(
                            novels = state.userNovels,
                            onAllClick = {
                                navigationManager.navigateToUserNovelsScreen(uid)
                            },
                            onNovelClick = navigationManager::navigateToNovelDetailScreen,
                            onSeriesClick = navigationManager::navigateToNovelSeriesScreen,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                if (state.userBookmarksIllusts.isNotEmpty()) {
                    item(key = KEY_USER_BOOKMARKS_ILLUSTS) {
                        // 插画、漫画收藏网格组件
                        IllustWidget(
                            title = stringResource(RStrings.illust_and_manga_liked),
                            endText = stringResource(RStrings.view_all),
                            navToPictureScreen = navigationManager::navigateToPictureScreen,
                            illusts = state.userBookmarksIllusts,
                            modifier = Modifier.fillMaxWidth(),
                            onAllClick = {
                                navigationManager.navigateToCollectionScreen(uid)
                            }
                        )
                    }
                }
                item(key = KEY_USER_BOOKMARKS_NOVELS) {
                    if (state.userBookmarksNovels.isNotEmpty()) {
                        // 小说收藏网格组件
                        NovelBookmarkWidget(
                            novels = state.userBookmarksNovels,
                            onAllClick = {
                                navigationManager.navigateToCollectionScreen(uid, true)
                            },
                            onNovelClick = navigationManager::navigateToNovelDetailScreen,
                            onSeriesClick = navigationManager::navigateToNovelSeriesScreen,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 20.dp)
                        )
                    }
                }
                item(key = KEY_SPACE) {
                    Spacer(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    )
                }
            }
        }
    }
}

private data class ProfileDetailItem(
    val label: String,
    val value: String,
)

@Composable
private fun ProfileDetails(
    userInfo: UserDetailResp,
    modifier: Modifier = Modifier,
) {
    val profile = userInfo.profile
    val workspace = userInfo.workspace
    val profileDetails = listOf(
        ProfileDetailItem(
            stringResource(RStrings.profile_comment),
            htmlToAnnotatedString(
                html = userInfo.user.comment,
                compactMode = true,
            ).toString(),
        ),
        ProfileDetailItem(stringResource(RStrings.profile_account), userInfo.user.account),
        ProfileDetailItem(stringResource(RStrings.profile_webpage), profile.webpage),
        ProfileDetailItem(stringResource(RStrings.profile_birthday), profile.birth),
        ProfileDetailItem(stringResource(RStrings.profile_region), profile.region),
        ProfileDetailItem(stringResource(RStrings.profile_job), profile.job),
        ProfileDetailItem(stringResource(RStrings.profile_twitter), profile.twitterAccount),
        ProfileDetailItem(stringResource(RStrings.profile_twitter_url), profile.twitterURL),
        ProfileDetailItem(stringResource(RStrings.profile_pawoo), profile.pawooURL),
    ).filter { it.value.isNotEmpty() }
    val workspaceDetails = if (workspace == null) {
        emptyList()
    } else {
        listOf(
            ProfileDetailItem(stringResource(RStrings.profile_pc), workspace.pc),
            ProfileDetailItem(stringResource(RStrings.profile_monitor), workspace.monitor),
            ProfileDetailItem(stringResource(RStrings.profile_tool), workspace.tool),
            ProfileDetailItem(stringResource(RStrings.profile_scanner), workspace.scanner),
            ProfileDetailItem(stringResource(RStrings.profile_tablet), workspace.tablet),
            ProfileDetailItem(stringResource(RStrings.profile_mouse), workspace.mouse),
            ProfileDetailItem(stringResource(RStrings.profile_printer), workspace.printer),
            ProfileDetailItem(stringResource(RStrings.profile_desktop), workspace.desktop),
            ProfileDetailItem(stringResource(RStrings.profile_music), workspace.music),
            ProfileDetailItem(stringResource(RStrings.profile_desk), workspace.desk),
            ProfileDetailItem(stringResource(RStrings.profile_chair), workspace.chair),
            ProfileDetailItem(
                stringResource(RStrings.profile_workspace_comment),
                workspace.comment,
            ),
        ).filter { it.value.isNotEmpty() }
    }
    val workspaceImageUrl = workspace?.workspaceImageURL.orEmpty()

    if (profileDetails.isEmpty() && workspaceDetails.isEmpty() && workspaceImageUrl.isEmpty()) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        if (profileDetails.isNotEmpty()) {
            ProfileDetailSection(
                title = stringResource(RStrings.profile_details),
                details = profileDetails,
            )
        }

        if (workspaceDetails.isNotEmpty() || workspaceImageUrl.isNotEmpty()) {
            ProfileDetailSection(
                title = stringResource(RStrings.profile_workspace),
                details = workspaceDetails,
                imageUrl = workspaceImageUrl,
            )
        }
    }
}

@Composable
private fun ProfileDetailSection(
    title: String,
    details: List<ProfileDetailItem>,
    modifier: Modifier = Modifier,
    imageUrl: String = "",
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = title,
            modifier = Modifier.padding(horizontal = 4.dp),
            style = MiuixTheme.textStyles.subtitle,
            fontWeight = FontWeight.SemiBold,
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (imageUrl.isNotEmpty()) {
                    val imageShape = RoundedCornerShape(12.dp)
                    AsyncImage(
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .data(imageUrl)
                            .allowRgb565(true)
                            .build(),
                        contentDescription = title,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 320.dp)
                            .clip(imageShape)
                            .border(
                                width = 1.dp,
                                color = MiuixTheme.colorScheme.dividerLine,
                                shape = imageShape,
                            ),
                    )
                }
                if (imageUrl.isNotEmpty() && details.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
                if (details.isNotEmpty()) {
                    SelectionContainer {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            details.forEachIndexed { index, detail ->
                                ProfileDetailRow(
                                    label = detail.label,
                                    value = detail.value,
                                )
                                if (index < details.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(vertical = 12.dp),
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
private fun ProfileDetailRow(label: String, value: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = label,
            style = MiuixTheme.textStyles.footnote1,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        )
        Text(
            text = value,
            style = MiuixTheme.textStyles.body2,
        )
    }
}

@Composable
private fun ProfileDetailAppBar(
    userInfo: UserDetailResp,
    scrollBehavior: ScrollBehavior,
    isBlocked: Boolean,
    onBack: () -> Unit,
    onPrivateFollow: (Long) -> Unit,
    onBlockUser: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showMenu by rememberSaveable { mutableStateOf(false) }

    TopAppBar(
        title = stringResource(RStrings.profile_detail_title),
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = MiuixIcons.Back,
                    contentDescription = null,
                )
            }
        },
        actions = {
            if (!isBlocked && !userInfo.user.isSelf) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Rounded.MoreVert,
                        contentDescription = null,
                    )
                }
            }
            WindowListPopup(
                show = showMenu,
                onDismissRequest = { showMenu = false },
            ) {
                ListPopupColumn {
                    val isSelf = userInfo.user.isSelf
                    if (!userInfo.user.isFollowing && !isSelf) {
                        BasicComponent(
                            title = stringResource(RStrings.private_follow),
                            onClick = {
                                onPrivateFollow(userInfo.user.id)
                                showMenu = false
                            },
                        )
                    }
                    if (!isSelf) {
                        BasicComponent(
                            title = stringResource(RStrings.block_user),
                            onClick = {
                                onBlockUser(userInfo.user.id)
                                showMenu = false
                            },
                        )
                        BasicComponent(
                            title = stringResource(RStrings.report_user),
                            onClick = {
                                showMenu = false
                            },
                        )
                    }
                }
            }
        },
        scrollBehavior = scrollBehavior,
        modifier = modifier,
    )
}
