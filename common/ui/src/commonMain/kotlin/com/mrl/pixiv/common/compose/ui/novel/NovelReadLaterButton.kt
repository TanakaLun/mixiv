package com.mrl.pixiv.common.compose.ui.novel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.WatchLater
import androidx.compose.material.icons.rounded.WatchLater
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.data.Novel
import com.mrl.pixiv.common.repository.NovelReadLaterRepository
import com.mrl.pixiv.common.repository.requireUserPreferenceFlow
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.ToastUtil
import com.mrl.pixiv.strings.add_to_read_later
import com.mrl.pixiv.strings.ai_translation_config_required
import com.mrl.pixiv.strings.ai_translation_failed
import com.mrl.pixiv.strings.read_later_added
import com.mrl.pixiv.strings.read_later_removed
import com.mrl.pixiv.strings.remove_from_read_later
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.theme.LocalContentColor
import top.yukonga.miuix.kmp.theme.MiuixTheme

data class NovelReadLaterController(
    val isAdded: Boolean,
    val toggle: () -> Unit,
)

@Composable
fun rememberNovelReadLaterController(
    novel: Novel,
    repository: NovelReadLaterRepository = koinInject(),
): NovelReadLaterController {
    val preference by requireUserPreferenceFlow.collectAsStateWithLifecycle()
    val targetLanguage = preference.appLanguage
        ?.takeIf { it.isNotBlank() }
        ?: Locale.current.toLanguageTag().ifBlank { "en" }
    val itemFlow = remember(novel.id, targetLanguage) {
        repository.observeItem(
            novelId = novel.id,
            targetLanguage = targetLanguage,
        )
    }
    val item by itemFlow.collectAsStateWithLifecycle(initialValue = null)
    val scope = rememberCoroutineScope()
    val toggle: () -> Unit = {
        scope.launch {
            try {
                if (item == null) {
                    repository.enqueue(
                        novel = novel,
                        targetLanguage = targetLanguage,
                    )
                } else {
                    repository.remove(
                        novelId = novel.id,
                        targetLanguage = targetLanguage,
                    )
                }
                ToastUtil.safeShortToast(
                    if (item == null) {
                        RStrings.read_later_added
                    } else {
                        RStrings.read_later_removed
                    }
                )
            } catch (cancellation: CancellationException) {
                throw cancellation
            } catch (_: IllegalArgumentException) {
                ToastUtil.safeShortToast(RStrings.ai_translation_config_required)
            } catch (throwable: Throwable) {
                ToastUtil.safeShortToast(
                    RStrings.ai_translation_failed,
                    throwable.message.orEmpty(),
                )
            }
        }
    }
    return NovelReadLaterController(isAdded = item != null, toggle = toggle)
}

@Composable
fun NovelReadLaterIcon(
    isAdded: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current,
    contentDescription: String? = null,
) {
    Icon(
        imageVector = if (isAdded) {
            Icons.Rounded.WatchLater
        } else {
            Icons.Outlined.WatchLater
        },
        contentDescription = contentDescription,
        tint = tint,
        modifier = modifier,
    )
}

@Composable
fun NovelReadLaterButton(
    novel: Novel,
    modifier: Modifier = Modifier,
    tint: Color? = null,
    repository: NovelReadLaterRepository = koinInject(),
) {
    val controller = rememberNovelReadLaterController(novel, repository)
    val resolvedTint = tint ?: if (controller.isAdded) {
        MiuixTheme.colorScheme.primary
    } else {
        MiuixTheme.colorScheme.onSurfaceVariantSummary
    }
    IconButton(
        modifier = modifier,
        onClick = controller.toggle,
    ) {
        NovelReadLaterIcon(
            isAdded = controller.isAdded,
            contentDescription = stringResource(
                if (controller.isAdded) {
                    RStrings.remove_from_read_later
                } else {
                    RStrings.add_to_read_later
                }
            ),
            tint = resolvedTint,
        )
    }
}