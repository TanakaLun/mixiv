package com.mrl.pixiv.setting.about

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.retain.retain
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mikepenz.markdown.m3.Markdown
import com.mrl.pixiv.common.compose.rememberThrottleClick
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.data.Constants
import com.mrl.pixiv.common.repository.VersionManager
import com.mrl.pixiv.common.repository.VersionManager.getCurrentFlavorAsset
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.AppUtil
import com.mrl.pixiv.common.util.RDrawables
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.ShareUtil
import com.mrl.pixiv.strings.about
import com.mrl.pixiv.strings.app_name
import com.mrl.pixiv.strings.cancel
import com.mrl.pixiv.strings.check_update
import com.mrl.pixiv.strings.current_version
import com.mrl.pixiv.strings.download
import com.mrl.pixiv.strings.feedback
import com.mrl.pixiv.strings.feedback_content
import com.mrl.pixiv.strings.ic_launcher
import com.mrl.pixiv.strings.new_version_available
import com.mrl.pixiv.strings.project_url
import com.mrl.pixiv.strings.recommend_content
import com.mrl.pixiv.strings.recommend_this_app
import com.mrl.pixiv.strings.share_app
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun AboutScreen(
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()
    val hasNewVersion by VersionManager.hasNewVersion.collectAsStateWithLifecycle()
    val latestVersionInfo by VersionManager.latestVersionInfo.collectAsStateWithLifecycle()
    var showUpdateDialog by retain { mutableStateOf(false) }

    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = modifier.pageScrollModifiers(scrollBehavior),
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.about),
                navigationIcon = {
                    IconButton(onClick = { navigationManager.popBackStack() }) {
                        Icon(MiuixIcons.Back, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // App Icon and Version
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(RDrawables.ic_launcher),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape),
                )
                Text(
                    text = stringResource(RStrings.app_name),
                    style = MiuixTheme.textStyles.subtitle,
                    modifier = Modifier.padding(top = 16.dp)
                )
                Text(
                    text = stringResource(RStrings.current_version, AppUtil.versionName),
                    style = MiuixTheme.textStyles.body1,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                // Project URL
                BasicComponent(
                    title = stringResource(RStrings.project_url),
                    summary = Constants.GITHUB_URL,
                    onClick = rememberThrottleClick {
                        uriHandler.openUri(Constants.GITHUB_URL)
                    },
                )

                // Feedback
                BasicComponent(
                    title = stringResource(RStrings.feedback),
                    summary = stringResource(RStrings.feedback_content),
                    onClick = rememberThrottleClick {
                        uriHandler.openUri(Constants.GITHUB_ISSUE_URL)
                    },
                )

                // Share App
                BasicComponent(
                    title = stringResource(RStrings.share_app),
                    summary = stringResource(RStrings.recommend_this_app),
                    onClick = rememberThrottleClick {
                        coroutineScope.launch {
                            ShareUtil.shareText(
                                AppUtil.getString(
                                    RStrings.recommend_content,
                                    Constants.GITHUB_RELEASE_URL
                                )
                            )
                        }
                    },
                )

                // Check Update
                BasicComponent(
                    title = stringResource(RStrings.check_update),
                    endActions = {
                        if (hasNewVersion) {
                            Badge {
                                Text(
                                    text = stringResource(RStrings.new_version_available),
                                )
                            }
                        }
                    },
                    onClick = rememberThrottleClick {
                        VersionManager.checkUpdate(true)
                        showUpdateDialog = true
                    },
                )
            }
        }

        if (showUpdateDialog && latestVersionInfo != null) {
            val latestVersionInfo = latestVersionInfo!!
            val asset = latestVersionInfo.getCurrentFlavorAsset()
            val releaseNotes = remember(latestVersionInfo.body) {
                normalizeReleaseNotesLineEndings(latestVersionInfo.body.orEmpty())
            }
            OverlayDialog(
                show = true,
                title = asset?.name ?: latestVersionInfo.tagName,
                onDismissRequest = { showUpdateDialog = false },
                content = {
                    Column {
                        Markdown(
                            content = releaseNotes,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 400.dp)
                                .verticalScroll(rememberScrollState())
                        )
                        Spacer(Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            TextButton(
                                text = stringResource(RStrings.cancel),
                                onClick = { showUpdateDialog = false },
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(16.dp))
                            TextButton(
                                text = stringResource(RStrings.download),
                                onClick = click@{
                                    val url = asset?.downloadUrl
                                        ?: run {
                                            VersionManager.checkUpdate()
                                            uriHandler.openUri(Constants.GITHUB_RELEASE_URL)
                                            return@click
                                        }
                                    uriHandler.openUri(url)
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.textButtonColorsPrimary(),
                            )
                        }
                    }
                },
            )
        }
    }
}

// The Markdown parser requires LF line endings for tables and thematic breaks.
internal fun normalizeReleaseNotesLineEndings(content: String): String =
    content.replace("\r\n", "\n").replace('\r', '\n')
