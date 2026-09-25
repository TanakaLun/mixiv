package com.mrl.pixiv.common.compose.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Book
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import com.mrl.pixiv.common.data.AppViewMode
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.illust
import com.mrl.pixiv.strings.novel
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Image
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 视图模式切换按钮（顶栏 action）
 * 通过 miuix 下拉菜单在插画模式和小说模式之间切换
 *
 * @param currentMode 当前的视图模式
 * @param onModeChange 模式切换回调
 * @param modifier Modifier
 */
@Composable
fun ViewModeAction(
    currentMode: AppViewMode,
    onModeChange: (AppViewMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = AppViewMode.entries
    val labels = listOf(stringResource(RStrings.illust), stringResource(RStrings.novel))
    val selectedIndex = modes.indexOf(currentMode).coerceAtLeast(0)
    val entry = remember(currentMode, labels, onModeChange) {
        DropdownEntry(
            modes.mapIndexed { index, mode ->
                DropdownItem(
                    text = labels[index],
                    selected = index == selectedIndex,
                    onClick = { onModeChange(mode) },
                )
            }
        )
    }
    OverlayIconDropdownMenu(entry = entry, modifier = modifier) {
        Icon(
            imageVector = if (currentMode == AppViewMode.ILLUST) {
                MiuixIcons.Image
            } else {
                Icons.Rounded.Book
            },
            contentDescription = labels[selectedIndex],
            tint = MiuixTheme.colorScheme.onBackground,
        )
    }
}
