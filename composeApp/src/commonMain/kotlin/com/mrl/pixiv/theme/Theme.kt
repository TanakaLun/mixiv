package com.mrl.pixiv.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.mrl.pixiv.common.data.setting.SettingTheme
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.SettingRepository.collectAsStateWithLifecycle
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.Colors
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
    val controller = remember(settingTheme, monetEnabled) {
        when (settingTheme) {
            SettingTheme.LIGHT ->
                if (monetEnabled) ThemeController(ColorSchemeMode.MonetLight)
                else ThemeController(ColorSchemeMode.Light)
            SettingTheme.DARK ->
                if (monetEnabled) ThemeController(ColorSchemeMode.MonetDark)
                else ThemeController(ColorSchemeMode.Dark)
            SettingTheme.SYSTEM ->
                if (monetEnabled) ThemeController(ColorSchemeMode.MonetSystem)
                else ThemeController(ColorSchemeMode.System)
        }
    }
    MiuixTheme(controller = controller) {
        // Thin color bridge so retained material3 widgets (DatePicker, FilterChip,
        // SwipeToDismissBox) follow the miuix scheme instead of m3 light defaults.
        val colors = MiuixTheme.colorScheme
        val dark = when (MiuixTheme.colorSchemeMode) {
            ColorSchemeMode.Light, ColorSchemeMode.MonetLight -> false
            ColorSchemeMode.Dark, ColorSchemeMode.MonetDark -> true
            else -> isSystemInDarkTheme()
        }
        val legacyMaterialColors = remember(colors, dark) { colors.toLegacyMaterialColors(dark) }
        MaterialTheme(colorScheme = legacyMaterialColors, content = content)
    }
}

private fun Colors.toLegacyMaterialColors(dark: Boolean): ColorScheme {
    val base = if (dark) darkColorScheme() else lightColorScheme()
    return base.copy(
        primary = primary,
        onPrimary = onPrimary,
        primaryContainer = primaryContainer,
        onPrimaryContainer = onPrimaryContainer,
        secondary = secondary,
        onSecondary = onSecondary,
        secondaryContainer = secondaryContainer,
        onSecondaryContainer = onSecondaryContainer,
        background = background,
        onBackground = onBackground,
        surface = surface,
        onSurface = onSurface,
        surfaceVariant = surfaceVariant,
        onSurfaceVariant = onSurfaceVariantSummary,
        outline = outline,
        error = error,
        onError = onError,
        errorContainer = errorContainer,
        onErrorContainer = onErrorContainer,
    )
}
