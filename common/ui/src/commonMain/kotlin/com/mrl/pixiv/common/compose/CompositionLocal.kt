package com.mrl.pixiv.common.compose

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.key.KeyEvent
import kotlinx.coroutines.flow.SharedFlow
import top.yukonga.miuix.kmp.basic.SnackbarHostState

val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> {
    noLocalProvidedFor("LocalSnackbarHostState")
}

val LocalKeyEventFlow = staticCompositionLocalOf<SharedFlow<KeyEvent>> {
    noLocalProvidedFor("LocalKeyEventFlow")
}

private fun noLocalProvidedFor(name: String): Nothing {
    error("CompositionLocal $name not present")
}
