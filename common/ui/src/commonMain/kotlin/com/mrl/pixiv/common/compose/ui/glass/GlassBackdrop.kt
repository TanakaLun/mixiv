package com.mrl.pixiv.common.compose.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 页面正文的背景采样，供顶栏、底栏与标签栏读取。
 * 只录制正文内容，不包含消费它的玻璃组件本身。
 */
val LocalBodyBackdrop = compositionLocalOf<LayerBackdrop?> { null }

/**
 * 创建页面正文的背景采样；设备不支持运行时着色器时返回 null，
 * 玻璃组件会自动回退为纯色底。
 */
@Composable
fun rememberGlassBackdrop(
    color: Color = MiuixTheme.colorScheme.surface,
): LayerBackdrop? {
    val supported = remember { isRuntimeShaderSupported() }
    if (!supported) return null
    return rememberLayerBackdrop {
        drawRect(color)
        drawContent()
    }
}