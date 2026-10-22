package com.mrl.pixiv.search.result.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CollectionsBookmark
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.repository.requireUserInfoFlow
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.throttleClick
import com.mrl.pixiv.components.date.MiuixDateRangePickerSheet
import com.mrl.pixiv.strings.label_default
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDateRange
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.icon.extended.Months
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowListPopup

@Composable
internal fun SearchResultAppBar(
    scrollBehavior: ScrollBehavior,
    searchWords: String,
    bookmarkNumRange: IntRange?,
    bookmarkStringRange: String?,
    searchDateRange: LocalDateRange?,
    onBookmarkNumRangeChanged: (IntRange?) -> Unit,
    onBookmarkStringRangeChanged: (String?) -> Unit,
    onSearchDateRangeChanged: (LocalDateRange?) -> Unit,
    popBack: () -> Unit,
    showBottomSheet: () -> Unit,
    modifier: Modifier = Modifier,
    showFilterAction: Boolean = true,
) {
    var showBookmarkMenu by rememberSaveable { mutableStateOf(false) }
    var showDateRangePicker by rememberSaveable { mutableStateOf(false) }
    val isPremium by requireUserInfoFlow.map { it.profile.isPremium }
        .collectAsStateWithLifecycle(false)

    TopAppBar(
        modifier = modifier,
        title = searchWords,
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            IconButton(onClick = popBack) {
                Icon(
                    imageVector = MiuixIcons.Back,
                    contentDescription = "Back",
                )
            }
        },
        actions = {
            if (showFilterAction) {
                IconButton(onClick = { showDateRangePicker = true }) {
                    Icon(
                        imageVector = MiuixIcons.Months,
                        contentDescription = "Date Range",
                        tint = if (searchDateRange != null) MiuixTheme.colorScheme.primary else LocalContentColor.current,
                    )
                }
                IconButton(onClick = { showBookmarkMenu = true }) {
                    Icon(
                        imageVector = Icons.Rounded.CollectionsBookmark,
                        contentDescription = "Bookmark Range",
                        tint = if (bookmarkNumRange != null) MiuixTheme.colorScheme.primary else LocalContentColor.current,
                    )
                }
                if (isPremium) {
                    PremiumBookmarkRangeSelector(
                        expanded = showBookmarkMenu,
                        onDismissRequest = { showBookmarkMenu = false },
                        bookmarkNumRange = bookmarkNumRange,
                        onBookmarkNumRangeChanged = onBookmarkNumRangeChanged,
                    )
                } else {
                    NonPremiumBookmarkRangeSelector(
                        expanded = showBookmarkMenu,
                        onDismissRequest = { showBookmarkMenu = false },
                        selectedRange = bookmarkStringRange,
                        onBookmarkStringRangeChanged = onBookmarkStringRangeChanged,
                    )
                }
                IconButton(onClick = showBottomSheet) {
                    Icon(
                        imageVector = MiuixIcons.Filter,
                        contentDescription = "Filter",
                    )
                }
            }
        },
    )
    MiuixDateRangePickerSheet(
        show = showDateRangePicker,
        initialStart = searchDateRange?.start,
        initialEnd = searchDateRange?.endInclusive,
        onDismissRequest = { showDateRangePicker = false },
        onClear = { onSearchDateRangeChanged(null) },
        onConfirm = { start, end ->
            onSearchDateRangeChanged(LocalDateRange(start, end))
        },
    )
    }

@Composable
private fun PremiumBookmarkRangeSelector(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    bookmarkNumRange: IntRange?,
    onBookmarkNumRangeChanged: (IntRange?) -> Unit,
) {
    WindowListPopup(
        show = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        ListPopupColumn {
            val ranges = listOf(
                stringResource(RStrings.label_default) to null,
                "10~29" to (10..29),
                "30~49" to (30..49),
                "50~99" to (50..99),
                "100~299" to (100..299),
                "300~499" to (300..499),
                "500~999" to (500..999),
                "1000~4999" to (1000..4999),
                "5000~9999" to (5000..9999),
                "10000~49999" to (10000..49999),
                ">50000" to (50000..Int.MAX_VALUE),
            )

            ranges.forEach { (label, range) ->
                val isSelected = range == bookmarkNumRange
                BasicComponent(
                    title = label,
                    onClick = {
                        onBookmarkNumRangeChanged(range)
                        onDismissRequest()
                    },
                    endActions = if (isSelected) {
                        {
                            Icon(
                                imageVector = MiuixIcons.Ok,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}

@Composable
private fun NonPremiumBookmarkRangeSelector(
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    selectedRange: String?,
    onBookmarkStringRangeChanged: (String?) -> Unit,
) {
    WindowListPopup(
        show = expanded,
        onDismissRequest = onDismissRequest,
    ) {
        ListPopupColumn {
            val ranges = listOf(
                stringResource(RStrings.label_default) to null,
                "100 users入り" to "100 users入り",
                "250 users入り" to "250 users入り",
                "500 users入り" to "500 users入り",
                "1000 users入り" to "1000 users入り",
                "5000 users入り" to "5000 users入り",
                "7500 users入り" to "7500 users入り",
                "10000 users入り" to "10000 users入り",
                "20000 users入り" to "20000 users入り",
                "30000 users入り" to "30000 users入り",
                "50000 users入り" to "50000 users入り",
            )

            ranges.forEach { (label, range) ->
                val isSelected = range == selectedRange
                BasicComponent(
                    title = label,
                    onClick = {
                        onBookmarkStringRangeChanged(range)
                        onDismissRequest()
                    },
                    endActions = if (isSelected) {
                        {
                            Icon(
                                imageVector = MiuixIcons.Ok,
                                contentDescription = null,
                            )
                        }
                    } else {
                        null
                    },
                )
            }
        }
    }
}
