package com.mrl.pixiv.search.result.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.compose.ui.SearchContentFilterControls
import com.mrl.pixiv.common.data.AppViewMode
import com.mrl.pixiv.common.data.search.SearchAiType
import com.mrl.pixiv.common.data.search.SearchSort
import com.mrl.pixiv.common.data.search.SearchTarget
import com.mrl.pixiv.common.repository.requireUserPreferenceValue
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.search.SearchState.SearchFilter
import com.mrl.pixiv.strings.ai_generate
import com.mrl.pixiv.strings.apply
import com.mrl.pixiv.strings.date_asc
import com.mrl.pixiv.strings.date_desc
import com.mrl.pixiv.strings.filter
import com.mrl.pixiv.strings.popular_desc
import com.mrl.pixiv.strings.popular_female
import com.mrl.pixiv.strings.popular_male
import com.mrl.pixiv.strings.tags_exact_match
import com.mrl.pixiv.strings.tags_partially_match
import com.mrl.pixiv.strings.title_and_description
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.preference.RadioButtonPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
internal fun FilterBottomSheet(
    show: Boolean,
    searchFilter: SearchFilter,
    onDismissRequest: () -> Unit,
    onUpdateFilter: (SearchFilter) -> Unit,
    modifier: Modifier = Modifier,
    isNovelMode: Boolean = false,
) {
    var innerSearchFilter by remember { mutableStateOf(searchFilter) }
    OverlayBottomSheet(
        show = show,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        backgroundColor = MiuixTheme.colorScheme.background,
        title = stringResource(RStrings.filter),
        endAction = {
            IconButton(
                onClick = {
                    onUpdateFilter(innerSearchFilter)
                    onDismissRequest()
                },
            ) {
                Icon(
                    imageVector = MiuixIcons.Ok,
                    contentDescription = stringResource(RStrings.apply),
                )
            }
        },
    ) {
        val searchTargetMap = remember(isNovelMode) {
            if (isNovelMode) {
                mapOf(
                    SearchTarget.PARTIAL_MATCH_FOR_TAGS to RStrings.tags_partially_match,
                    SearchTarget.EXACT_MATCH_FOR_TAGS to RStrings.tags_exact_match,
                    SearchTarget.KEYWORD to RStrings.title_and_description,
                )
            } else {
                mapOf(
                    SearchTarget.PARTIAL_MATCH_FOR_TAGS to RStrings.tags_partially_match,
                    SearchTarget.EXACT_MATCH_FOR_TAGS to RStrings.tags_exact_match,
                    SearchTarget.TITLE_AND_CAPTION to RStrings.title_and_description,
                )
            }
        }
        val searchSortMap = remember(isNovelMode) {
            if (isNovelMode) {
                mapOf(
                    SearchSort.DATE_DESC to RStrings.date_desc,
                    SearchSort.DATE_ASC to RStrings.date_asc,
                    SearchSort.POPULAR_DESC to RStrings.popular_desc,
                )
            } else {
                mapOf(
                    SearchSort.DATE_DESC to RStrings.date_desc,
                    SearchSort.DATE_ASC to RStrings.date_asc,
                    SearchSort.POPULAR_DESC to RStrings.popular_desc,
                    SearchSort.POPULAR_MALE_DESC to RStrings.popular_male,
                    SearchSort.POPULAR_FEMALE_DESC to RStrings.popular_female,
                )
            }
        }

        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                searchTargetMap.forEach { (key, value) ->
                    RadioButtonPreference(
                        title = stringResource(value),
                        selected = innerSearchFilter.searchTarget == key,
                        onClick = {
                            innerSearchFilter = innerSearchFilter.copy(searchTarget = key)
                        }
                    )
                }
            }

            Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                searchSortMap.forEach { (key, value) ->
                    RadioButtonPreference(
                        title = stringResource(value),
                        selected = innerSearchFilter.sort == key,
                        onClick = {
                            innerSearchFilter = innerSearchFilter.copy(sort = key)
                        }
                    )
                }
            }

            Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                SearchContentFilterControls(
                    filter = innerSearchFilter.contentFilter,
                    defaultShowR18 = requireUserPreferenceValue.isR18Enabled,
                    onChange = { innerSearchFilter = innerSearchFilter.copy(contentFilter = it) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    mode = if (isNovelMode) AppViewMode.NOVEL else AppViewMode.ILLUST,
                )
            }

            Card(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                SwitchPreference(
                    title = stringResource(RStrings.ai_generate),
                    checked = innerSearchFilter.searchAiType == SearchAiType.SHOW_AI,
                    onCheckedChange = { checked ->
                        innerSearchFilter = innerSearchFilter.copy(
                            searchAiType = if (checked) SearchAiType.SHOW_AI else SearchAiType.HIDE_AI
                        )
                    },
                )
            }
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}