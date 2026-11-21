package com.mrl.pixiv.common.compose.ui.glass

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.glass.GlassPopupItem

/**
 * 玻璃列表弹窗：在原地登记内容，由页面级浮层宿主渲染。
 *
 * @param show 是否展开。
 * @param onDismissRequest 点击遮罩或返回时关闭。
 * @param anchorBounds 触发控件在窗口中的位置，弹窗从这里展开。
 * @param content 弹窗内容，通常使用 [GlassPopupItem]。
 */
@Composable
fun GlassListPopup(
    show: Boolean,
    onDismissRequest: () -> Unit,
    anchorBounds: Rect,
    content: @Composable ColumnScope.() -> Unit,
) {
    val host = LocalGlassOverlayHost.current ?: return
    val key = remember { Any() }
    val currentDismiss by rememberUpdatedState(onDismissRequest)
    val currentContent by rememberUpdatedState(content)
    SideEffect {
        host.update(
            key = key,
            show = show,
            anchorBounds = anchorBounds,
            onDismissRequest = { currentDismiss() },
            content = { currentContent() },
        )
    }
    DisposableEffect(host, key) {
        onDispose { host.dismiss(key) }
    }
}

/**
 * 玻璃版 [top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu]。
 *
 * 外观与交互对齐 miuix 版本，但弹窗由玻璃浮层承载：条目使用 [GlassPopupItem]，
 * 选中项显示对勾，点击后按 [collapseOnSelection] 收起。
 */
@Composable
fun OverlayIconDropdownMenu(
    entries: List<DropdownEntry>,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    collapseOnSelection: Boolean = entries.size <= 1,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    val nonEmptyEntries = remember(entries) { entries.filter { it.items.isNotEmpty() } }
    val bounds = rememberGlassPopupBounds()
    var expanded by remember { mutableStateOf(false) }
    val actualEnabled = enabled && nonEmptyEntries.isNotEmpty()
    val setExpanded: (Boolean) -> Unit = { value ->
        if (expanded != value) {
            expanded = value
            onExpandedChange?.invoke(value)
        }
    }
    Box(modifier = modifier) {
        IconButton(
            onClick = { setExpanded(true) },
            enabled = actualEnabled,
            modifier = Modifier.glassPopupAnchorBounds(bounds),
            content = content,
        )
    }
    GlassListPopup(
        show = expanded,
        onDismissRequest = { setExpanded(false) },
        anchorBounds = bounds.value,
    ) {
        nonEmptyEntries.forEach { entry ->
            entry.items.forEach { item ->
                // 本项目未使用二级菜单，children 非空的条目保持不展开。
                if (item.children.isNullOrEmpty()) {
                    GlassPopupItem(
                        text = item.text,
                        onClick = {
                            item.onClick?.invoke()
                            if (collapseOnSelection) setExpanded(false)
                        },
                        enabled = entry.enabled && item.enabled,
                        selected = item.selected,
                        summary = item.summary,
                    )
                }
            }
        }
    }
}

/** 单组下拉菜单，等价于 miuix 的同名重载。 */
@Composable
fun OverlayIconDropdownMenu(
    entry: DropdownEntry,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit,
) {
    OverlayIconDropdownMenu(
        entries = remember(entry) { listOf(entry) },
        modifier = modifier,
        enabled = enabled,
        onExpandedChange = onExpandedChange,
        content = content,
    )
}
