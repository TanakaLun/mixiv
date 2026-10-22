package com.mrl.pixiv.components.date

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.mrl.pixiv.strings.clear_date_range
import com.mrl.pixiv.strings.date_range
import com.mrl.pixiv.strings.end_date
import com.mrl.pixiv.strings.start_date
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.lengthOfMonth
import kotlinx.datetime.minus
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.NumberPicker
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.time.Clock

@Composable
internal fun MiuixDateRangePickerSheet(
    show: Boolean,
    initialStart: LocalDate?,
    initialEnd: LocalDate?,
    onDismissRequest: () -> Unit,
    onClear: () -> Unit,
    onConfirm: (start: LocalDate, endInclusive: LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = today.minus(1, DateTimeUnit.DAY)

    var startYear by remember(show, initialStart) {
        mutableIntStateOf(initialStart?.year ?: DatePickerMinYear)
    }
    var startMonth by remember(show, initialStart) {
        mutableIntStateOf(initialStart?.monthNumber ?: 1)
    }
    var startDay by remember(show, initialStart) {
        mutableIntStateOf(initialStart?.dayOfMonth ?: 1)
    }
    var endYear by remember(show, initialEnd) {
        mutableIntStateOf(initialEnd?.year ?: yesterday.year)
    }
    var endMonth by remember(show, initialEnd) {
        mutableIntStateOf(initialEnd?.monthNumber ?: yesterday.monthNumber)
    }
    var endDay by remember(show, initialEnd) {
        mutableIntStateOf(initialEnd?.dayOfMonth ?: yesterday.dayOfMonth)
    }

    fun maxMonthOf(year: Int): Int = if (year >= today.year) today.monthNumber else 12

    fun maxDayOf(year: Int, month: Int): Int =
        if (year == today.year && month == today.monthNumber) {
            yesterday.dayOfMonth
        } else {
            LocalDate(year, month, 1).lengthOfMonth()
        }

    val startMaxMonth = maxMonthOf(startYear)
    val startMaxDay = maxDayOf(startYear, startMonth)
    val endMaxMonth = maxMonthOf(endYear)
    val endMaxDay = maxDayOf(endYear, endMonth)

    LaunchedEffect(startMaxMonth) {
        if (startMonth > startMaxMonth) startMonth = startMaxMonth
    }
    LaunchedEffect(startMaxDay) {
        if (startDay > startMaxDay) startDay = startMaxDay
    }
    LaunchedEffect(endMaxMonth) {
        if (endMonth > endMaxMonth) endMonth = endMaxMonth
    }
    LaunchedEffect(endMaxDay) {
        if (endDay > endMaxDay) endDay = endMaxDay
    }

    OverlayBottomSheet(
        show = show,
        title = stringResource(RStrings.date_range),
        startAction = {
            IconButton(onClick = onDismissRequest) {
                Icon(MiuixIcons.Close, contentDescription = null)
            }
        },
        endAction = {
            IconButton(
                onClick = {
                    var start = LocalDate(startYear, startMonth, startDay)
                    var end = LocalDate(endYear, endMonth, endDay)
                    if (start > end) {
                        val tmp = start
                        start = end
                        end = tmp
                    }
                    onConfirm(start, end)
                },
            ) {
                Icon(MiuixIcons.Ok, contentDescription = null)
            }
        },
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            RangeLabel(text = stringResource(RStrings.start_date))
            DateWheelRow(
                year = startYear,
                onYearChange = { startYear = it },
                month = startMonth,
                onMonthChange = { startMonth = it },
                day = startDay,
                onDayChange = { startDay = it },
                minYear = DatePickerMinYear,
                maxYear = today.year,
                maxMonth = startMaxMonth,
                maxDay = startMaxDay,
            )
            Spacer(modifier = Modifier.height(8.dp))
            RangeLabel(text = stringResource(RStrings.end_date))
            DateWheelRow(
                year = endYear,
                onYearChange = { endYear = it },
                month = endMonth,
                onMonthChange = { endMonth = it },
                day = endDay,
                onDayChange = { endDay = it },
                minYear = DatePickerMinYear,
                maxYear = today.year,
                maxMonth = endMaxMonth,
                maxDay = endMaxDay,
            )
            TextButton(
                text = stringResource(RStrings.clear_date_range),
                onClick = {
                    onClear()
                    onDismissRequest()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun RangeLabel(text: String) {
    Text(
        text = text,
        style = MiuixTheme.textStyles.footnote2,
        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        modifier = Modifier.padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
    )
}

@Composable
private fun DateWheelRow(
    year: Int,
    onYearChange: (Int) -> Unit,
    month: Int,
    onMonthChange: (Int) -> Unit,
    day: Int,
    onDayChange: (Int) -> Unit,
    minYear: Int,
    maxYear: Int,
    maxMonth: Int,
    maxDay: Int,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        NumberPicker(
            value = year,
            onValueChange = onYearChange,
            range = minYear..maxYear,
            wrapAround = false,
            label = { it.toString().padStart(2, '0') },
            modifier = Modifier.weight(1f),
        )
        DateUnitText("年")
        NumberPicker(
            value = month,
            onValueChange = onMonthChange,
            range = 1..maxMonth,
            wrapAround = false,
            label = { it.toString().padStart(2, '0') },
            modifier = Modifier.weight(1f),
        )
        DateUnitText("月")
        NumberPicker(
            value = day,
            onValueChange = onDayChange,
            range = 1..maxDay,
            wrapAround = false,
            label = { it.toString().padStart(2, '0') },
            modifier = Modifier.weight(1f),
        )
        DateUnitText("日")
    }
}