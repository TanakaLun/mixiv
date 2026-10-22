package com.mrl.pixiv.components.date

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.select_date
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.time.Clock

/** 排行榜/搜索结果可选的日期下界（pixiv 排行历史足够早）。 */
internal const val DatePickerMinYear = 2015

@Composable
internal fun MiuixDatePickerSheet(
    show: Boolean,
    initialDate: LocalDate?,
    onDismissRequest: () -> Unit,
    onConfirm: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)
    var year by remember(show, initialDate) {
        mutableIntStateOf(initialDate?.year ?: yesterday.year)
    }
    var month by remember(show, initialDate) {
        mutableIntStateOf(initialDate?.monthNumber ?: yesterday.monthNumber)
    }
    var day by remember(show, initialDate) {
        mutableIntStateOf(initialDate?.dayOfMonth ?: yesterday.dayOfMonth)
    }
    val maxMonth = remember(year) {
        if (year >= today.year) today.monthNumber else 12
    }
    val maxDay = remember(year, month) {
        if (year == today.year && month == today.monthNumber) {
            yesterday.dayOfMonth
        } else {
            LocalDate(year, month, 1).lengthOfMonth()
        }
    }
    LaunchedEffect(maxMonth) {
        if (month > maxMonth) month = maxMonth
    }
    LaunchedEffect(maxDay) {
        if (day > maxDay) day = maxDay
    }

    OverlayBottomSheet(
        show = show,
        title = stringResource(RStrings.select_date),
        startAction = {
            IconButton(onClick = onDismissRequest) {
                Icon(MiuixIcons.Close, contentDescription = null)
            }
        },
        endAction = {
            IconButton(onClick = { onConfirm(LocalDate(year, month, day)) }) {
                Icon(MiuixIcons.Ok, contentDescription = null)
            }
        },
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            NumberPicker(
                value = year,
                onValueChange = { year = it },
                range = DatePickerMinYear..today.year,
                wrapAround = false,
                label = { it.toString().padStart(2, '0') },
                modifier = Modifier.weight(1f),
            )
            DateUnitText("年")
            NumberPicker(
                value = month,
                onValueChange = { month = it },
                range = 1..maxMonth,
                wrapAround = false,
                label = { it.toString().padStart(2, '0') },
                modifier = Modifier.weight(1f),
            )
            DateUnitText("月")
            NumberPicker(
                value = day,
                onValueChange = { day = it },
                range = 1..maxDay,
                wrapAround = false,
                label = { it.toString().padStart(2, '0') },
                modifier = Modifier.weight(1f),
            )
            DateUnitText("日")
        }
    }
}

@Composable
internal fun DateUnitText(text: String) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.title2,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(horizontal = 2.dp),
    )
}