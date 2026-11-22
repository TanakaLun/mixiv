package com.mrl.pixiv.novel.series

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.repository.NovelSeriesReadingProgress
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.novel_series_continue_reading
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun NovelSeriesContinueReadingCard(
    progress: NovelSeriesReadingProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val percent = (progress.fraction * 100).toInt().coerceIn(0, 100)
    Card(
        onClick = onClick,
        modifier = modifier,
        insideMargin = PaddingValues(16.dp),
        colors = CardDefaults.defaultColors(
            color = MiuixTheme.colorScheme.secondaryContainer,
            contentColor = MiuixTheme.colorScheme.onSecondaryContainer,
        ),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // 操作与进度独立显示，避免长章节标题撑大按钮或挤走百分比。
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(RStrings.novel_series_continue_reading),
                    modifier = Modifier.weight(1f),
                    style = MiuixTheme.textStyles.title4,
                )
                Text(
                    text = "$percent%",
                    style = MiuixTheme.textStyles.footnote1,
                    maxLines = 1,
                )
            }
            Text(
                text = progress.title,
                style = MiuixTheme.textStyles.body2,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
