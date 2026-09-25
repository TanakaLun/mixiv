package com.mrl.pixiv.collection.components

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.mrl.pixiv.collection.RestrictBookmarkTag
import com.mrl.pixiv.common.data.Restrict
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.bookmark_tags
import com.mrl.pixiv.strings.collection_visibility
import com.mrl.pixiv.strings.filter
import com.mrl.pixiv.strings.word_private
import com.mrl.pixiv.strings.word_public
import kotlinx.collections.immutable.ImmutableList
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference

@Composable
fun FilterDialog(
    onDismissRequest: () -> Unit,
    userBookmarkTags: ImmutableList<RestrictBookmarkTag>,
    privateBookmarkTags: ImmutableList<RestrictBookmarkTag>,
    restrict: Restrict,
    filterTag: String?,
    onLoadUserBookmarksTags: (Restrict) -> Unit,
    onSelected: (restrict: Restrict, tag: String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val tags = if (restrict == Restrict.PUBLIC) userBookmarkTags else privateBookmarkTags
    val restrictItems = listOf(
        stringResource(RStrings.word_public),
        stringResource(RStrings.word_private),
    )
    val tagItems = tags.map { tag ->
        if (tag.count != null) "${tag.displayName} (${tag.count})" else tag.displayName
    }
    val tagIndex = tags.indexOfFirst { it.name == filterTag }
    LaunchedEffect(restrict) {
        onLoadUserBookmarksTags(restrict)
    }
    OverlayDialog(
        show = true,
        modifier = modifier,
        title = stringResource(RStrings.filter),
        onDismissRequest = onDismissRequest,
        content = {
            Column {
                OverlayDropdownPreference(
                    items = restrictItems,
                    selectedIndex = if (restrict == Restrict.PUBLIC) 0 else 1,
                    title = stringResource(RStrings.collection_visibility),
                    onSelectedIndexChange = { index ->
                        val newRestrict = if (index == 0) Restrict.PUBLIC else Restrict.PRIVATE
                        onSelected(newRestrict, filterTag)
                    },
                )
                OverlayDropdownPreference(
                    items = tagItems,
                    selectedIndex = tagIndex,
                    title = stringResource(RStrings.bookmark_tags),
                    onSelectedIndexChange = { index ->
                        onSelected(restrict, tags.getOrNull(index)?.name)
                        onDismissRequest()
                    },
                )
            }
        },
    )
}
