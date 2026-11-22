package com.mrl.pixiv.common.compose.ui

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 左滑删除容器，替代 Material3 的 `SwipeToDismissBox`。
 *
 * 只支持从右向左滑动：滑过 35% 宽度后松手删除，否则回弹。删除回调触发后由调用方移除数据。
 */
@Composable
fun SwipeToDeleteBox(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var width by remember { mutableIntStateOf(0) }
    var offset by remember { mutableFloatStateOf(0f) }
    val scope = rememberCoroutineScope()
    val threshold = width * 0.35f

    Box(
        modifier = modifier
            .clipToBounds()
            .onSizeChanged { width = it.width },
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(MiuixTheme.colorScheme.error),
            contentAlignment = Alignment.CenterEnd,
        ) {
            Icon(
                imageVector = MiuixIcons.Delete,
                contentDescription = null,
                modifier = Modifier.padding(end = 20.dp),
                tint = MiuixTheme.colorScheme.onError,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offset.roundToInt(), 0) }
                .pointerInput(width) {
                    if (width == 0) return@pointerInput
                    detectHorizontalDragGestures(
                        onHorizontalDrag = { change, dragAmount ->
                            change.consume()
                            offset = (offset + dragAmount).coerceIn(-width.toFloat(), 0f)
                        },
                        onDragEnd = {
                            scope.launch {
                                if (offset <= -threshold) {
                                    animate(
                                        initialValue = offset,
                                        targetValue = -width.toFloat(),
                                        animationSpec = spring(stiffness = 800f),
                                    ) { value, _ ->
                                        offset = value
                                    }
                                    onDelete()
                                } else {
                                    animate(
                                        initialValue = offset,
                                        targetValue = 0f,
                                        animationSpec = spring(stiffness = 800f),
                                    ) { value, _ ->
                                        offset = value
                                    }
                                }
                            }
                        },
                        onDragCancel = {
                            scope.launch {
                                animate(
                                        initialValue = offset,
                                        targetValue = 0f,
                                        animationSpec = spring(stiffness = 800f),
                                    ) { value, _ ->
                                    offset = value
                                }
                            }
                        },
                    )
                },
        ) {
            content()
        }
    }
}
