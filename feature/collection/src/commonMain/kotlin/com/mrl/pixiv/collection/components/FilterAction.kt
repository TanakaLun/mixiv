package com.mrl.pixiv.collection.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.mrl.pixiv.collection.RestrictBookmarkTag
import com.mrl.pixiv.common.data.Restrict
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.filter
import com.mrl.pixiv.strings.word_private
import com.mrl.pixiv.strings.word_public
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.DropdownEntry
import top.yukonga.miuix.kmp.basic.DropdownItem
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Filter
import top.yukonga.miuix.kmp.menu.OverlayIconDropdownMenu
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * Collection filter as a top-app-bar action.
 *
 * Replaces the old filter dialog with an [OverlayIconDropdownMenu] whose groups (visibility and
 * bookmark tags) are separated by a divider. Bookmark tags are loaded when the menu is expanded.
 *
 * @param restrict Current visibility filter.
 * @param filterTag Current bookmark tag filter, `null` means all tags.
 * @param userBookmarkTags Bookmark tags of the public collection.
 * @param privateBookmarkTags Bookmark tags of the private collection.
 * @param onLoadUserBookmarksTags Requests the bookmark tags of [restrict].
 * @param onSelected Invoked with the newly selected visibility and bookmark tag.
 * @param modifier Modifier.
 */
@Composable
fun FilterAction(
    restrict: Restrict,
    filterTag: String?,
    userBookmarkTags: ImmutableList<RestrictBookmarkTag>,
    privateBookmarkTags: ImmutableList<RestrictBookmarkTag>,
    onLoadUserBookmarksTags: (Restrict) -> Unit,
    onSelected: (restrict: Restrict, tag: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    LaunchedEffect(expanded, restrict) {
        if (expanded) onLoadUserBookmarksTags(restrict)
    }
    val tags = if (restrict == Restrict.PUBLIC) userBookmarkTags else privateBookmarkTags
    val restrictLabels = listOf(
        stringResource(RStrings.word_public),
        stringResource(RStrings.word_private),
    )
    val filterLabel = stringResource(RStrings.filter)
    val restrictIndex = if (restrict == Restrict.PUBLIC) 0 else 1
    val tagIndex = tags.indexOfFirst { it.name == filterTag }
    val entries = listOf(
        DropdownEntry(
            restrictLabels.mapIndexed { index, label ->
                DropdownItem(
                    text = label,
                    selected = index == restrictIndex,
                    onClick = {
                        val newRestrict = if (index == 0) Restrict.PUBLIC else Restrict.PRIVATE
                        onSelected(newRestrict, filterTag)
                    },
                )
            }
        ),
        DropdownEntry(
            tags.mapIndexed { index, tag ->
                DropdownItem(
                    text = if (tag.count != null) "${tag.displayName} (${tag.count})" else tag.displayName,
                    selected = index == tagIndex,
                    onClick = { onSelected(restrict, tag.name) },
                )
            }
        ),
    )
    OverlayIconDropdownMenu(
        entries = entries,
        modifier = modifier,
        onExpandedChange = { expanded = it },
    ) {
        Icon(
            imageVector = MiuixIcons.Filter,
            contentDescription = filterLabel,
            tint = MiuixTheme.colorScheme.onBackground,
        )
    }
}
