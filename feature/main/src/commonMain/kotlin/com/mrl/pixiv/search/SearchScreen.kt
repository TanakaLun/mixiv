package com.mrl.pixiv.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.LifecycleResumeEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.compose.rememberThrottleClick
import com.mrl.pixiv.common.compose.ui.ViewModeAction
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.data.AppViewMode
import com.mrl.pixiv.common.kts.spaceBy
import com.mrl.pixiv.common.repository.SearchRepository
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.SettingRepository.collectAsStateWithLifecycle
import com.mrl.pixiv.common.router.DestinationsDeepLink
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.PixivLinkTarget
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.readTextFromClipboard
import com.mrl.pixiv.common.util.throttleClick
import com.mrl.pixiv.common.viewmodel.asState
import com.mrl.pixiv.strings.cancel
import com.mrl.pixiv.strings.enter_keywords
import com.mrl.pixiv.strings.find_for
import com.mrl.pixiv.strings.id_search
import com.mrl.pixiv.strings.illust
import com.mrl.pixiv.strings.keyword_search
import com.mrl.pixiv.strings.novel
import com.mrl.pixiv.strings.search
import com.mrl.pixiv.strings.search_history
import com.mrl.pixiv.strings.select_pixiv_link
import com.mrl.pixiv.strings.users
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.InputField
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.basic.SearchBar
import top.yukonga.miuix.kmp.basic.SmallTitle
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Tune
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = koinViewModel(),
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val dispatch = viewModel::dispatch
    val state = viewModel.asState()
    val appViewMode by SettingRepository.userPreferenceFlow.collectAsStateWithLifecycle { appViewMode }
    val readClipboardOnSearch by SettingRepository.userPreferenceFlow
        .collectAsStateWithLifecycle { readClipboardOnSearch }
    val searchHistory by remember(appViewMode) {
        when (appViewMode) {
            AppViewMode.ILLUST -> SearchRepository.searchHistoryFlow.map { it.searchHistoryList }
            AppViewMode.NOVEL -> SearchRepository.novelSearchHistoryFlow.map { it.novelSearchHistory }
        }
    }.collectAsStateWithLifecycle(emptyList())
    val searchIdHistory by remember(appViewMode) {
        when (appViewMode) {
            AppViewMode.ILLUST -> SearchRepository.searchIdHistoryFlow.map {
                it?.toList().orEmpty()
            }

            AppViewMode.NOVEL -> SearchRepository.novelSearchIdHistoryFlow.map {
                it?.toList().orEmpty()
            }
        }
    }.collectAsStateWithLifecycle(emptyList())
    var textState by remember { mutableStateOf(TextFieldValue(viewModel.searchWords)) }
    var pendingLinks by remember { mutableStateOf<List<PixivLinkTarget>>(emptyList()) }
    fun handlePixivLinks(
        text: String,
        alwaysShowSelection: Boolean = false,
    ): Boolean {
        return when (val action = resolvePixivLinkSearchAction(text, alwaysShowSelection)) {
            PixivLinkSearchAction.NoMatch -> false
            is PixivLinkSearchAction.Open -> {
                navigationManager.navigate(action.link.toDestination())
                true
            }

            is PixivLinkSearchAction.ShowSelection -> {
                pendingLinks = action.links
                true
            }
        }
    }

    val focusRequester = remember { FocusRequester() }
    val coroutineScope = rememberCoroutineScope()
    LifecycleResumeEffect(readClipboardOnSearch) {
        val clipboardJob = coroutineScope.launch {
            val handledClipboardLink = if (readClipboardOnSearch) {
                val clipboardText = readTextFromClipboard().orEmpty()
                viewModel.isClipboardTextChanged(clipboardText) && handlePixivLinks(
                    text = clipboardText,
                    alwaysShowSelection = true,
                )
            } else {
                false
            }
            if (!handledClipboardLink) {
                try {
                    focusRequester.requestFocus()
                } catch (_: Exception) {
                }
                textState = textState.copy(selection = TextRange(textState.text.length))
            }
        }
        onPauseOrDispose { clipboardJob.cancel() }
    }
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        modifier = modifier.clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
        ) {
            focusManager.clearFocus()
            softwareKeyboardController?.hide()
        },
        topBar = {
            SearchScreenAppBar(
                scrollBehavior = scrollBehavior,
                textState = textState,
                focusRequester = focusRequester,
                onValueChange = {
                    textState = it
                    dispatch(SearchAction.UpdateSearchWords(it.text))
                    val token = it.completionToken()
                    if (token.text.isNotBlank()) {
                        dispatch(SearchAction.SearchAutoComplete(token.text))
                    } else {
                        dispatch(SearchAction.ClearAutoCompleteSearchWords)
                    }
                },
                onBack = { navigationManager.popBackStack() },
                actions = {
                    SearchTypeAction(
                        isIdSearch = state.isIdSearch,
                        onModeChange = { dispatch(SearchAction.UpdateIsIdSearch(it)) },
                    )
                    ViewModeAction(
                        currentMode = appViewMode,
                        onModeChange = viewModel::switchViewMode,
                    )
                },
                onSearch = search@{
                    if (handlePixivLinks(textState.text)) {
                        focusRequester.freeFocus()
                        return@search
                    }
                    if (state.isIdSearch) {
                        viewModel.addSearchIdHistory(textState.text)
                    } else {
                        dispatch(SearchAction.AddSearchHistory(textState.text))
                    }
                    focusRequester.freeFocus()
                    navigationManager.navigateToSearchResultScreen(
                        searchWord = textState.text,
                        isIdSearch = state.isIdSearch,
                        searchMode = appViewMode
                    )
                }
            )
        },
    ) {
        // 用LazyColumn构造自动补全列表，点击跳转搜索结果页面
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
                .padding(top = 8.dp)
                .imePadding()
                .pageScrollModifiers(scrollBehavior),
            contentPadding = PaddingValues(horizontal = 8.dp),
            verticalArrangement = 16f.spaceBy
        ) {
            item(key = "search_section_title") {
                SmallTitle(
                    text = if (textState.text.isEmpty())
                        stringResource(RStrings.search_history)
                    else
                        stringResource(RStrings.find_for),
                )
            }
            if (textState.text.isEmpty()) {
                item(key = "search_history_card") {
                    Card {
                        if (state.isIdSearch) {
                            searchIdHistory.forEach { historyItem ->
                                BasicComponent(
                                    title = historyItem,
                                    onClick = rememberThrottleClick {
                                        viewModel.addSearchIdHistory(historyItem)
                                        focusRequester.freeFocus()
                                        navigationManager.navigateToSearchResultScreen(
                                            searchWord = historyItem,
                                            isIdSearch = true,
                                            searchMode = appViewMode
                                        )
                                    },
                                    endActions = {
                                        Icon(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .throttleClick {
                                                    viewModel.deleteSearchIdHistory(historyItem)
                                                },
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "delete"
                                        )
                                    },
                                )
                            }
                        } else {
                            searchHistory.forEach { item ->
                                BasicComponent(
                                    title = item.keyword,
                                    onClick = rememberThrottleClick {
                                        dispatch(SearchAction.AddSearchHistory(item.keyword))
                                        focusRequester.freeFocus()
                                        navigationManager.navigateToSearchResultScreen(
                                            searchWord = item.keyword,
                                            isIdSearch = false,
                                            searchMode = appViewMode
                                        )
                                    },
                                    endActions = {
                                        Icon(
                                            modifier = Modifier
                                                .size(24.dp)
                                                .throttleClick {
                                                    dispatch(SearchAction.DeleteSearchHistory(item.keyword))
                                                },
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = "delete"
                                        )
                                    },
                                )
                            }
                        }
                    }
                }
            } else {
                item(key = "autocomplete_card") {
                    Card {
                        state.autoCompleteSearchWords.forEach { word ->
                            BasicComponent(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .pointerInput(word.name) {
                                        detectTapGestures(onLongPress = {
                                            coroutineScope.launch {
                                                com.mrl.pixiv.common.util.copyToClipboard(word.name)
                                            }
                                        })
                                    },
                                title = word.name,
                                summary = if (word.translatedName.isNotBlank()) word.translatedName else null,
                                onClick = rememberThrottleClick {
                                    val query = textState.completionToken().replaceIn(textState.text, word.name)
                                    dispatch(SearchAction.AddSearchHistory(query))
                                    focusRequester.freeFocus()
                                    navigationManager.navigateToSearchResultScreen(
                                        searchWord = query,
                                        isIdSearch = state.isIdSearch,
                                        searchMode = appViewMode
                                    )
                                },
                            )
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }

        if (pendingLinks.isNotEmpty()) {
            PixivLinkSelectionDialog(
                links = pendingLinks,
                onSelect = { link ->
                    pendingLinks = emptyList()
                    navigationManager.navigate(link.toDestination())
                },
                onDismiss = { pendingLinks = emptyList() },
            )
        }
    }
}

@Composable
private fun PixivLinkSelectionDialog(
    links: List<PixivLinkTarget>,
    onSelect: (PixivLinkTarget) -> Unit,
    onDismiss: () -> Unit,
) {
    OverlayDialog(
        show = true,
        title = stringResource(RStrings.select_pixiv_link),
        onDismissRequest = onDismiss,
        content = {
            Column(
                modifier = Modifier
                    .heightIn(max = 400.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                links.forEach { link ->
                    val type = when (link) {
                        is PixivLinkTarget.Illust -> stringResource(RStrings.illust)
                        is PixivLinkTarget.Novel -> stringResource(RStrings.novel)
                        is PixivLinkTarget.User -> stringResource(RStrings.users)
                    }
                    BasicComponent(
                        title = "$type #${link.id}",
                        summary = link.url,
                        onClick = rememberThrottleClick {
                            onSelect(link)
                        },
                    )
                }
            }
            top.yukonga.miuix.kmp.basic.TextButton(
                text = stringResource(RStrings.cancel),
                onClick = onDismiss,
            )
        },
    )
}

@Composable
private fun SearchScreenAppBar(
    scrollBehavior: ScrollBehavior,
    textState: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    focusRequester: FocusRequester,
    onBack: () -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit = {},
) {
    var expanded by remember { mutableStateOf(false) }
    TopAppBar(
        title = stringResource(RStrings.search),
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        actions = actions,
        navigationIcon = {
            IconButton(
                onClick = onBack,
            ) {
                Icon(
                    imageVector = MiuixIcons.Back,
                    contentDescription = "Back",
                )
            }
        },
        bottomContent = {
            SearchBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                expanded = expanded,
                onExpandedChange = { expanded = it },
                inputField = {
                    Box(modifier = Modifier.focusRequester(focusRequester)) {
                        InputField(
                            query = textState.text,
                            onQueryChange = { text ->
                                onValueChange(TextFieldValue(text, TextRange(text.length)))
                            },
                            onSearch = { onSearch() },
                            expanded = expanded,
                            onExpandedChange = { expanded = it },
                            label = stringResource(RStrings.enter_keywords),
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { if (!it.isFocused) expanded = false },
                        )
                    }
                },
                content = {},
            )
        },
    )
}

@Composable
private fun SearchTypeAction(
    isIdSearch: Boolean,
    onModeChange: (Boolean) -> Unit,
) {
    val labels = listOf(
        stringResource(RStrings.keyword_search),
        stringResource(RStrings.id_search),
    )
    val selectedIndex = if (isIdSearch) 1 else 0
    val entry = remember(isIdSearch, labels, onModeChange) {
        DropdownEntry(
            labels.mapIndexed { index, label ->
                DropdownItem(
                    text = label,
                    selected = index == selectedIndex,
                    onClick = { onModeChange(index == 1) },
                )
            }
        )
    }
    OverlayIconDropdownMenu(entry = entry) {
        Icon(
            imageVector = MiuixIcons.Tune,
            contentDescription = labels[selectedIndex],
            tint = MiuixTheme.colorScheme.onBackground,
        )
    }
}

internal fun shouldShowSearchInputClearIcon(input: String): Boolean = input.isNotEmpty()

internal fun resolvePixivLinkSearchAction(
    text: String,
    alwaysShowSelection: Boolean,
): PixivLinkSearchAction {
    val links = DestinationsDeepLink.findLinks(text)
    return when {
        links.isEmpty() -> PixivLinkSearchAction.NoMatch
        alwaysShowSelection || links.size > 1 -> PixivLinkSearchAction.ShowSelection(links)
        else -> PixivLinkSearchAction.Open(links.single())
    }
}

internal sealed interface PixivLinkSearchAction {
    data object NoMatch : PixivLinkSearchAction
    data class Open(val link: PixivLinkTarget) : PixivLinkSearchAction
    data class ShowSelection(val links: List<PixivLinkTarget>) : PixivLinkSearchAction
}
