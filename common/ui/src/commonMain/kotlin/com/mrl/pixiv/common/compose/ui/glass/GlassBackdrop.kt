package com.mrl.pixiv.common.compose.ui.glass

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.material.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 页面正文的背景采样，供顶栏、底栏读取。
 * 只录制正文内容，不包含消费它的玻璃组件本身。
 */
val LocalBodyBackdrop = compositionLocalOf<LayerBackdrop?> { null }

/**
 * 只录制页面底色、不含任何内容的静态采样。
 *
 * 供位于正文内部的玻璃组件（分段标签栏、正文里的图标按钮）使用：
 * 这些组件本身属于 [LocalBodyBackdrop] 的录制范围，再采样正文会形成
 * RenderNode 自引用并导致渲染线程栈溢出。
 */
val LocalStaticBackdrop = compositionLocalOf<LayerBackdrop?> { null }

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

/** 创建只含页面底色的静态采样，可安全地被正文内部组件消费。 */
@Composable
fun rememberStaticGlassBackdrop(
    color: Color = MiuixTheme.colorScheme.surface,
): LayerBackdrop? {
    val supported = remember { isRuntimeShaderSupported() }
    if (!supported) return null
    val currentColor by rememberUpdatedState(color)
    return rememberLayerBackdrop {
        drawRect(currentColor)
    }
}
