package com.mrl.pixiv.profile.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.mrl.pixiv.common.util.throttleClick
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ExpandMore

/**
 * 压暗遮罩 + 居中 [ExpandMore] 图标的"查看更多"入口。需要在 [Box] 作用域内调用，
 * 默认遮罩铺满父容器（配合 [androidx.compose.foundation.layout.BoxScope.matchParentSize]）。
 */
@Composable
internal fun BoxScope.MoreOverlayCover(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.35f))
            .throttleClick(onClick = onClick),
    )
    IconButton(
        onClick = onClick,
        modifier = Modifier.align(Alignment.Center),
    ) {
        Icon(
            imageVector = MiuixIcons.ExpandMore,
            contentDescription = null,
            tint = Color.White,
        )
    }
}