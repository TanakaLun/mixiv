package com.mrl.pixiv

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp

/**
 * 主页面底部玻璃导航栏覆盖正文的高度。
 *
 * 主页面正文需要延伸到导航栏下方，玻璃底才能采样到滚动内容；
 * 列表应把该值加到底部 `contentPadding`，让最后一项仍能滚动到导航栏上方。
 */
val LocalMainBottomContentPadding = compositionLocalOf { 0.dp }
