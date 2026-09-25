package com.mrl.pixiv.common.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.StringResource

object ToastUtil : CoroutineScope by MainScope() {
    private val _toastFlow = Channel<String>(Channel.BUFFERED)
    val toastFlow: Flow<String> = _toastFlow.receiveAsFlow()

    fun safeShortToast(strId: StringResource, vararg params: Any) {
        val text = AppUtil.getString(strId, *params)
        launch {
            _toastFlow.send(text)
        }
    }

    fun safeShortToast(message: Any) {
        launch {
            _toastFlow.trySend(message.toToastText())
        }
    }

    private fun Any.toToastText(): String = when (this) {
        is String -> this
        is StringResource -> AppUtil.getString(this)
        else -> toString()
    }
}
