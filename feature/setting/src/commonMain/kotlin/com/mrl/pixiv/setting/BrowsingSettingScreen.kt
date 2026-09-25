package com.mrl.pixiv.setting

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FilterAlt
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.ViewModule
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.data.setting.BrowsingSettings
import com.mrl.pixiv.common.data.setting.PreviewImageQuality
import com.mrl.pixiv.common.data.setting.SearchResultIllustLayout
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.requireUserPreferenceFlow
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.auto_hide_preview_controls
import com.mrl.pixiv.strings.auto_hide_preview_controls_desc
import com.mrl.pixiv.strings.browsing_setting
import com.mrl.pixiv.strings.filter_long_novel_tags
import com.mrl.pixiv.strings.filter_long_novel_tags_desc
import com.mrl.pixiv.strings.max_novel_tag_length
import com.mrl.pixiv.strings.max_novel_tag_length_desc
import com.mrl.pixiv.strings.max_novel_tag_segments
import com.mrl.pixiv.strings.max_novel_tag_segments_desc
import com.mrl.pixiv.strings.preview_image_quality
import com.mrl.pixiv.strings.preview_image_quality_high
import com.mrl.pixiv.strings.preview_image_quality_medium
import com.mrl.pixiv.strings.preview_image_quality_original
import com.mrl.pixiv.strings.search_result_illust_layout
import com.mrl.pixiv.strings.search_result_illust_layout_original_aspect_ratio
import com.mrl.pixiv.strings.search_result_illust_layout_square
import com.mrl.pixiv.strings.span_count_adaptive
import com.mrl.pixiv.strings.span_count_landscape
import com.mrl.pixiv.strings.span_count_portrait
import com.mrl.pixiv.strings.tap_image_to_open_full_resolution_preview
import com.mrl.pixiv.strings.tap_image_to_open_full_resolution_preview_desc
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TextField
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference

@Composable
fun BrowsingSettingScreen(
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val userPreference by requireUserPreferenceFlow.collectAsStateWithLifecycle()
    val browsingSettings = userPreference.browsingSettings

    val scrollBehavior = MiuixScrollBehavior()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.browsing_setting),
                navigationIcon = {
                    IconButton(onClick = navigationManager::popBackStack) {
                        Icon(MiuixIcons.Back, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) {
        Column(
            modifier = Modifier
                .pageScrollModifiers(scrollBehavior)
                .verticalScroll(rememberScrollState())
                .padding(it)
                .imePadding()
                .padding(vertical = 8.dp)
        ) {
            Card(modifier = Modifier.padding(horizontal = 12.dp)) {
                SpanCountSetting(
                    title = stringResource(RStrings.span_count_portrait),
                    currentSpanCount = userPreference.spanCountPortrait,
                    onSpanCountChange = SettingRepository::setSpanCountPortrait,
                )
                SpanCountSetting(
                    title = stringResource(RStrings.span_count_landscape),
                    currentSpanCount = userPreference.spanCountLandscape,
                    onSpanCountChange = SettingRepository::setSpanCountLandscape,
                )
                SearchResultIllustLayoutSetting(
                    selectedLayout = browsingSettings.searchResultIllustLayout,
                    onLayoutChange = { layout ->
                        SettingRepository.setBrowsingSettings(
                            browsingSettings.copy(searchResultIllustLayout = layout),
                        )
                    },
                )
                PreviewImageQualitySetting(
                    selectedQuality = browsingSettings.previewImageQuality,
                    onQualityChange = { quality ->
                        SettingRepository.setBrowsingSettings(
                            browsingSettings.copy(previewImageQuality = quality),
                        )
                    },
                )
                SwitchPreference(
                    checked = browsingSettings.autoHidePreviewControls,
                    onCheckedChange = { checked ->
                        SettingRepository.setBrowsingSettings(
                            browsingSettings.copy(autoHidePreviewControls = checked),
                        )
                    },
                    title = stringResource(RStrings.auto_hide_preview_controls),
                    summary = stringResource(RStrings.auto_hide_preview_controls_desc),
                    startAction = { Icon(Icons.Rounded.VisibilityOff, contentDescription = null) },
                )
                SwitchPreference(
                    checked = browsingSettings.tapImageToOpenFullResolutionPreview,
                    onCheckedChange = { checked ->
                        SettingRepository.setBrowsingSettings(
                            browsingSettings.copy(tapImageToOpenFullResolutionPreview = checked),
                        )
                    },
                    title = stringResource(RStrings.tap_image_to_open_full_resolution_preview),
                    summary = stringResource(RStrings.tap_image_to_open_full_resolution_preview_desc),
                    startAction = { Icon(Icons.Rounded.TouchApp, contentDescription = null) },
                )
                SwitchPreference(
                    checked = browsingSettings.filterLongNovelTags,
                    onCheckedChange = { checked ->
                        SettingRepository.setBrowsingSettings(
                            browsingSettings.copy(filterLongNovelTags = checked),
                        )
                    },
                    title = stringResource(RStrings.filter_long_novel_tags),
                    summary = stringResource(RStrings.filter_long_novel_tags_desc),
                    startAction = { Icon(Icons.Rounded.FilterAlt, contentDescription = null) },
                )
                if (browsingSettings.filterLongNovelTags) {
                    NovelTagLimitSetting(
                        title = stringResource(RStrings.max_novel_tag_length),
                        description = stringResource(RStrings.max_novel_tag_length_desc),
                        value = browsingSettings.maxNovelTagLength,
                        onValueChange = { value ->
                            SettingRepository.setBrowsingSettings(
                                browsingSettings.copy(maxNovelTagLength = value),
                            )
                        },
                    )
                    NovelTagLimitSetting(
                        title = stringResource(RStrings.max_novel_tag_segments),
                        description = stringResource(RStrings.max_novel_tag_segments_desc),
                        value = browsingSettings.maxNovelTagSegments,
                        onValueChange = { value ->
                            SettingRepository.setBrowsingSettings(
                                browsingSettings.copy(maxNovelTagSegments = value),
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultIllustLayoutSetting(
    selectedLayout: SearchResultIllustLayout,
    onLayoutChange: (SearchResultIllustLayout) -> Unit,
    modifier: Modifier = Modifier,
) {
    val layouts = remember { SearchResultIllustLayout.entries }
    var selectedIndex by remember(selectedLayout) {
        mutableIntStateOf(layouts.indexOf(selectedLayout).coerceAtLeast(0))
    }

    OverlayDropdownPreference(
        items = layouts.map { it.label() },
        selectedIndex = selectedIndex,
        title = stringResource(RStrings.search_result_illust_layout),
        modifier = modifier,
        startAction = { Icon(Icons.Rounded.ViewModule, contentDescription = null) },
        onSelectedIndexChange = { index ->
            selectedIndex = index
            onLayoutChange(layouts[index])
        },
    )
}

@Composable
private fun SearchResultIllustLayout.label(): String = when (this) {
    SearchResultIllustLayout.SQUARE ->
        stringResource(RStrings.search_result_illust_layout_square)

    SearchResultIllustLayout.ORIGINAL_ASPECT_RATIO ->
        stringResource(RStrings.search_result_illust_layout_original_aspect_ratio)
}

@Composable
private fun SpanCountSetting(
    title: String,
    currentSpanCount: Int,
    onSpanCountChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val adaptiveLabel = stringResource(RStrings.span_count_adaptive)
    val options = remember(adaptiveLabel) {
        listOf(
            2 to "2",
            3 to "3",
            4 to "4",
            -1 to adaptiveLabel,
        )
    }
    val selectedIndex = options.indexOfFirst { it.first == currentSpanCount }
        .coerceAtLeast(0)

    OverlayDropdownPreference(
        items = options.map { it.second },
        selectedIndex = selectedIndex,
        title = title,
        modifier = modifier,
        startAction = { Icon(Icons.Rounded.ViewModule, contentDescription = null) },
        onSelectedIndexChange = { index ->
            onSpanCountChange(options[index].first)
        },
    )
}

@Composable
private fun NovelTagLimitSetting(
    title: String,
    description: String,
    value: Int,
    onValueChange: (Int) -> Unit,
) {
    var input by remember(value) { mutableStateOf(value.toString()) }
    val validRange = BrowsingSettings.MIN_NOVEL_TAG_LIMIT..BrowsingSettings.MAX_NOVEL_TAG_LIMIT
    val parsedValue = input.toIntOrNull()

    BasicComponent(
        title = title,
        summary = description,
        startAction = { Icon(Icons.Rounded.Tag, contentDescription = null) },
        endActions = {
            TextField(
                modifier = Modifier.width(104.dp),
                value = input,
                onValueChange = { newValue ->
                    val digits = newValue.filter(Char::isDigit).take(3)
                    input = digits
                    digits.toIntOrNull()
                        ?.takeIf { it in validRange }
                        ?.let(onValueChange)
                },
                singleLine = true,
                enabled = parsedValue != null && parsedValue in validRange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
    )
}

@Composable
private fun PreviewImageQualitySetting(
    selectedQuality: PreviewImageQuality,
    onQualityChange: (PreviewImageQuality) -> Unit,
    modifier: Modifier = Modifier,
) {
    val qualities = remember { PreviewImageQuality.entries }
    var selectedIndex by remember(selectedQuality) {
        mutableIntStateOf(qualities.indexOf(selectedQuality).coerceAtLeast(0))
    }

    OverlayDropdownPreference(
        items = qualities.map { it.label() },
        selectedIndex = selectedIndex,
        title = stringResource(RStrings.preview_image_quality),
        modifier = modifier,
        startAction = { Icon(Icons.Rounded.Image, contentDescription = null) },
        onSelectedIndexChange = { index ->
            selectedIndex = index
            onQualityChange(qualities[index])
        },
    )
}

@Composable
private fun PreviewImageQuality.label(): String {
    return when (this) {
        PreviewImageQuality.MEDIUM -> stringResource(RStrings.preview_image_quality_medium)
        PreviewImageQuality.HIGH -> stringResource(RStrings.preview_image_quality_high)
        PreviewImageQuality.ORIGINAL -> stringResource(RStrings.preview_image_quality_original)
    }
}
