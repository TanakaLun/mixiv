// Adapted from compose-coui-ui (io.github.suqi8.coui.kmp.basic.Chip)
// Copyright 2026, compose-coui-ui contributors
// SPDX-License-Identifier: Apache-2.0
package com.mrl.pixiv.common.compose.ui.chip

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Indication
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 独立实现的 ColorOS 风格 chip（转录自 COUI 的 `Chip` / `SuggestionChip`）。
 *
 * 胶囊形、可选中的筛选标签：未选中为半透明底色 + 常规文字，选中为 primary 填充 +
 * onPrimary 文字，颜色切换带过渡；按下有轻微缩放反馈。支持前置图标与尾部计数。
 */
@Composable
fun Chip(
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    count: String? = null,
    cornerRadius: Dp = ChipDefaults.CornerRadius,
    minWidth: Dp = ChipDefaults.MinWidth,
    minHeight: Dp = ChipDefaults.MinHeight,
    maxWidth: Dp = ChipDefaults.MaxWidth,
    colors: ChipColors = ChipDefaults.chipColors(),
    insideMargin: PaddingValues = ChipDefaults.InsideMargin,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
) {
    ChipImpl(
        selectable = true,
        selected = selected,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        count = count,
        cornerRadius = cornerRadius,
        minWidth = minWidth,
        minHeight = minHeight,
        maxWidth = maxWidth,
        colors = colors,
        insideMargin = insideMargin,
        textStyle = ChipDefaults.textStyle(),
        interactionSource = interactionSource,
        indication = indication,
    )
}

/**
 * 独立实现的 ColorOS 风格建议 chip：用于“动作/占位符/标签”，没有选中态。
 */
@Composable
fun SuggestionChip(
    onClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: (@Composable () -> Unit)? = null,
    count: String? = null,
    cornerRadius: Dp = ChipDefaults.SuggestionCornerRadius,
    minWidth: Dp = ChipDefaults.MinWidth,
    minHeight: Dp = ChipDefaults.SuggestionMinHeight,
    maxWidth: Dp = ChipDefaults.MaxWidth,
    colors: ChipColors = ChipDefaults.chipColors(),
    insideMargin: PaddingValues = ChipDefaults.InsideMargin,
    interactionSource: MutableInteractionSource? = null,
    indication: Indication? = null,
) {
    ChipImpl(
        selectable = false,
        selected = false,
        onClick = onClick,
        label = label,
        modifier = modifier,
        enabled = enabled,
        icon = icon,
        count = count,
        cornerRadius = cornerRadius,
        minWidth = minWidth,
        minHeight = minHeight,
        maxWidth = maxWidth,
        colors = colors,
        insideMargin = insideMargin,
        textStyle = ChipDefaults.suggestionTextStyle(),
        interactionSource = interactionSource,
        indication = indication,
    )
}

@Composable
private fun ChipImpl(
    selectable: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    label: String,
    modifier: Modifier,
    enabled: Boolean,
    icon: (@Composable () -> Unit)?,
    count: String?,
    cornerRadius: Dp,
    minWidth: Dp,
    minHeight: Dp,
    maxWidth: Dp,
    colors: ChipColors,
    insideMargin: PaddingValues,
    textStyle: TextStyle,
    interactionSource: MutableInteractionSource?,
    indication: Indication?,
) {
    val interaction = interactionSource ?: remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed && enabled) 0.94f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "chipScale",
    )
    val targetContainer = when {
        !enabled && selected -> colors.selectedDisabledContainerColor
        !enabled -> colors.disabledContainerColor
        selected -> colors.selectedContainerColor
        else -> colors.containerColor
    }
    val targetContent = when {
        !enabled && selected -> colors.selectedDisabledContentColor
        !enabled -> colors.disabledContentColor
        selected -> colors.selectedContentColor
        else -> colors.contentColor
    }
    val containerColor by animateColorAsState(targetContainer, tween(150), label = "chipContainer")
    val contentColor by animateColorAsState(targetContent, tween(150), label = "chipContent")
    val resolvedIndication = indication ?: LocalIndication.current
    val clickModifier = if (selectable) {
        Modifier.selectable(
            selected = selected,
            enabled = enabled,
            role = Role.Checkbox,
            interactionSource = interaction,
            indication = resolvedIndication,
            onClick = onClick,
        )
    } else {
        Modifier.clickable(
            enabled = enabled,
            role = Role.Button,
            interactionSource = interaction,
            indication = resolvedIndication,
            onClick = onClick,
        )
    }
    Row(
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .widthIn(min = minWidth, max = maxWidth)
            .heightIn(min = minHeight)
            .clip(RoundedCornerShape(cornerRadius))
            .background(containerColor)
            .then(clickModifier)
            .padding(insideMargin)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        if (icon != null) {
            Box(
                modifier = Modifier.size(ChipDefaults.IconSize),
                contentAlignment = Alignment.Center,
            ) {
                CompositionLocalProvider(LocalContentColor provides contentColor, content = icon)
            }
        }
        Text(
            text = label,
            modifier = Modifier.widthIn(max = ChipDefaults.TextMaxWidth),
            color = contentColor,
            style = textStyle,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (count != null) {
            Text(
                text = count,
                color = colors.countColor,
                style = MiuixTheme.textStyles.footnote2,
                maxLines = 1,
            )
        }
    }
}

@Immutable
data class ChipColors(
    val containerColor: Color,
    val selectedContainerColor: Color,
    val disabledContainerColor: Color,
    val selectedDisabledContainerColor: Color,
    val contentColor: Color,
    val selectedContentColor: Color,
    val disabledContentColor: Color,
    val selectedDisabledContentColor: Color,
    val countColor: Color,
)

object ChipDefaults {
    val MinWidth = 52.dp
    val MinHeight = 32.dp
    val MaxWidth = 300.dp
    val TextMaxWidth = 200.dp
    val CornerRadius = 16.dp
    val SuggestionMinHeight = 28.dp
    val SuggestionCornerRadius = 14.dp
    val InsideMargin = PaddingValues(horizontal = 12.dp)
    val IconSize = 16.dp

    @Composable
    fun textStyle() = MiuixTheme.textStyles.body2

    @Composable
    fun suggestionTextStyle() = MiuixTheme.textStyles.footnote1

    @Composable
    fun chipColors(
        containerColor: Color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        selectedContainerColor: Color = MiuixTheme.colorScheme.primary,
        disabledContainerColor: Color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.04f),
        selectedDisabledContainerColor: Color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.08f),
        contentColor: Color = MiuixTheme.colorScheme.onSurface,
        selectedContentColor: Color = MiuixTheme.colorScheme.onPrimary,
        disabledContentColor: Color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        selectedDisabledContentColor: Color = MiuixTheme.colorScheme.onSurface.copy(alpha = 0.38f),
        countColor: Color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
    ): ChipColors = remember(
        containerColor,
        selectedContainerColor,
        disabledContainerColor,
        selectedDisabledContainerColor,
        contentColor,
        selectedContentColor,
        disabledContentColor,
        selectedDisabledContentColor,
        countColor,
    ) {
        ChipColors(
            containerColor = containerColor,
            selectedContainerColor = selectedContainerColor,
            disabledContainerColor = disabledContainerColor,
            selectedDisabledContainerColor = selectedDisabledContainerColor,
            contentColor = contentColor,
            selectedContentColor = selectedContentColor,
            disabledContentColor = disabledContentColor,
            selectedDisabledContentColor = selectedDisabledContentColor,
            countColor = countColor,
        )
    }
}
