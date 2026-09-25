package com.mrl.pixiv.collection.components

import androidx.compose.runtime.Composable
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
 * Bookmark collection filter as top-app-bar dropdown groups.
 *
 * Returns one [DropdownEntry] per option group (visibility and bookmark tags); rendering the
 * entries in a single menu separates the groups with a divider.
 *
 * @param restrict Current visibility filter.
 * @param filterTag Current bookmark tag filter, `null` means all tags.
 * @param userBookmarkTags Bookmark tags of the public collection.
 * @param privateBookmarkTags Bookmark tags of the private collection.
 * @param onSelected Invoked with the newly selected visibility and bookmark tag.
 */
@Composable
fun filterDropdownEntries(
    restrict: Restrict,
    filterTag: String?,
    userBookmarkTags: ImmutableList<RestrictBookmarkTag>,
    privateBookmarkTags: ImmutableList<RestrictBookmarkTag>,
    onSelected: (restrict: Restrict, tag: String?) -> Unit,
): List<DropdownEntry> {
    val tags = if (restrict == Restrict.PUBLIC) userBookmarkTags else privateBookmarkTags
    val restrictLabels = listOf(
        stringResource(RStrings.word_public),
        stringResource(RStrings.word_private),
    )
    val restrictIndex = if (restrict == Restrict.PUBLIC) 0 else 1
    val tagIndex = tags.indexOfFirst { it.name == filterTag }
    return listOf(
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
}

/**
 * The filter action of a top app bar: an [OverlayIconDropdownMenu] with the miuix filter icon
 * that shows [entries] groups separated by dividers.
 *
 * @param entries Dropdown groups shown in the menu.
 * @param onExpandedChange Reports whether the menu is open; use it to refresh bookmark tags.
 * @param modifier Modifier.
 */
@Composable
fun FilterAction(
    entries: List<DropdownEntry>,
    onExpandedChange: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    OverlayIconDropdownMenu(
        entries = entries,
        modifier = modifier,
        onExpandedChange = onExpandedChange,
    ) {
        Icon(
            imageVector = MiuixIcons.Filter,
            contentDescription = stringResource(RStrings.filter),
            tint = MiuixTheme.colorScheme.onBackground,
        )
    }
}
