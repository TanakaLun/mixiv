package com.mrl.pixiv.novel

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.HideImage
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material3.SheetValue
import androidx.compose.material3.rememberBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.dropShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import co.touchlab.kermit.Logger
import com.mrl.pixiv.common.compose.layout.currentPaneLayoutInfo
import com.mrl.pixiv.common.compose.rememberThrottleClick
import com.mrl.pixiv.common.compose.ui.BlockSurface
import com.mrl.pixiv.common.compose.ui.BookmarkIcon
import com.mrl.pixiv.common.compose.ui.NovelBottomBookmarkSheet
import com.mrl.pixiv.common.compose.ui.novel.NovelReadLaterButton
import com.mrl.pixiv.common.data.AppViewMode
import com.mrl.pixiv.common.kts.spaceBy
import com.mrl.pixiv.common.repository.BlockingRepositoryV2
import com.mrl.pixiv.common.repository.NovelReadingProgress
import com.mrl.pixiv.common.repository.viewmodel.bookmark.BookmarkState
import com.mrl.pixiv.common.repository.viewmodel.bookmark.isBookmark
import com.mrl.pixiv.common.repository.viewmodel.bookmark.isPrivateBookmark
import com.mrl.pixiv.common.router.CommentType
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.StatusBarVisibilityEffect
import com.mrl.pixiv.common.viewmodel.asState
import com.mrl.pixiv.strings.ai_translation_setting
import com.mrl.pixiv.strings.back
import com.mrl.pixiv.strings.cancel
import com.mrl.pixiv.strings.chapter_next
import com.mrl.pixiv.strings.chapter_previous
import com.mrl.pixiv.strings.delete_translation
import com.mrl.pixiv.strings.export_txt_button
import com.mrl.pixiv.strings.font_size_value
import com.mrl.pixiv.strings.hide_novel
import com.mrl.pixiv.strings.line_spacing_value
import com.mrl.pixiv.strings.more
import com.mrl.pixiv.strings.novel_collection
import com.mrl.pixiv.strings.novel_hidden
import com.mrl.pixiv.strings.novel_marker
import com.mrl.pixiv.strings.novel_marker_page
import com.mrl.pixiv.strings.novel_work_information
import com.mrl.pixiv.strings.read_later
import com.mrl.pixiv.strings.regenerate_translation
import com.mrl.pixiv.strings.share_link
import com.mrl.pixiv.strings.show_novel
import com.mrl.pixiv.strings.show_original_text
import com.mrl.pixiv.strings.show_translated_text
import com.mrl.pixiv.strings.translate_novel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import top.yukonga.miuix.kmp.basic.BasicComponent
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Slider
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.icon.extended.Download
import top.yukonga.miuix.kmp.icon.extended.Info
import top.yukonga.miuix.kmp.icon.extended.Image
import top.yukonga.miuix.kmp.icon.extended.More
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.icon.extended.Settings
import top.yukonga.miuix.kmp.icon.extended.Share
import top.yukonga.miuix.kmp.icon.extended.Translate
import top.yukonga.miuix.kmp.overlay.OverlayBottomSheet
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.math.roundToInt
import kotlin.time.Duration.Companion.milliseconds

internal data class NovelTranslationListAnchor(
    val novelId: Long,
    val itemIndex: Int,
    val scrollOffset: Int,
)

internal fun shouldRestoreNovelTranslationListAnchor(
    wasTranslating: Boolean,
    isTranslating: Boolean,
    isTranslated: Boolean,
): Boolean = wasTranslating && !isTranslating && !isTranslated

internal fun resolveNovelTranslationListAnchorItemIndex(
    requestedItemIndex: Int,
    paragraphStartItemIndex: Int,
    paragraphCount: Int,
): Int = requestedItemIndex.coerceIn(
    minimumValue = 0,
    maximumValue = paragraphStartItemIndex + paragraphCount.coerceAtLeast(0),
)

@Composable
fun NovelScreen(
    novelId: Long,
    markerPage: Int? = null,
    readLaterTargetLanguage: String? = null,
    modifier: Modifier = Modifier,
    viewModel: NovelViewModel = koinViewModel {
        parametersOf(novelId, markerPage ?: 0)
    },
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val uriHandler = LocalUriHandler.current
    val paneInfo = currentPaneLayoutInfo()
    val density = LocalDensity.current
    val state = viewModel.asState()
    val chapterStateKey = novelChapterStateKey(
        entryNovelId = novelId,
        loadedNovelId = state.novel?.id,
    )
    val currentNovelId = chapterStateKey
    val isNovelBlocked = BlockingRepositoryV2.collectNovelBlockAsState(currentNovelId)
    val listState = key(chapterStateKey) {
        rememberLazyListState()
    }
    var readerContentWidthPx by remember {
        mutableIntStateOf(with(density) {
            (paneInfo.size.width.roundToPx() - 2 * 16.dp.roundToPx()).coerceAtLeast(0)
        })
    }
    var resizeReadingAnchor by remember(chapterStateKey) {
        mutableStateOf<NovelReadingProgress?>(null)
    }
    val paragraphLayoutCacheKey = state.paragraphLayoutCacheKey(
        contentWidthPx = readerContentWidthPx,
        density = density.density,
        fontScale = density.fontScale,
    )
    val paragraphLayouts = remember(paragraphLayoutCacheKey) {
        mutableStateMapOf<Int, TextLayoutResult>()
    }
    val cumulativeParagraphLengths = remember(state.paragraphs) {
        buildCumulativeParagraphLengths(state.paragraphs)
    }
    val markerPages = remember(state.paragraphSpans) {
        markerPagesForSpans(state.paragraphSpans)
    }
    var showBookmarkBottomSheet by remember { mutableStateOf(false) }
    var showMetadataBottomSheet by remember(state.novel?.id) { mutableStateOf(false) }
    var translationListAnchor by remember(state.novel?.id) {
        mutableStateOf<NovelTranslationListAnchor?>(null)
    }
    var wasTranslating by remember(state.novel?.id) { mutableStateOf(false) }

    LaunchedEffect(state.loading, state.novel?.id, readLaterTargetLanguage) {
        if (!state.loading &&
            state.novel?.id == novelId &&
            !readLaterTargetLanguage.isNullOrBlank()
        ) {
            viewModel.dispatch(
                NovelIntent.ApplyReadLaterTranslation(readLaterTargetLanguage)
            )
        }
    }

    // 沉浸逻辑: 滚动到正文区域时隐藏TopBar和FAB
    val isContentVisible by remember(listState) {
        derivedStateOf {
            listState.layoutInfo.visibleItemsInfo.firstOrNull()?.key is Int // index
        }
    }
    var manuallyShowTopBar by remember { mutableStateOf(false) }
    val showBar = !isContentVisible || manuallyShowTopBar
    val readingProgressFraction by remember(
        state.novel?.id,
        state.paragraphs,
        state.isTranslating,
        listState,
        paragraphLayoutCacheKey,
    ) {
        derivedStateOf {
            if (state.isTranslating) return@derivedStateOf 0f
            val novel = state.novel ?: return@derivedStateOf 0f
            val paragraphStartIndex =
                paragraphStartItemIndex(novel.series.title != null, novel.caption.isNotEmpty())
            buildBottomReadingProgressFraction(
                listState = listState,
                paragraphStartIndex = paragraphStartIndex,
                paragraphCount = state.paragraphs.size,
                paragraphLayouts = paragraphLayouts,
                paragraphs = state.paragraphs,
                cumulativeParagraphLengths = cumulativeParagraphLengths,
            )
        }
    }
    val currentMarkerPage by remember(state.novel?.id, state.paragraphSpans, listState) {
        derivedStateOf {
            val novel = state.novel ?: return@derivedStateOf 1
            val paragraphStartIndex =
                paragraphStartItemIndex(novel.series.title != null, novel.caption.isNotEmpty())
            val paragraphItem = listState.layoutInfo.visibleItemsInfo.firstOrNull { itemInfo ->
                itemInfo.index in paragraphStartIndex until
                        (paragraphStartIndex + state.paragraphSpans.size)
            }
            val paragraphIndex = paragraphItem
                ?.let { it.index - paragraphStartIndex }
                ?: 0
            markerPages.getOrElse(paragraphIndex) { 1 }
        }
    }

    if (!paneInfo.isSplit) {
        StatusBarVisibilityEffect(hidden = state.novel != null && !isNovelBlocked && !showBar)
    }

    LaunchedEffect(manuallyShowTopBar) {
        if (manuallyShowTopBar) {
            delay(3000.milliseconds) // 3秒后自动隐藏
            manuallyShowTopBar = false
        }
    }

    val latestState = rememberUpdatedState(state)
    val latestParagraphLayouts = rememberUpdatedState(paragraphLayouts)
    var handledRestoreVersion by remember(state.novel?.id) { mutableStateOf(-1L) }
    val saveReadingProgress = remember(listState, viewModel, state.novel?.id) {
        val chapterId = state.novel?.id
        {
            val currentState = latestState.value
            val novel = currentState.novel ?: return@remember
            if (novel.id != chapterId || currentState.isTranslating ||
                currentState.paragraphs.isEmpty() || resizeReadingAnchor != null ||
                (currentState.restoreProgress != null &&
                    handledRestoreVersion != currentState.restoreVersion)
            ) {
                return@remember
            }
            val paragraphStartIndex =
                paragraphStartItemIndex(novel.series.title != null, novel.caption.isNotEmpty())
            val firstVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index
                ?: return@remember
            val contentRange =
                paragraphStartIndex until (paragraphStartIndex + currentState.paragraphs.size)
            if (firstVisibleItemIndex !in contentRange) {
                viewModel.clearProgress(novelId = novel.id)
                return@remember
            }
            val progress = buildVisibleReadingProgress(
                listState = listState,
                paragraphStartIndex = paragraphStartIndex,
                paragraphCount = currentState.paragraphs.size,
                paragraphLayouts = latestParagraphLayouts.value,
                paragraphs = currentState.paragraphs
            ) ?: return@remember
            viewModel.saveProgress(novelId = novel.id, progress = progress)
        }
    }

    LaunchedEffect(state.novel?.id, listState) {
        snapshotFlow { listState.isScrollInProgress }
            .distinctUntilChanged()
            .drop(1)
            .filter { !it }
            .collect {
                if (resizeReadingAnchor == null) {
                    saveReadingProgress()
                }
            }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) { saveReadingProgress() }
    DisposableEffect(saveReadingProgress) {
        onDispose { saveReadingProgress() }
    }

    LaunchedEffect(state.restoreVersion, state.novel?.id, state.isTranslating, state.paragraphs) {
        if (handledRestoreVersion == state.restoreVersion) return@LaunchedEffect
        if (state.isTranslating) return@LaunchedEffect
        val novel = state.novel ?: return@LaunchedEffect
        val resolvedProgress = state.restoreProgress ?: return@LaunchedEffect
        if (state.paragraphs.isEmpty()) return@LaunchedEffect
        val paragraphStartIndex =
            paragraphStartItemIndex(novel.series.title != null, novel.caption.isNotEmpty())

        try {
            val targetItemIndex = paragraphStartIndex + resolvedProgress.paragraphIndex
            Logger.d(tag = "NovelScreen") { "Restore: paragraphStartIndex=$paragraphStartIndex, targetItemIndex=$targetItemIndex" }

            // 先滚动到目标段落；布局缓存已按正文和排版参数隔离，只会包含当前布局结果。
            listState.scrollToItem(targetItemIndex, 0)

            // 等待目标段落的布局完成。包含图片标记的段落可能没有文本布局，这里做超时兜底。
            val layout = withTimeoutOrNull(500L.milliseconds) {
                while (latestParagraphLayouts.value[resolvedProgress.paragraphIndex] == null) {
                    delay(16.milliseconds)
                }
                latestParagraphLayouts.value[resolvedProgress.paragraphIndex]
            } ?: run {
                Logger.d(tag = "NovelScreen") {
                    "Restore: paragraphIndex=${resolvedProgress.paragraphIndex} has no text layout, keep item-top restore."
                }
                return@LaunchedEffect
            }

            val targetParagraph = state.paragraphs[resolvedProgress.paragraphIndex]
            val targetCharIndex = resolvedProgress.charIndex.coerceIn(0, targetParagraph.length)

            // 根据字符位置计算所在行数
            val lineIndex = layout.getLineForOffset(targetCharIndex)

            // 获取该行顶部的Y坐标
            val lineTop = layout.getLineTop(lineIndex)

            // 补偿LazyColumn的内边距（如果有的话）
            val beforeContentPaddingCompensation =
                (-listState.layoutInfo.viewportStartOffset).coerceAtLeast(0)

            // 计算最终偏移量：将该行的顶部与视口顶部对齐
            val offset = (lineTop + beforeContentPaddingCompensation).toInt().coerceAtLeast(0)

            Logger.d(tag = "NovelScreen") {
                "Restore: paragraphIndex=${resolvedProgress.paragraphIndex}, " +
                        "charIndex=$targetCharIndex, lineIndex=$lineIndex, " +
                        "lineTop=$lineTop, offset=$offset"
            }

            // 执行滚动，将目标行的顶部与视口顶部对齐
            listState.scrollToItem(targetItemIndex, offset)
        } finally {
            // 等待滚动后的最后一帧，避免将恢复过程中的临时位置保存为阅读进度。
            withFrameNanos { }
            handledRestoreVersion = state.restoreVersion
        }
    }

    // A drag keeps the character that was visible before reflow; it never reapplies the saved bookmark.
    LaunchedEffect(paragraphLayoutCacheKey) {
        val anchor = resizeReadingAnchor ?: return@LaunchedEffect
        val novel = state.novel ?: return@LaunchedEffect
        delay(80.milliseconds)
        if (listState.isScrollInProgress || latestState.value.isTranslating) {
            resizeReadingAnchor = null
            return@LaunchedEffect
        }
        val layout = withTimeoutOrNull(500.milliseconds) {
            while (paragraphLayouts[anchor.paragraphIndex] == null) {
                delay(16.milliseconds)
            }
            paragraphLayouts[anchor.paragraphIndex]
        }
        if (layout != null) {
            val paragraph = state.paragraphs.getOrNull(anchor.paragraphIndex)
            if (paragraph != null && paragraph.hashCode() == anchor.paragraphHash) {
                val line = layout.getLineForOffset(anchor.charIndex.coerceIn(0, paragraph.length))
                val padding = (-listState.layoutInfo.viewportStartOffset).coerceAtLeast(0)
                listState.scrollToItem(
                    index = paragraphStartItemIndex(novel.series.title != null, novel.caption.isNotEmpty()) +
                            anchor.paragraphIndex,
                    scrollOffset = (layout.getLineTop(line) + padding).toInt().coerceAtLeast(0),
                )
                // Keep the resize marker through the scroll-idle observer's next frame.
                withFrameNanos { }
            }
        }
        resizeReadingAnchor = null
    }

    LaunchedEffect(state.novel?.id, state.isTranslating) {
        val novel = state.novel ?: return@LaunchedEffect
        val previouslyTranslating = wasTranslating
        wasTranslating = state.isTranslating
        if (!previouslyTranslating && state.isTranslating) {
            val paragraphStartIndex =
                paragraphStartItemIndex(novel.series.title != null, novel.caption.isNotEmpty())
            paragraphLayouts.clear()
            listState.scrollToItem(paragraphStartIndex, 0)
        } else if (
            shouldRestoreNovelTranslationListAnchor(
                wasTranslating = previouslyTranslating,
                isTranslating = state.isTranslating,
                isTranslated = state.isTranslated,
            )
        ) {
            val anchorToRestore = translationListAnchor
            translationListAnchor = null
            anchorToRestore
                ?.takeIf { it.novelId == novel.id }
                ?.let { anchor ->
                    val paragraphStartIndex =
                        paragraphStartItemIndex(
                            novel.series.title != null,
                            novel.caption.isNotEmpty(),
                        )
                    val resolvedItemIndex = resolveNovelTranslationListAnchorItemIndex(
                        requestedItemIndex = anchor.itemIndex,
                        paragraphStartItemIndex = paragraphStartIndex,
                        paragraphCount = state.paragraphSpans.size,
                    )
                    listState.scrollToItem(
                        index = resolvedItemIndex,
                        scrollOffset = if (resolvedItemIndex == anchor.itemIndex) {
                            anchor.scrollOffset
                        } else {
                            0
                        },
                    )
                }
        }

        if (previouslyTranslating && !state.isTranslating) {
            translationListAnchor = null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.addHistory()
        }
    }

    val requestTranslation: () -> Unit = request@{
        val novel = state.novel ?: return@request
        translationListAnchor = NovelTranslationListAnchor(
            novelId = novel.id,
            itemIndex = listState.firstVisibleItemIndex,
            scrollOffset = listState.firstVisibleItemScrollOffset,
        )
        saveReadingProgress()
        viewModel.dispatch(
            NovelIntent.TranslateNovel(forceRefresh = state.isTranslated)
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            AnimatedVisibility(
                visible = showBar,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                Row(
                    horizontalArrangement = 8.spaceBy
                ) {
                    // 上一章按钮
                    if (state.prevNovelId != null) {
                        FloatingActionButton(
                            onClick = {
                                saveReadingProgress()
                                viewModel.dispatch(NovelIntent.NavigateToChapter(state.prevNovelId))
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(RStrings.chapter_previous)
                            )
                        }
                    }

                    // 下一章按钮
                    if (state.nextNovelId != null) {
                        FloatingActionButton(
                            onClick = {
                                saveReadingProgress()
                                viewModel.dispatch(NovelIntent.NavigateToChapter(state.nextNovelId))
                            }
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = stringResource(RStrings.chapter_next)
                            )
                        }
                    }
                }
            }
        },
    ) { paddingValues ->
        when {
            state.loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.novel != null -> {
                Box(
                    modifier = Modifier.padding(paddingValues),
                ) {
                    if (isNovelBlocked) {
                        BlockSurface(
                            modifier = Modifier.fillMaxSize(),
                            icon = {
                                Icon(
                                    imageVector = Icons.Rounded.HideImage,
                                    contentDescription = null,
                                    modifier = Modifier.size(100.dp),
                                )
                            },
                            title = {
                                Text(
                                    text = stringResource(RStrings.novel_hidden),
                                    style = MiuixTheme.textStyles.main,
                                )
                            },
                            button = {
                                Button(
                                    onClick = viewModel::removeBlockNovel
                                ) {
                                    Text(text = stringResource(RStrings.show_novel))
                                }
                            }
                        )
                    } else {
                        NovelReaderContent(
                            state = state,
                            listState = listState,
                            readingProgressFraction = readingProgressFraction,
                            onParagraphTextLayout = { paragraphIndex, layout ->
                                // Keep results isolated until the new width's cache is composed. Keep old
                                // layouts intact until that callback captures the character anchor.
                                if (layout.layoutInput.constraints.maxWidth == paragraphLayoutCacheKey.contentWidthPx) {
                                    paragraphLayouts[paragraphIndex] = layout
                                }
                            },
                            paragraphLayoutCacheKey = paragraphLayoutCacheKey,
                            onContentWidthChanged = { width ->
                                if (width != readerContentWidthPx) {
                                    if (resizeReadingAnchor == null && !state.isTranslating) {
                                        resizeReadingAnchor = buildVisibleReadingProgress(
                                            listState = listState,
                                            paragraphStartIndex = paragraphStartItemIndex(
                                                state.novel.series.title != null,
                                                state.novel.caption.isNotEmpty(),
                                            ),
                                            paragraphCount = state.paragraphs.size,
                                            paragraphLayouts = paragraphLayouts,
                                            paragraphs = state.paragraphs,
                                        )
                                    }
                                    readerContentWidthPx = width
                                }
                            },
                            onContentClick = {
                                manuallyShowTopBar = !manuallyShowTopBar
                            },
                            onTagClick = { tag ->
                                navigationManager.navigateToSearchResultScreen(
                                    searchWord = tag,
                                    isIdSearch = false,
                                    searchMode = AppViewMode.NOVEL
                                )
                            },
                            onPixivImageClick = { illustId ->
                                navigationManager.navigateToSinglePictureScreen(illustId)
                            },
                            onAuthorClick = { userId ->
                                navigationManager.navigateToProfileDetailScreen(userId)
                            },
                            onSeriesClick = { seriesId ->
                                navigationManager.navigateToNovelSeriesScreen(seriesId)
                            },
                            onCaptionLinkClick = { url ->
                                when (val target = resolveNovelCaptionLink(url)) {
                                    is NovelCaptionLinkTarget.Illust ->
                                        navigationManager.navigateToSinglePictureScreen(target.id)

                                    is NovelCaptionLinkTarget.Novel ->
                                        navigationManager.navigateToNovelDetailScreen(target.id)

                                    is NovelCaptionLinkTarget.User ->
                                        navigationManager.navigateToProfileDetailScreen(target.id)

                                    is NovelCaptionLinkTarget.External ->
                                        runCatching { uriHandler.openUri(target.url) }

                                    null -> Unit
                                }
                            },
                            onCommentClick = {
                                saveReadingProgress()
                                navigationManager.navigateToCommentScreen(
                                    state.novel.id,
                                    CommentType.NOVEL
                                )
                            }
                        )
                    }
                    AnimatedVisibility(
                        visible = showBar,
                        enter = slideInVertically(initialOffsetY = { -it }),
                        exit = slideOutVertically(targetOffsetY = { -it })
                    ) {
                        val topBarColor = MiuixTheme.colorScheme.surface
                        TopAppBar(
                            title = "",
                            modifier = Modifier.dropShadow(RectangleShape) {
                                radius = 2f
                                color = topBarColor
                                val isExit = transition.currentState == EnterExitState.Visible &&
                                        transition.targetState == EnterExitState.PostExit
                                alpha = if (isContentVisible && !isExit) 1f else 0f
                            },
                            navigationIcon = {
                                IconButton(onClick = navigationManager::popBackStack) {
                                    Icon(
                                        MiuixIcons.Back,
                                        contentDescription = stringResource(RStrings.back)
                                    )
                                }
                            },
                            actions = {
                                if (!isNovelBlocked) {
                                    if (state.isTranslating) {
                                        IconButton(
                                            onClick = {
                                                viewModel.dispatch(NovelIntent.CancelTranslation)
                                            }
                                        ) {
                                            Icon(
                                                imageVector = MiuixIcons.Close,
                                                contentDescription = stringResource(RStrings.cancel)
                                            )
                                        }
                                    } else if (!state.isTranslated) {
                                        IconButton(onClick = requestTranslation) {
                                            Icon(
                                                imageVector = MiuixIcons.Translate,
                                                contentDescription = stringResource(
                                                    RStrings.translate_novel
                                                )
                                            )
                                        }
                                    }
                                    androidx.compose.material3.IconButton(
                                        onClick = { viewModel.dispatch(NovelIntent.ToggleBookmark) },
                                        onLongClick = { showBookmarkBottomSheet = true }
                                    ) {
                                        val isBookmark = state.novel.isBookmark
                                        BookmarkIcon(
                                            isBookmarked = isBookmark,
                                            isPrivate = state.novel.isPrivateBookmark,
                                            bookmarkedImageVector = Icons.Rounded.Favorite,
                                            unbookmarkedImageVector = Icons.Rounded.FavoriteBorder,
                                            tint = LocalContentColor.current,
                                            contentDescription = stringResource(RStrings.novel_collection),
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.dispatch(
                                                NovelIntent.ToggleMarker(currentMarkerPage)
                                            )
                                        },
                                        enabled = !state.markerUpdating && !state.isTranslating,
                                    ) {
                                        if (state.markerUpdating) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.dp,
                                            )
                                        } else {
                                            Icon(
                                                imageVector = if (
                                                    state.markerPage == currentMarkerPage
                                                ) {
                                                    Icons.Rounded.Bookmark
                                                } else {
                                                    Icons.Rounded.BookmarkBorder
                                                },
                                                contentDescription = if (state.markerPage != null) {
                                                    stringResource(
                                                        RStrings.novel_marker_page,
                                                        state.markerPage,
                                                    )
                                                } else {
                                                    stringResource(RStrings.novel_marker)
                                                },
                                            )
                                        }
                                    }
                                    IconButton(
                                        onClick = { showMetadataBottomSheet = true }
                                    ) {
                                        Icon(
                                            MiuixIcons.Info,
                                            contentDescription = stringResource(
                                                RStrings.novel_work_information
                                            )
                                        )
                                    }
                                    IconButton(
                                        onClick = { viewModel.dispatch(NovelIntent.ToggleBottomSheet) }
                                    ) {
                                        Icon(
                                            MiuixIcons.More,
                                            contentDescription = stringResource(RStrings.more)
                                        )
                                    }
                                }
                            },
                            color = Color.Transparent,
                        )
                    }
                }
            }
        }
    }

    if (showBookmarkBottomSheet && state.novel != null) {
        val bottomSheetState = rememberBottomSheetState(
            initialValue = SheetValue.Hidden,
            enabledValues = setOf(SheetValue.Hidden, SheetValue.Expanded),
        )
        NovelBottomBookmarkSheet(
            hideBottomSheet = { showBookmarkBottomSheet = false },
            novel = state.novel,
            bottomSheetState = bottomSheetState,
            onBookmarkClick = { restrict, tags, isEdit ->
                if (isEdit || !state.novel.isBookmark) {
                    BookmarkState.bookmarkNovel(state.novel.id, restrict, tags)
                } else {
                    BookmarkState.deleteBookmarkNovel(state.novel.id)
                }
            }
        )
    }

    if (showMetadataBottomSheet && state.novel != null && !isNovelBlocked) {
        NovelMetadataBottomSheet(
            novel = state.novel,
            onDismissRequest = { showMetadataBottomSheet = false },
            onAuthorClick = { userId ->
                showMetadataBottomSheet = false
                navigationManager.navigateToProfileDetailScreen(userId)
            },
            onSeriesClick = { seriesId ->
                showMetadataBottomSheet = false
                navigationManager.navigateToNovelSeriesScreen(seriesId)
            },
            onTagClick = { tag ->
                showMetadataBottomSheet = false
                navigationManager.navigateToSearchResultScreen(
                    searchWord = tag,
                    isIdSearch = false,
                    searchMode = AppViewMode.NOVEL
                )
            },
            onCaptionLinkClick = { url ->
                showMetadataBottomSheet = false
                when (val target = resolveNovelCaptionLink(url)) {
                    is NovelCaptionLinkTarget.Illust ->
                        navigationManager.navigateToSinglePictureScreen(target.id)

                    is NovelCaptionLinkTarget.Novel ->
                        navigationManager.navigateToNovelDetailScreen(target.id)

                    is NovelCaptionLinkTarget.User ->
                        navigationManager.navigateToProfileDetailScreen(target.id)

                    is NovelCaptionLinkTarget.External ->
                        runCatching { uriHandler.openUri(target.url) }

                    null -> Unit
                }
            },
            onCommentClick = {
                saveReadingProgress()
                showMetadataBottomSheet = false
                navigationManager.navigateToCommentScreen(
                    state.novel.id,
                    CommentType.NOVEL
                )
            },
        )
    }

    // BottomSheet
    OverlayBottomSheet(
        show = state.showBottomSheet,
        onDismissRequest = { viewModel.dispatch(NovelIntent.ToggleBottomSheet) },
    ) {
            NovelBottomSheetContent(
                state = state,
                onFontSizeChange = {
                    saveReadingProgress()
                    viewModel.dispatch(NovelIntent.UpdateFontSize(it))
                },
                onLineSpacingChange = {
                    saveReadingProgress()
                    viewModel.dispatch(NovelIntent.UpdateLineSpacing(it))
                },
                onExport = { viewModel.dispatch(NovelIntent.ExportToTxt) },
                onShare = { viewModel.dispatch(NovelIntent.ShareNovel) },
                onToggleDisplayedText = {
                    viewModel.dispatch(NovelIntent.ToggleDisplayOriginalText)
                },
                onDeleteTranslation = {
                    viewModel.dispatch(NovelIntent.DeleteNovelTranslation)
                },
                onRegenerateTranslation = {
                    viewModel.dispatch(NovelIntent.ToggleBottomSheet)
                    requestTranslation()
                },
                onAiSetting = {
                    viewModel.dispatch(NovelIntent.ToggleBottomSheet)
                    navigationManager.navigateToAiTranslationSettingScreen()
                },
                isNovelBlocked = isNovelBlocked,
                onBlockNovel = {
                    if (isNovelBlocked) {
                        viewModel.removeBlockNovel()
                    } else {
                        viewModel.blockNovel()
                    }
                    viewModel.dispatch(NovelIntent.ToggleBottomSheet)
                },
            )
        }
    }

@Composable
private fun NovelBottomSheetContent(
    state: NovelState,
    onFontSizeChange: (Int) -> Unit,
    onLineSpacingChange: (Int) -> Unit,
    onExport: () -> Unit,
    onShare: () -> Unit,
    onToggleDisplayedText: () -> Unit,
    onDeleteTranslation: () -> Unit,
    onRegenerateTranslation: () -> Unit,
    onAiSetting: () -> Unit,
    isNovelBlocked: Boolean,
    onBlockNovel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
    ) {
        // 字号调整
        BasicComponent(
            title = stringResource(RStrings.font_size_value, state.fontSize),
            bottomAction = {
                Slider(
                    value = state.fontSize.toFloat(),
                    onValueChange = { onFontSizeChange(it.roundToInt()) },
                    valueRange = 10f..32f,
                    steps = 21
                )
            },
        )

        // 行间距调整
        BasicComponent(
            title = stringResource(
                RStrings.line_spacing_value,
                (if (state.lineSpacingSp >= 0) "+" else "") + state.lineSpacingSp.toString()
            ),
            bottomAction = {
                Slider(
                    value = state.lineSpacingSp.toFloat(),
                    onValueChange = { onLineSpacingChange(it.roundToInt()) },
                    valueRange = -10f..10f,
                    steps = 19
                )
            },
        )

        // 导出按钮
        BasicComponent(
            title = stringResource(RStrings.export_txt_button),
            startAction = {
                Icon(
                    imageVector = MiuixIcons.Download,
                    contentDescription = stringResource(RStrings.export_txt_button)
                )
            },
            onClick = rememberThrottleClick(onClick = onExport),
        )

        // 分享按钮
        BasicComponent(
            title = stringResource(RStrings.share_link),
            startAction = {
                Icon(
                    imageVector = MiuixIcons.Share,
                    contentDescription = stringResource(RStrings.share_link)
                )
            },
            onClick = rememberThrottleClick(onClick = onShare),
        )

        state.novel?.let { novel ->
            BasicComponent(
                title = stringResource(RStrings.read_later),
                endActions = {
                    NovelReadLaterButton(
                        novel = novel,
                        tint = LocalContentColor.current,
                    )
                },
            )
        }

        if (state.isTranslated && !state.isTranslating) {
            BasicComponent(
                title = stringResource(RStrings.regenerate_translation),
                startAction = {
                    Icon(
                        imageVector = MiuixIcons.Refresh,
                        contentDescription = stringResource(RStrings.regenerate_translation)
                    )
                },
                onClick = rememberThrottleClick(onClick = onRegenerateTranslation),
            )

            BasicComponent(
                title = stringResource(
                    if (state.isShowingOriginalText) {
                        RStrings.show_translated_text
                    } else {
                        RStrings.show_original_text
                    }
                ),
                startAction = {
                    Icon(
                        imageVector = if (state.isShowingOriginalText) {
                            MiuixIcons.Translate
                        } else {
                            Icons.Rounded.Visibility
                        },
                        contentDescription = stringResource(
                            if (state.isShowingOriginalText) {
                                RStrings.show_translated_text
                            } else {
                                RStrings.show_original_text
                            }
                        )
                    )
                },
                onClick = rememberThrottleClick(onClick = onToggleDisplayedText),
            )

            BasicComponent(
                title = stringResource(RStrings.delete_translation),
                startAction = {
                    Icon(
                        imageVector = MiuixIcons.Delete,
                        contentDescription = stringResource(RStrings.delete_translation)
                    )
                },
                onClick = rememberThrottleClick(onClick = onDeleteTranslation),
            )
        }

        BasicComponent(
            title = stringResource(
                if (isNovelBlocked) RStrings.show_novel else RStrings.hide_novel
            ),
            startAction = {
                Icon(
                    imageVector = if (isNovelBlocked) MiuixIcons.Image else Icons.Rounded.HideImage,
                    contentDescription = stringResource(
                        if (isNovelBlocked) RStrings.show_novel else RStrings.hide_novel
                    )
                )
            },
            onClick = rememberThrottleClick(onClick = onBlockNovel),
        )

        BasicComponent(
            title = stringResource(RStrings.ai_translation_setting),
            startAction = {
                Icon(
                    imageVector = MiuixIcons.Settings,
                    contentDescription = stringResource(RStrings.ai_translation_setting)
                )
            },
            onClick = rememberThrottleClick(onClick = onAiSetting),
        )
    }
}
