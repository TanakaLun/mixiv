package com.mrl.pixiv.setting

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.NetworkWifi
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.compose.rememberThrottleClick
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.data.setting.SettingTheme
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.SettingRepository.collectAsStateWithLifecycle
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.isPlatformDynamicColorSupported
import com.mrl.pixiv.strings.ai_translation_setting
import com.mrl.pixiv.strings.app_language
import com.mrl.pixiv.strings.browsing_setting
import com.mrl.pixiv.strings.color_mode
import com.mrl.pixiv.strings.file_name_format_title
import com.mrl.pixiv.strings.history_setting
import com.mrl.pixiv.strings.label_default
import com.mrl.pixiv.strings.monet_dark
import com.mrl.pixiv.strings.monet_light
import com.mrl.pixiv.strings.monet_system
import com.mrl.pixiv.strings.network_setting
import com.mrl.pixiv.strings.privacy_setting
import com.mrl.pixiv.strings.search_setting
import com.mrl.pixiv.strings.setting
import com.mrl.pixiv.strings.theme_dark
import com.mrl.pixiv.strings.theme_light
import com.mrl.pixiv.strings.theme_system
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference

const val KEY_LANGUAGE = "language"
const val KEY_NETWORK_SETTING = "network_setting"
const val KEY_BROWSING_SETTING = "browsing_setting"
const val KEY_SEARCH_SETTING = "search_setting"
const val KEY_HISTORY_SETTING = "history_setting"
const val KEY_PRIVACY_SETTING = "privacy_setting"
const val KEY_FILE_NAME_FORMAT = "file_name_format"
const val KEY_AI_TRANSLATION_SETTING = "ai_translation_setting"

@Composable
fun SettingScreen(
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val labelDefault = stringResource(RStrings.label_default)
    val languages = remember { getLanguages() }
    var currentLanguage by remember(labelDefault) {
        mutableIntStateOf(
            languages.indexOfFirst { it.langTag == (getInitialLanguages() ?: labelDefault) }
                .coerceAtLeast(0)
        )
    }

    LaunchedEffect(currentLanguage, labelDefault) {
        val language = languages.getOrNull(currentLanguage) ?: return@LaunchedEffect
        triggerLocaleChange(language.langTag, labelDefault)
    }

    val scrollBehavior = MiuixScrollBehavior()
    val themeName by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle { theme }
    val monetEnabled by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle { monet }
    val settingTheme = remember(themeName) {
        SettingTheme.entries.firstOrNull { it.name == themeName } ?: SettingTheme.SYSTEM
    }
    val colorModeItems = if (isPlatformDynamicColorSupported) {
        listOf(
            stringResource(RStrings.theme_system),
            stringResource(RStrings.theme_light),
            stringResource(RStrings.theme_dark),
            stringResource(RStrings.monet_system),
            stringResource(RStrings.monet_light),
            stringResource(RStrings.monet_dark),
        )
    } else {
        listOf(
            stringResource(RStrings.theme_system),
            stringResource(RStrings.theme_light),
            stringResource(RStrings.theme_dark),
        )
    }
    val baseColorMode = when (settingTheme) {
        SettingTheme.SYSTEM -> 0
        SettingTheme.LIGHT -> 1
        SettingTheme.DARK -> 2
    }
    val selectedColorMode =
        if (monetEnabled && isPlatformDynamicColorSupported) baseColorMode + 3 else baseColorMode
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.setting),
                navigationIcon = {
                    IconButton(onClick = navigationManager::popBackStack) {
                        Icon(MiuixIcons.Back, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .pageScrollModifiers(scrollBehavior),
        ) {
            item(key = KEY_LANGUAGE) {
                Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                    OverlayDropdownPreference(
                        items = languages.map { it.displayName },
                        selectedIndex = currentLanguage,
                        title = stringResource(RStrings.app_language),
                        startAction = { Icon(Icons.Rounded.Translate, contentDescription = null) },
                        onSelectedIndexChange = { currentLanguage = it },
                    )
                    OverlayDropdownPreference(
                        items = colorModeItems,
                        selectedIndex = selectedColorMode,
                        title = stringResource(RStrings.color_mode),
                        startAction = { Icon(Icons.Rounded.Palette, contentDescription = null) },
                        onSelectedIndexChange = { index ->
                            val theme = when (index % 3) {
                                0 -> SettingTheme.SYSTEM
                                1 -> SettingTheme.LIGHT
                                else -> SettingTheme.DARK
                            }
                            SettingRepository.setSettingTheme(theme)
                            SettingRepository.setMonetEnabled(
                                isPlatformDynamicColorSupported && index >= 3
                            )
                        },
                    )
                    SettingDestinationItem(
                        title = stringResource(RStrings.network_setting),
                        icon = Icons.Rounded.NetworkWifi,
                        onClick = navigationManager::navigateToNetworkSettingScreen,
                    )
                    SettingDestinationItem(
                        title = stringResource(RStrings.browsing_setting),
                        icon = Icons.Rounded.Image,
                        onClick = navigationManager::navigateToBrowsingSettingScreen,
                    )
                    SettingDestinationItem(
                        title = stringResource(RStrings.search_setting),
                        icon = Icons.Rounded.Search,
                        onClick = navigationManager::navigateToSearchSettingScreen,
                    )
                    SettingDestinationItem(
                        title = stringResource(RStrings.history_setting),
                        icon = Icons.Rounded.History,
                        onClick = navigationManager::navigateToHistorySettingScreen,
                    )
                    SettingDestinationItem(
                        title = stringResource(RStrings.privacy_setting),
                        icon = Icons.Rounded.Lock,
                        onClick = navigationManager::navigateToPrivacySettingScreen,
                    )
                    SettingDestinationItem(
                        title = stringResource(RStrings.file_name_format_title),
                        icon = Icons.Rounded.Save,
                        onClick = navigationManager::navigateToFileNameFormatScreen,
                    )
                    SettingDestinationItem(
                        title = stringResource(RStrings.ai_translation_setting),
                        icon = Icons.Rounded.Translate,
                        onClick = navigationManager::navigateToAiTranslationSettingScreen,
                    )
                    AppLinkItem()
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}

@Composable
private fun SettingDestinationItem(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    ArrowPreference(
        title = title,
        startAction = { Icon(imageVector = icon, contentDescription = null) },
        onClick = rememberThrottleClick(onClick = onClick),
    )
}

expect fun getInitialLanguages(): String?

expect fun triggerLocaleChange(
    currentLanguage: String,
    labelDefault: String,
)

@Composable
expect fun AppLinkItem()
