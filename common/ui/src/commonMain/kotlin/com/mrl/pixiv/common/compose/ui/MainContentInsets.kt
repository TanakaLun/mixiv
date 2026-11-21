package com.mrl.pixiv.common.compose.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * 主页面底部玻璃导航栏覆盖正文的高度。
 *
 * 主页面正文需要延伸到导航栏下方，玻璃底才能采样到滚动内容；
 * 列表应把该值加到底部 `contentPadding`，让最后一项仍能滚动到导航栏上方。
 */
val LocalMainBottomContentPadding = compositionLocalOf { 0.dp }

/**
 * 主页面 Scaffold 的 `PaddingValues`（顶栏、系统栏与安全区）。
 *
 * 主页面正文不再整体内缩，而是把该值作为滚动容器的 `contentPadding`：
 * 内容因此会滚到玻璃顶栏/底栏下方，而不是被裁在栏外。
 */
val LocalMainContentInsets = compositionLocalOf { PaddingValues(0.dp) }
