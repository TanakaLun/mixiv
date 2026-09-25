package com.mrl.pixiv.common.compose.ui.illust

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.mrl.pixiv.common.compose.layout.isWidthAtLeastExpanded
import com.mrl.pixiv.common.compose.ui.BookmarkIcon
import com.mrl.pixiv.common.compose.ui.IllustBottomBookmarkSheet
import com.mrl.pixiv.common.compose.ui.LongPressIconButton
import com.mrl.pixiv.common.data.AiType
import com.mrl.pixiv.common.data.Illust
import com.mrl.pixiv.common.data.Restrict
import com.mrl.pixiv.common.data.Type
import com.mrl.pixiv.common.kts.HSpacer
import com.mrl.pixiv.common.kts.round
import com.mrl.pixiv.common.repository.BlockingRepositoryV2
import com.mrl.pixiv.common.repository.SettingRepository
import com.mrl.pixiv.common.repository.requireUserPreferenceValue
import com.mrl.pixiv.common.repository.viewmodel.bookmark.isPrivateBookmark
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.allowRgb565
import com.mrl.pixiv.common.util.conditionally
import com.mrl.pixiv.common.util.throttleClick
import com.mrl.pixiv.strings.long_click_to_edit_favorite
import com.mrl.pixiv.strings.manga
import com.mrl.pixiv.strings.series
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.Badge
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TooltipAnchorPosition
import top.yukonga.miuix.kmp.basic.TooltipBox
import top.yukonga.miuix.kmp.basic.rememberTooltipState
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Copy
import top.yukonga.miuix.kmp.theme.MiuixTheme
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

@Composable
fun SquareIllustItem(
    illust: Illust,
    isBookmarked: Boolean,
    onBookmarkClick: (Restrict, List<String>?, Boolean) -> Unit,
    navToPictureScreen: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    elevation: Dp = 5.dp,
    shouldShowTip: Boolean = false,
    shape: Shape = 12.round,
    enableTransition: Boolean = !currentWindowAdaptiveInfoV2().isWidthAtLeastExpanded,
) {
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val prefix = rememberSaveable(enableTransition) { Uuid.random().toHexString() }
    val isIllustBlocked = BlockingRepositoryV2.collectIllustBlockAsState(illust.id)
    val isUserBlocked = BlockingRepositoryV2.collectUserBlockAsState(illust.user.id)
    val enableTransition = enableTransition && !isIllustBlocked && !isUserBlocked
    val onClick = {
        navToPictureScreen(prefix, enableTransition)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .shadow(elevation, shape)
            .background(MiuixTheme.colorScheme.background)
            .throttleClick { onClick() }
    ) {
        val imageKey = illust.imageUrls.squareMedium
        AsyncImage(
            modifier = Modifier
                .matchParentSize()
                .conditionally(isIllustBlocked || isUserBlocked) {
                    blur(50.dp, BlurredEdgeTreatment(shape))
                },
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(illust.imageUrls.squareMedium)
                .crossfade(1.seconds.inWholeMilliseconds.toInt())
                .allowRgb565(true)
                .placeholderMemoryCacheKey(imageKey)
                .memoryCacheKey(imageKey)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
        )
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (illust.illustAIType == AiType.AiGeneratedWorks) {
                AIBadge()
            }
            if (illust.type == Type.Ugoira) {
                GifBadge()
            }
            if (illust.type == Type.Manga) {
                TextBadge(text = stringResource(RStrings.manga))
            }
            if (illust.series != null) {
                TextBadge(text = stringResource(RStrings.series))
            }
            if (illust.pageCount > 1) {
                PageBadge(
                    pageCount = illust.pageCount,
                )
            }
        }
        if (!isUserBlocked && !isIllustBlocked) {
            BookmarkTooltipBox(
                shouldShowTip = shouldShowTip,
                modifier = Modifier.align(Alignment.BottomEnd),
            ) {
                LongPressIconButton(
                    onClick = throttleClick {
                        val restrict =
                            if (requireUserPreferenceValue.defaultPrivateBookmark) Restrict.PRIVATE else Restrict.PUBLIC
                        onBookmarkClick(restrict, null, false)
                    },
                    onLongClick = { showBottomSheet = true },
                ) {
                    BookmarkIcon(
                        isBookmarked = isBookmarked,
                        isPrivate = illust.isPrivateBookmark,
                        iconSize = 24.dp,
                        contentDescription = "",
                    )
                }
            }
        }
    }
    if (showBottomSheet) {
        IllustBottomBookmarkSheet(
            hideBottomSheet = { showBottomSheet = false },
            illust = illust,
            onBookmarkClick = onBookmarkClick,
        )
    }
}

@Composable
fun RectangleIllustItem(
    navToPictureScreen: (String, Boolean) -> Unit,
    illust: Illust,
    isBookmarked: Boolean,
    onBookmarkClick: (Restrict, List<String>?, Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enableTransition: Boolean = !currentWindowAdaptiveInfoV2().isWidthAtLeastExpanded,
    shouldShowTip: Boolean = false,
) {
    val scale = calculateIllustAspectRatio(illust.width, illust.height)
    val prefix = rememberSaveable(enableTransition) { Uuid.random().toHexString() }
    val isIllustBlocked = BlockingRepositoryV2.collectIllustBlockAsState(illust.id)
    val isUserBlocked = BlockingRepositoryV2.collectUserBlockAsState(illust.user.id)
    val enableTransition = enableTransition && !isIllustBlocked && !isUserBlocked
    var showBottomSheet by rememberSaveable { mutableStateOf(false) }
    val onBookmarkLongClick = {
        showBottomSheet = true
    }
    val context = LocalPlatformContext.current

    val shape = 10f.round
    Box(
        modifier = modifier
            .padding(horizontal = 5.dp)
            .padding(bottom = 5.dp)
            .shadow(4.dp, shape, clip = false)
            .clip(shape)
            .throttleClick {
                navToPictureScreen(prefix, enableTransition)
            }
            .background(color = MiuixTheme.colorScheme.surface, shape = shape),
    ) {
        Column {
            val imageKey = illust.imageUrls.medium
            val imageShape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
            AsyncImage(
                model = remember {
                    ImageRequest.Builder(context)
                        .data(illust.imageUrls.medium)
                        .allowRgb565(true)
                        .crossfade(1.seconds.inWholeMilliseconds.toInt())
                        .placeholderMemoryCacheKey(imageKey)
                        .memoryCacheKey(imageKey)
                        .build()
                },
                contentDescription = null,
                modifier = Modifier
                    .aspectRatio(scale)
                    .conditionally(isIllustBlocked || isUserBlocked) {
                        blur(50.dp, BlurredEdgeTreatment(imageShape))
                    }
                    .clip(imageShape),
                alignment = Alignment.TopCenter,
            )
            Row(
                modifier = Modifier
                    .padding(start = 5.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = illust.title,
                        style = MiuixTheme.textStyles.main,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = illust.user.name,
                        style = MiuixTheme.textStyles.body1,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                BookmarkTooltipBox(
                    shouldShowTip = shouldShowTip,
                ) {
                    LongPressIconButton(
                        onClick = throttleClick {
                            val restrict =
                                if (requireUserPreferenceValue.defaultPrivateBookmark) Restrict.PRIVATE else Restrict.PUBLIC
                            onBookmarkClick(restrict, null, false)
                        },
                        onLongClick = onBookmarkLongClick,
                    ) {
                        BookmarkIcon(
                            isBookmarked = isBookmarked,
                            isPrivate = illust.isPrivateBookmark,
                            iconSize = 24.dp,
                            contentDescription = "",
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(5.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (illust.illustAIType == AiType.AiGeneratedWorks) {
                AIBadge()
            }
            if (illust.type == Type.Ugoira) {
                GifBadge()
            }
            if (illust.type == Type.Manga) {
                TextBadge(text = stringResource(RStrings.manga))
            }
            if (illust.series != null) {
                TextBadge(text = stringResource(RStrings.series))
            }
            if (illust.pageCount > 1) {
                PageBadge(
                    pageCount = illust.pageCount,
                )
            }
        }
    }
    if (showBottomSheet) {
        IllustBottomBookmarkSheet(
            hideBottomSheet = { showBottomSheet = false },
            illust = illust,
            onBookmarkClick = onBookmarkClick,
        )
    }
}

internal fun calculateIllustAspectRatio(width: Int, height: Int): Float {
    return width.toFloat() / height
}

@Composable
private fun BookmarkTooltipBox(
    shouldShowTip: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val tooltipState = rememberTooltipState(isPersistent = true)

    LaunchedEffect(shouldShowTip) {
        if (shouldShowTip && !SettingRepository.userPreferenceFlow.value.hasShowBookmarkTip) {
            SettingRepository.setHasShowBookmarkTip(true)
            launch { tooltipState.show() }
            delay(3000.milliseconds)
            tooltipState.dismiss()
        }
    }

    Box(modifier = modifier) {
        TooltipBox(
            text = stringResource(RStrings.long_click_to_edit_favorite),
            state = tooltipState,
            positioning = TooltipAnchorPosition.Above,
            enabled = false,
            content = content,
        )
    }
}

@Composable
private fun TextBadge(
    text: String,
    modifier: Modifier = Modifier
) {
    Badge(
        modifier = modifier,
        containerColor = MiuixTheme.colorScheme.primaryContainer,
        contentColor = MiuixTheme.colorScheme.onPrimaryContainer,
    ) {
        Text(
            text = text,
            style = MiuixTheme.textStyles.footnote2,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

@Composable
internal fun AIBadge(
    modifier: Modifier = Modifier
) {
    Badge(
        modifier = modifier,
        containerColor = MiuixTheme.colorScheme.primaryContainer,
        contentColor = MiuixTheme.colorScheme.onPrimaryContainer,
    ) {
        Text(
            text = "AI",
            style = MiuixTheme.textStyles.footnote2,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun GifBadge(
    modifier: Modifier = Modifier
) {
    Badge(
        modifier = modifier,
        containerColor = MiuixTheme.colorScheme.primaryContainer,
        contentColor = MiuixTheme.colorScheme.onPrimaryContainer,
    ) {
        Text(
            text = "GIF",
            style = MiuixTheme.textStyles.footnote2,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
        )
    }
}

@Composable
private fun PageBadge(
    pageCount: Int,
    modifier: Modifier = Modifier,
) {
    Badge(
        modifier = modifier,
        containerColor = Color.Black.copy(alpha = 0.5f),
        contentColor = Color.White,
    ) {
        5f.HSpacer
        Icon(
            imageVector = MiuixIcons.Copy,
            contentDescription = null,
            modifier = Modifier.size(10.dp)
        )
        2f.HSpacer
        Text(
            text = "$pageCount",
            modifier = Modifier.padding(vertical = 2.dp),
            style = MiuixTheme.textStyles.footnote2,
        )
        5f.HSpacer
    }
}
