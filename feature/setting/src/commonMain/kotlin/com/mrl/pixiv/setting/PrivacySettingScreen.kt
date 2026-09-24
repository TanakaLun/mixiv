package com.mrl.pixiv.setting

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentPaste
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded._18UpRating
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import be.digitalia.compose.htmlconverter.htmlToAnnotatedString
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.cancel
import com.mrl.pixiv.strings.confirm
import com.mrl.pixiv.strings.default_private_bookmark
import com.mrl.pixiv.strings.privacy_setting
import com.mrl.pixiv.strings.r18
import com.mrl.pixiv.strings.r18_alert_message
import com.mrl.pixiv.strings.read_clipboard_on_search
import com.mrl.pixiv.strings.read_clipboard_on_search_desc
import com.mrl.pixiv.strings.tips
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.ButtonDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun PrivacySettingScreen(
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val userPreference by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle()
    var showR18Warning by rememberSaveable { mutableStateOf(false) }
    val tipText = stringResource(RStrings.r18_alert_message)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.privacy_setting),
                navigationIcon = {
                    IconButton(onClick = navigationManager::popBackStack) {
                        Icon(MiuixIcons.Back, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(horizontal = 8.dp),
        ) {
            SwitchPreference(
                checked = userPreference.isR18Enabled,
                onCheckedChange = { checked ->
                    if (checked) showR18Warning = true
                    else SettingRepository.setIsR18Enabled(false)
                },
                title = stringResource(RStrings.r18),
                startAction = { Icon(Icons.Rounded._18UpRating, contentDescription = null) },
            )
            SwitchPreference(
                checked = userPreference.defaultPrivateBookmark,
                onCheckedChange = SettingRepository::setDefaultPrivateBookmark,
                title = stringResource(RStrings.default_private_bookmark),
                startAction = { Icon(Icons.Rounded.Favorite, contentDescription = null) },
            )
            SwitchPreference(
                checked = userPreference.readClipboardOnSearch,
                onCheckedChange = SettingRepository::setReadClipboardOnSearch,
                title = stringResource(RStrings.read_clipboard_on_search),
                summary = stringResource(RStrings.read_clipboard_on_search_desc),
                startAction = { Icon(Icons.Rounded.ContentPaste, contentDescription = null) },
            )
        }
    }

    OverlayDialog(
        show = showR18Warning,
        title = stringResource(RStrings.tips),
        onDismissRequest = { showR18Warning = false },
        content = {
            Column {
                Text(text = remember(tipText) { htmlToAnnotatedString(tipText) })
                Spacer(Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(
                        text = stringResource(RStrings.cancel),
                        onClick = { showR18Warning = false },
                        modifier = Modifier.weight(1f),
                    )
                    Spacer(Modifier.width(16.dp))
                    TextButton(
                        text = stringResource(RStrings.confirm),
                        onClick = {
                            SettingRepository.setIsR18Enabled(true)
                            showR18Warning = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.textButtonColorsPrimary(),
                    )
                }
            }
        },
    )
}
