package com.mrl.pixiv.common.compose.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import top.yukonga.miuix.kmp.glass.GlassPopup

/**
 * 玻璃弹窗的锚点位置（窗口坐标）。
 *
 * 与 miuix 的 `GlassPopupAnchor` 不同，这里只记录位置，不参与按钮形变，
 * 因此可以在任意模块里配合 [glassPopupAnchorBounds] 使用。
 */
@Stable
class GlassPopupBounds {
    var value: Rect by mutableStateOf(Rect.Zero)
        internal set
}

@Composable
fun rememberGlassPopupBounds(): GlassPopupBounds = remember { GlassPopupBounds() }

/** 把控件在窗口中的位置写入 [bounds]，供玻璃弹窗从控件处展开。 */
fun Modifier.glassPopupAnchorBounds(bounds: GlassPopupBounds): Modifier =
    onGloballyPositioned { bounds.value = it.boundsInWindow() }

internal class GlassOverlayEntry(
    val key: Any,
    val show: Boolean,
    val anchorBounds: Rect,
    val onDismissRequest: () -> Unit,
    val content: @Composable ColumnScope.() -> Unit,
)

/**
 * 页面级玻璃浮层宿主。
 *
 * `GlassPopup` 需要一整屏的父布局才能正确铺满遮罩并约束自身宽度，
 * 因此业务侧只在原地登记弹窗内容，由 [Scaffold] 在页面根节点统一渲染。
 * 同一时间只保留一个菜单，符合“点开新菜单会关闭旧菜单”的交互预期。
 */
@Stable
class GlassOverlayHost internal constructor() {
    internal var entry: GlassOverlayEntry? by mutableStateOf(null)
        private set

    internal fun update(
        key: Any,
        show: Boolean,
        anchorBounds: Rect,
        onDismissRequest: () -> Unit,
        content: @Composable ColumnScope.() -> Unit,
    ) {
        val current = entry
        when {
            current == null -> if (show) {
                entry = GlassOverlayEntry(key, show, anchorBounds, onDismissRequest, content)
            }

            current.key != key -> if (show) {
                entry = GlassOverlayEntry(key, show, anchorBounds, onDismissRequest, content)
            }

            current.show != show || current.anchorBounds != anchorBounds -> {
                entry = GlassOverlayEntry(key, show, anchorBounds, onDismissRequest, content)
            }
        }
    }

    internal fun dismiss(key: Any) {
        val current = entry ?: return
        if (current.key == key && current.show) {
            entry = GlassOverlayEntry(
                key = current.key,
                show = false,
                anchorBounds = current.anchorBounds,
                onDismissRequest = current.onDismissRequest,
                content = current.content,
            )
        }
    }

    internal fun remove(key: Any) {
        val current = entry ?: return
        if (current.key == key && !current.show) {
            entry = null
        }
    }
}

internal val LocalGlassOverlayHost = compositionLocalOf<GlassOverlayHost?> { null }

@Composable
internal fun GlassOverlayHostContent(host: GlassOverlayHost) {
    val entry = host.entry ?: return
    Box(modifier = Modifier.fillMaxSize()) {
        GlassPopup(
            show = entry.show,
            onDismissRequest = entry.onDismissRequest,
            anchorBounds = entry.anchorBounds,
            backdrop = LocalBodyBackdrop.current,
            content = entry.content,
        )
    }
}
