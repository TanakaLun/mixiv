package com.mrl.pixiv.common.compose.ui.markdown

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.DefaultMarkdownColors
import com.mikepenz.markdown.model.DefaultMarkdownTypography
import com.mikepenz.markdown.model.markdownAlertColors
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Markdown 渲染器，配色与排版取自 miuix 主题，替代 `markdown-m3` 的 Material3 版本。
 */
@Composable
fun MiuixMarkdown(
    content: String,
    modifier: Modifier = Modifier,
) {
    val colorScheme = MiuixTheme.colorScheme
    val textStyles = MiuixTheme.textStyles
    val colors = DefaultMarkdownColors(
            text = colorScheme.onBackground,
            codeBackground = colorScheme.onBackground.copy(alpha = 0.1f),
            inlineCodeBackground = colorScheme.onBackground.copy(alpha = 0.1f),
            dividerColor = colorScheme.outline,
            tableBackground = colorScheme.onBackground.copy(alpha = 0.02f),
            alert = markdownAlertColors(colorScheme.background.luminance() < 0.5f),
        highlightText = colorScheme.onSecondaryContainer,
        highlightBackground = colorScheme.secondaryContainer,
    )
    val typography = run {
        val body = textStyles.body1
        DefaultMarkdownTypography(
            h1 = textStyles.headline1,
            h2 = textStyles.headline2,
            h3 = textStyles.title1,
            h4 = textStyles.title2,
            h5 = textStyles.title3,
            h6 = textStyles.title4,
            text = body,
            quote = body.copy(fontStyle = FontStyle.Italic),
            code = textStyles.body2.copy(fontFamily = FontFamily.Monospace),
            inlineCode = textStyles.body2.copy(fontFamily = FontFamily.Monospace),
            paragraph = body,
            ordered = body,
            bullet = body,
            list = body,
            textLink = TextLinkStyles(
                style = SpanStyle(
                    color = colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    textDecoration = TextDecoration.Underline,
                ),
            ),
            table = textStyles.body2,
            alertTitle = body.copy(fontWeight = FontWeight.Bold),
        )
    }
    Markdown(
        content = content,
        colors = colors,
        typography = typography,
        modifier = modifier,
    )
}
