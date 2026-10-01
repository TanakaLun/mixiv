package com.mrl.pixiv.common.compose.ui

import androidx.compose.runtime.Composable
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.pull_refresh_complete
import com.mrl.pixiv.strings.pull_refresh_pull_down
import com.mrl.pixiv.strings.pull_refresh_refreshing
import com.mrl.pixiv.strings.pull_refresh_release
import org.jetbrains.compose.resources.stringResource

@Composable
fun rememberRefreshTexts(): List<String> = listOf(
    stringResource(RStrings.pull_refresh_pull_down),
    stringResource(RStrings.pull_refresh_release),
    stringResource(RStrings.pull_refresh_refreshing),
    stringResource(RStrings.pull_refresh_complete),
)
