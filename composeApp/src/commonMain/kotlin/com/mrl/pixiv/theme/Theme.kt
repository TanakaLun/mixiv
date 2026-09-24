package com.mrl.pixiv.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.expressiveLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.mrl.pixiv.common.data.setting.SettingTheme
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.SettingRepository.collectAsStateWithLifecycle
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun PiPixivTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorScheme: ColorScheme = if (darkTheme) darkColorScheme() else expressiveLightColorScheme(),
    content: @Composable () -> Unit,
) {
    val themeMode by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle { theme }
    val settingTheme = remember(themeMode) {
        SettingTheme.entries.firstOrNull { it.name == themeMode } ?: SettingTheme.SYSTEM
    }
    val controller = remember(settingTheme) {
        when (settingTheme) {
            SettingTheme.LIGHT -> ThemeController(ColorSchemeMode.MonetLight)
            SettingTheme.DARK -> ThemeController(ColorSchemeMode.MonetDark)
            SettingTheme.SYSTEM -> ThemeController(ColorSchemeMode.MonetSystem)
        }
    }
    MiuixTheme(controller = controller) {
        MaterialExpressiveTheme(
            colorScheme = colorScheme,
            content = content,
        )
    }
}
