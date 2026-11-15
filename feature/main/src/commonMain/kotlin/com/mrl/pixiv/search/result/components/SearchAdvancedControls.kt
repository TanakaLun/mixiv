package com.mrl.pixiv.search.result.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.data.search.IllustAdvancedFilter
import com.mrl.pixiv.common.data.search.NovelAdvancedFilter
import com.mrl.pixiv.common.data.search.SearchGenreOption
import com.mrl.pixiv.common.data.search.SearchLanguageOption
import com.mrl.pixiv.common.data.search.SearchNumberRange
import com.mrl.pixiv.common.data.search.SearchRatio
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.search.SearchState.SearchFilter
import com.mrl.pixiv.search.result.SearchOptionsState
import com.mrl.pixiv.strings.all
import com.mrl.pixiv.strings.discovery_advanced_filters
import com.mrl.pixiv.strings.discovery_advanced_sort_note
import com.mrl.pixiv.strings.discovery_character_count
import com.mrl.pixiv.strings.discovery_genre
import com.mrl.pixiv.strings.discovery_height_pixels
import com.mrl.pixiv.strings.discovery_invalid_range
import com.mrl.pixiv.strings.discovery_landscape
import com.mrl.pixiv.strings.discovery_language
import com.mrl.pixiv.strings.discovery_maximum
import com.mrl.pixiv.strings.discovery_minimum
import com.mrl.pixiv.strings.discovery_original_only
import com.mrl.pixiv.strings.discovery_portrait
import com.mrl.pixiv.strings.discovery_ratio
import com.mrl.pixiv.strings.discovery_reading_minutes
import com.mrl.pixiv.strings.discovery_replaceable_only
import com.mrl.pixiv.strings.discovery_retry
import com.mrl.pixiv.strings.discovery_search_options_failed
import com.mrl.pixiv.strings.discovery_square
import com.mrl.pixiv.strings.discovery_tool
import com.mrl.pixiv.strings.discovery_width_pixels
import com.mrl.pixiv.strings.discovery_word_count
import com.mrl.pixiv.strings.discovery_word_count_languages
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowListPopup

/**
 * 搜索高级筛选（miuix 风格）：数值范围使用内联输入框，选项使用弹出列表，
 * 布尔项使用开关。所有改动即时写回筛选条件。
 */
@Composable
internal fun SearchAdvancedControls(
    filter: SearchFilter,
    isNovel: Boolean,
    options: SearchOptionsState,
    onChange: (SearchFilter) -> Unit,
    onValidityChanged: (Boolean) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val validity = remember { mutableStateMapOf<String, Boolean>() }
    val reportValidity: (String, Boolean) -> Unit = { key, valid ->
        validity[key] = valid
        onValidityChanged(validity.values.all { it })
    }
    val allLabel = stringResource(RStrings.all)
    val advancedIllust = filter.illustAdvanced
    val advancedNovel = filter.novelAdvanced
    val searchOptions = options.options

    Column(modifier = modifier) {
        SmallTitle(
            text = stringResource(RStrings.discovery_advanced_filters),
            insideMargin = PaddingValues(horizontal = 0.dp, vertical = 4.dp),
        )
        Text(
            text = stringResource(RStrings.discovery_advanced_sort_note),
            style = MiuixTheme.textStyles.footnote2,
            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 4.dp),
        )

        when {
            options.loading -> {
                BasicComponent(
                    title = stringResource(RStrings.discovery_advanced_filters),
                    summary = stringResource(RStrings.discovery_retry),
                )
            }

            options.failed -> {
                Card {
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Text(
                            text = stringResource(RStrings.discovery_search_options_failed),
                            style = MiuixTheme.textStyles.footnote2,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                        TextButton(
                            text = stringResource(RStrings.discovery_retry),
                            onClick = onRetry,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            else -> {
                if (isNovel) {
                    NovelAdvancedSection(
                        advanced = advancedNovel,
                        options = searchOptions?.novel,
                        allLabel = allLabel,
                        reportValidity = reportValidity,
                        onChange = { onChange(filter.copy(novelAdvanced = it)) },
                    )
                } else {
                    IllustAdvancedSection(
                        advanced = advancedIllust,
                        options = searchOptions?.illust,
                        allLabel = allLabel,
                        reportValidity = reportValidity,
                        onChange = { onChange(filter.copy(illustAdvanced = it)) },
                    )
                }
            }
        }
    }
}

@Composable
private fun IllustAdvancedSection(
    advanced: IllustAdvancedFilter,
    options: com.mrl.pixiv.common.data.search.SearchIllustOptions?,
    allLabel: String,
    reportValidity: (String, Boolean) -> Unit,
    onChange: (IllustAdvancedFilter) -> Unit,
) {
    Card(modifier = Modifier.padding(vertical = 4.dp)) {
        SearchRangePreference(
            title = stringResource(RStrings.discovery_width_pixels),
            range = advanced.width,
            onValidityChanged = { reportValidity("illust_width", it) },
            onRangeChange = { onChange(advanced.copy(width = it)) },
        )
        SearchRangePreference(
            title = stringResource(RStrings.discovery_height_pixels),
            range = advanced.height,
            onValidityChanged = { reportValidity("illust_height", it) },
            onRangeChange = { onChange(advanced.copy(height = it)) },
        )
        SearchOptionPreference<SearchRatio>(
            title = stringResource(RStrings.discovery_ratio),
            allLabel = allLabel,
            options = listOf(
                SearchRatio.PORTRAIT to stringResource(RStrings.discovery_portrait),
                SearchRatio.LANDSCAPE to stringResource(RStrings.discovery_landscape),
                SearchRatio.SQUARE to stringResource(RStrings.discovery_square),
            ),
            selected = advanced.ratio,
            onSelect = { onChange(advanced.copy(ratio = it)) },
        )
        SearchOptionPreference<String>(
            title = stringResource(RStrings.discovery_tool),
            allLabel = allLabel,
            options = options?.tool?.options.orEmpty().map { it to it },
            selected = advanced.tool,
            onSelect = { onChange(advanced.copy(tool = it)) },
        )
        SearchOptionPreference<String>(
            title = stringResource(RStrings.discovery_language),
            allLabel = allLabel,
            options = options?.lang?.options.orEmpty().map { it.code to it.displayName() },
            selected = advanced.language,
            onSelect = { onChange(advanced.copy(language = it)) },
        )
    }
}

@Composable
private fun NovelAdvancedSection(
    advanced: NovelAdvancedFilter,
    options: com.mrl.pixiv.common.data.search.SearchNovelOptions?,
    allLabel: String,
    reportValidity: (String, Boolean) -> Unit,
    onChange: (NovelAdvancedFilter) -> Unit,
) {
    Card(modifier = Modifier.padding(vertical = 4.dp)) {
        SearchRangePreference(
            title = stringResource(RStrings.discovery_character_count),
            range = advanced.textLength,
            onValidityChanged = { reportValidity("novel_text_length", it) },
            onRangeChange = { onChange(advanced.copy(textLength = it)) },
        )
        val wordCountLanguages = options?.wordCountSupportedLanguages.orEmpty()
        SearchRangePreference(
            title = stringResource(RStrings.discovery_word_count),
            range = advanced.wordCount,
            summary = wordCountLanguages.takeIf { it.isNotBlank() }?.let {
                stringResource(RStrings.discovery_word_count_languages, it)
            },
            onValidityChanged = { reportValidity("novel_word_count", it) },
            onRangeChange = { onChange(advanced.copy(wordCount = it)) },
        )
        SearchRangePreference(
            title = stringResource(RStrings.discovery_reading_minutes),
            range = advanced.readingTime,
            onValidityChanged = { reportValidity("novel_reading_time", it) },
            onRangeChange = { onChange(advanced.copy(readingTime = it)) },
        )
        SearchOptionPreference<String>(
            title = stringResource(RStrings.discovery_language),
            allLabel = allLabel,
            options = options?.lang?.options.orEmpty().map { it.code to it.displayName() },
            selected = advanced.language,
            onSelect = { onChange(advanced.copy(language = it)) },
        )
        SearchOptionPreference<Int>(
            title = stringResource(RStrings.discovery_genre),
            allLabel = allLabel,
            options = options?.genre?.options.orEmpty().map { it.id to it.label },
            selected = advanced.genre,
            onSelect = { onChange(advanced.copy(genre = it)) },
        )
        SwitchPreference(
            title = stringResource(RStrings.discovery_original_only),
            checked = advanced.originalOnly,
            onCheckedChange = { onChange(advanced.copy(originalOnly = it)) },
        )
        SwitchPreference(
            title = stringResource(RStrings.discovery_replaceable_only),
            checked = advanced.replaceableOnly,
            onCheckedChange = { onChange(advanced.copy(replaceableOnly = it)) },
        )
    }
}

/**
 * 数值范围输入：空值表示不限制；仅接受非负整数，且最小值不能大于最大值。
 */
@Composable
private fun SearchRangePreference(
    title: String,
    range: SearchNumberRange,
    onRangeChange: (SearchNumberRange) -> Unit,
    onValidityChanged: (Boolean) -> Unit,
    summary: String? = null,
) {
    var minText by remember(range) { mutableStateOf(range.min?.toString().orEmpty()) }
    var maxText by remember(range) { mutableStateOf(range.max?.toString().orEmpty()) }
    var valid by remember { mutableStateOf(true) }

    fun emit() {
        val min = minText.toIntOrNull()
        val max = maxText.toIntOrNull()
        valid = (minText.isEmpty() || min != null) &&
            (maxText.isEmpty() || max != null) &&
            (min == null || max == null || min <= max)
        onValidityChanged(valid)
        if (valid) {
            onRangeChange(SearchNumberRange(min, max))
        }
    }

    BasicComponent(
        title = title,
        summary = summary,
        bottomAction = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    TextField(
                        value = minText,
                        onValueChange = {
                            minText = it.filter(Char::isDigit)
                            emit()
                        },
                        modifier = Modifier.weight(1f),
                        label = stringResource(RStrings.discovery_minimum),
                        useLabelAsPlaceholder = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                    Text(
                        text = "~",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                    )
                    TextField(
                        value = maxText,
                        onValueChange = {
                            maxText = it.filter(Char::isDigit)
                            emit()
                        },
                        modifier = Modifier.weight(1f),
                        label = stringResource(RStrings.discovery_maximum),
                        useLabelAsPlaceholder = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
                if (!valid) {
                    Text(
                        text = stringResource(RStrings.discovery_invalid_range),
                        style = MiuixTheme.textStyles.footnote2,
                        color = MiuixTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp),
                    )
                }
            }
        },
    )
}

/**
 * 单选弹出列表：选中项显示对勾，第一项为“全部”（清空该条件）。
 */
@Composable
private fun <T> SearchOptionPreference(
    title: String,
    allLabel: String,
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T?) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedLabel = options.firstOrNull { it.first == selected }?.second ?: allLabel

    ArrowPreference(
        title = title,
        summary = selectedLabel,
        onClick = { expanded = true },
    )
    WindowListPopup(
        show = expanded,
        onDismissRequest = { expanded = false },
    ) {
        ListPopupColumn {
            BasicComponent(
                title = allLabel,
                onClick = {
                    onSelect(null)
                    expanded = false
                },
                endActions = {
                    if (selected == null) {
                        Icon(imageVector = MiuixIcons.Ok, contentDescription = null)
                    }
                },
            )
            options.forEach { (value, label) ->
                BasicComponent(
                    title = label,
                    onClick = {
                        onSelect(value)
                        expanded = false
                    },
                    endActions = {
                        if (value == selected) {
                            Icon(imageVector = MiuixIcons.Ok, contentDescription = null)
                        }
                    },
                )
            }
        }
    }
}

private fun SearchLanguageOption.displayName(): String =
    name.takeIf { it.isNotBlank() } ?: code

private fun SearchGenreOption.displayName(): String =
    label.takeIf { it.isNotBlank() } ?: id.toString()
