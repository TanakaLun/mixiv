package com.mrl.pixiv.novel

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.systemBarsIgnoringVisibility
import androidx.compose.runtime.Composable

@Composable
internal actual fun novelReaderContentInsets(): WindowInsets =
    WindowInsets.systemBarsIgnoringVisibility
