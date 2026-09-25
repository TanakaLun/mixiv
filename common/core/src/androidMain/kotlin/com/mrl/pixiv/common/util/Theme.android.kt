package com.mrl.pixiv.common.util

import android.os.Build
import androidx.appcompat.app.AppCompatDelegate
import com.mrl.pixiv.common.data.setting.SettingTheme

actual val isPlatformDynamicColorSupported: Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

actual fun setAppCompatDelegateThemeMode(theme: SettingTheme) {
    AppCompatDelegate.setDefaultNightMode(
        when (theme) {
            SettingTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            SettingTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            SettingTheme.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
    )
}