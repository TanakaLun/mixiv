package com.mrl.pixiv.common.util

import com.mrl.pixiv.common.data.setting.SettingTheme

expect fun setAppCompatDelegateThemeMode(theme: SettingTheme)

/**
 * Whether the platform provides dynamic (wallpaper) colors.
 *
 * Below Android 12 there is no platform color source, so the Monet color modes
 * can only fall back to a fixed seed; the app then uses miuix's default theme colors.
 */
expect val isPlatformDynamicColorSupported: Boolean