package com.mrl.pixiv.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.compose.ui.SearchContentFilterControls
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.data.search.SearchAiType
import com.mrl.pixiv.common.data.search.SearchSort
import com.mrl.pixiv.common.data.search.SearchTarget
import com.mrl.pixiv.common.data.setting.SearchResultDisplayMode
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.requireUserPreferenceFlow
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.ai_generate
import com.mrl.pixiv.strings.date_asc
import com.mrl.pixiv.strings.date_desc
import com.mrl.pixiv.strings.default_search_sort
import com.mrl.pixiv.strings.default_search_target
import com.mrl.pixiv.strings.popular_desc
import com.mrl.pixiv.strings.popular_female
import com.mrl.pixiv.strings.popular_male
import com.mrl.pixiv.strings.search_result_display_mode
import com.mrl.pixiv.strings.search_result_display_mode_infinite
import com.mrl.pixiv.strings.search_result_display_mode_paged
import com.mrl.pixiv.strings.search_setting
import com.mrl.pixiv.strings.tags_exact_match
import com.mrl.pixiv.strings.tags_partially_match
import com.mrl.pixiv.strings.title_and_description
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun SearchSettingScreen(
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val userPreference by requireUserPreferenceFlow.collectAsStateWithLifecycle()
    val searchSettings = userPreference.searchSettings

    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.search_setting),
                navigationIcon = {
                    IconButton(onClick = navigationManager::popBackStack) {
                        Icon(MiuixIcons.Back, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .pageScrollModifiers(scrollBehavior)
                .verticalScroll(rememberScrollState())
                .padding(paddingValues)
                .imePadding()
                .padding(vertical = 8.dp)
        ) {
            Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                DefaultSearchTargetSetting(
                    selectedTarget = searchSettings.defaultSearchTarget,
                    onTargetChange = { target ->
                        SettingRepository.setSearchSettings(
                            searchSettings.copy(defaultSearchTarget = target),
                        )
                    },
                )
                DefaultSearchSortSetting(
                    selectedSort = searchSettings.defaultSearchSort,
                    onSortChange = { sort ->
                        SettingRepository.setSearchSettings(
                            searchSettings.copy(defaultSearchSort = sort),
                        )
                    },
                )
                SwitchPreference(
                    checked = searchSettings.defaultSearchAiType == SearchAiType.SHOW_AI,
                    onCheckedChange = { checked ->
                        SettingRepository.setSearchSettings(
                            searchSettings.copy(
                                defaultSearchAiType = if (checked) {
                                    SearchAiType.SHOW_AI
                                } else {
                                    SearchAiType.HIDE_AI
                                },
                            ),
                        )
                    },
                    title = stringResource(RStrings.ai_generate),
                    startAction = { Icon(Icons.Rounded.AutoAwesome, contentDescription = null) },
                )
                SearchResultDisplayModeSetting(
                    selectedMode = searchSettings.searchResultDisplayMode,
                    onModeChange = { mode ->
                        SettingRepository.setSearchSettings(
                            searchSettings.copy(searchResultDisplayMode = mode),
                        )
                    },
                )
                SearchContentFilterControls(
                    filter = searchSettings.defaultContentFilter,
                    defaultShowR18 = userPreference.isR18Enabled,
                    onChange = {
                        SettingRepository.setSearchSettings(searchSettings.copy(defaultContentFilter = it))
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                )
            }
        }
    }
}

@Composable
private fun DefaultSearchTargetSetting(
    selectedTarget: SearchTarget,
    onTargetChange: (SearchTarget) -> Unit,
    modifier: Modifier = Modifier,
) {
    val targets = remember {
        listOf(
            SearchTarget.PARTIAL_MATCH_FOR_TAGS,
            SearchTarget.EXACT_MATCH_FOR_TAGS,
            SearchTarget.TITLE_AND_CAPTION,
        )
    }
    var selectedIndex by remember(selectedTarget) {
        mutableIntStateOf(targets.indexOf(selectedTarget).coerceAtLeast(0))
    }

    OverlayDropdownPreference(
        items = targets.map { it.label() },
        selectedIndex = selectedIndex,
        title = stringResource(RStrings.default_search_target),
        modifier = modifier,
        startAction = { Icon(Icons.Rounded.FilterAlt, contentDescription = null) },
        onSelectedIndexChange = { index ->
            selectedIndex = index
            onTargetChange(targets[index])
        },
    )
}

@Composable
private fun DefaultSearchSortSetting(
    selectedSort: SearchSort,
    onSortChange: (SearchSort) -> Unit,
    modifier: Modifier = Modifier,
) {
    val sorts = remember {
        listOf(
            SearchSort.DATE_DESC,
            SearchSort.DATE_ASC,
            SearchSort.POPULAR_DESC,
            SearchSort.POPULAR_MALE_DESC,
            SearchSort.POPULAR_FEMALE_DESC,
        )
    }
    var selectedIndex by remember(selectedSort) {
        mutableIntStateOf(sorts.indexOf(selectedSort).coerceAtLeast(0))
    }

    OverlayDropdownPreference(
        items = sorts.map { it.label() },
        selectedIndex = selectedIndex,
        title = stringResource(RStrings.default_search_sort),
        modifier = modifier,
        startAction = { Icon(Icons.AutoMirrored.Rounded.Sort, contentDescription = null) },
        onSelectedIndexChange = { index ->
            selectedIndex = index
            onSortChange(sorts[index])
        },
    )
}

@Composable
private fun SearchSort.label(): String {
    return when (this) {
        SearchSort.DATE_DESC -> stringResource(RStrings.date_desc)
        SearchSort.DATE_ASC -> stringResource(RStrings.date_asc)
        SearchSort.POPULAR_DESC -> stringResource(RStrings.popular_desc)
        SearchSort.POPULAR_MALE_DESC -> stringResource(RStrings.popular_male)
        SearchSort.POPULAR_FEMALE_DESC -> stringResource(RStrings.popular_female)
    }
}

@Composable
private fun SearchTarget.label(): String {
    return when (this) {
        SearchTarget.PARTIAL_MATCH_FOR_TAGS ->
            stringResource(RStrings.tags_partially_match)

        SearchTarget.EXACT_MATCH_FOR_TAGS ->
            stringResource(RStrings.tags_exact_match)

        SearchTarget.TITLE_AND_CAPTION,
        SearchTarget.TEXT,
        SearchTarget.KEYWORD -> stringResource(RStrings.title_and_description)
    }
}

@Composable
private fun SearchResultDisplayModeSetting(
    selectedMode: SearchResultDisplayMode,
    onModeChange: (SearchResultDisplayMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val modes = remember { SearchResultDisplayMode.entries }
    var selectedIndex by remember(selectedMode) {
        mutableIntStateOf(modes.indexOf(selectedMode).coerceAtLeast(0))
    }

    OverlayDropdownPreference(
        items = modes.map { it.label() },
        selectedIndex = selectedIndex,
        title = stringResource(RStrings.search_result_display_mode),
        modifier = modifier,
        startAction = { Icon(Icons.Rounded.ViewModule, contentDescription = null) },
        onSelectedIndexChange = { index ->
            selectedIndex = index
            onModeChange(modes[index])
        },
    )
}

@Composable
private fun SearchResultDisplayMode.label(): String {
    return when (this) {
        SearchResultDisplayMode.INFINITE_SCROLL ->
            stringResource(RStrings.search_result_display_mode_infinite)

        SearchResultDisplayMode.PAGED ->
            stringResource(RStrings.search_result_display_mode_paged)
    }
}
