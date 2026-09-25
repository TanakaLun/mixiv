package com.mrl.pixiv.setting

import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AddLink
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import com.mrl.pixiv.common.compose.rememberThrottleClick
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.allow_open_link
import com.mrl.pixiv.strings.default_open
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.preference.ArrowPreference

actual fun getInitialLanguages(): String? {
    return AppCompatDelegate.getApplicationLocales().get(0)?.toLanguageTag()
}

actual fun triggerLocaleChange(currentLanguage: String, labelDefault: String) {
    val locale = if (currentLanguage == labelDefault) {
        LocaleListCompat.getEmptyLocaleList()
    } else {
        LocaleListCompat.forLanguageTags(currentLanguage)
    }
    AppCompatDelegate.setApplicationLocales(locale)
}

@Composable
actual fun AppLinkItem() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        ArrowPreference(
            title = stringResource(RStrings.default_open),
            summary = stringResource(RStrings.allow_open_link),
            startAction = {
                Icon(imageVector = Icons.Rounded.AddLink, contentDescription = null)
            },
            onClick = rememberThrottleClick {
                try {
                    val intent = Intent().apply {
                        action = Settings.ACTION_APP_OPEN_BY_DEFAULT_SETTINGS
                        addCategory(Intent.CATEGORY_DEFAULT)
                        data = "package:${context.packageName}".toUri()
                        addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                        addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                    }
                    context.startActivity(intent)
                } catch (_: Throwable) {
                }
            },
        )
    }
}
