package com.mrl.pixiv.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.mrl.pixiv.common.data.setting.SettingTheme
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.SettingRepository.collectAsStateWithLifecycle
import com.mrl.pixiv.common.util.isPlatformDynamicColorSupported
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@Composable
fun PiPixivTheme(
    content: @Composable () -> Unit,
) {
    val themeMode by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle { theme }
    val monetEnabled by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle { monet }
    val settingTheme = remember(themeMode) {
        SettingTheme.entries.firstOrNull { it.name == themeMode } ?: SettingTheme.SYSTEM
    }
    // Below Android 12 there are no platform color sources, so the Monet modes would
    // only produce a fixed seed; let miuix fall back to its default theme colors instead.
    val useMonet = monetEnabled && isPlatformDynamicColorSupported
    val controller = remember(settingTheme, useMonet) {
        when (settingTheme) {
            SettingTheme.LIGHT ->
                if (useMonet) ThemeController(ColorSchemeMode.MonetLight)
                else ThemeController(ColorSchemeMode.Light)
            SettingTheme.DARK ->
                if (useMonet) ThemeController(ColorSchemeMode.MonetDark)
                else ThemeController(ColorSchemeMode.Dark)
            SettingTheme.SYSTEM ->
                if (useMonet) ThemeController(ColorSchemeMode.MonetSystem)
                else ThemeController(ColorSchemeMode.System)
        }
    }
    MiuixTheme(controller = controller, content = content)
}
