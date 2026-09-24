package com.mrl.pixiv.collection.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.collection.RestrictBookmarkTag
import com.mrl.pixiv.common.compose.lightBlue
import com.mrl.pixiv.common.data.Restrict
import com.mrl.pixiv.common.kts.round
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.common.util.conditionally
import com.mrl.pixiv.common.util.throttleClick
import com.mrl.pixiv.strings.bookmark_tags
import com.mrl.pixiv.strings.word_private
import com.mrl.pixiv.strings.word_public
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.basic.TabRow
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

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
    val scope = rememberCoroutineScope()
    var selectedTab by remember(restrict) { mutableIntStateOf(if (restrict == Restrict.PUBLIC) 0 else 1) }
    val pagerState = rememberPagerState(initialPage = selectedTab, pageCount = { 2 })
    OverlayDialog(
        show = true,
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        content = {
            Column {
                TabRow(
                    tabs = listOf(
                        stringResource(RStrings.word_public),
                        stringResource(RStrings.word_private),
                    ),
                    selectedTabIndex = selectedTab,
                    onTabSelected = { index ->
                        selectedTab = index
                        scope.launch { pagerState.animateScrollToPage(index) }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .clip(4.round),
                )
                LaunchedEffect(pagerState.currentPage) {
                    selectedTab = pagerState.currentPage
                }
                Text(
                    text = stringResource(RStrings.bookmark_tags),
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .fillMaxWidth()
                        .background(MiuixTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                        .padding(vertical = 8.dp)
                        .padding(start = 8.dp),
                    style = MiuixTheme.textStyles.footnote1,
                )
                HorizontalPager(state = pagerState) { currentPage ->
                    LaunchedEffect(Unit) {
                        onLoadUserBookmarksTags(if (currentPage == 0) Restrict.PUBLIC else Restrict.PRIVATE)
                    }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp)
                            .height(300.dp)
                    ) {
                        items(
                            if (currentPage == 0) userBookmarkTags else privateBookmarkTags,
                            key = { it.name.toString() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .throttleClick(indication = LocalIndication.current) {
                                        onSelected(
                                            if (selectedTab == 0) Restrict.PUBLIC else Restrict.PRIVATE,
                                            it.name
                                        )
                                        onDismissRequest()
                                    }
                                    .conditionally(((restrict == Restrict.PUBLIC && it.isPublic) || (restrict == Restrict.PRIVATE && !it.isPublic)) && filterTag == it.name) {
                                        Modifier.background(lightBlue, 4.round)
                                    }
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Text(text = it.displayName)
                                if (it.count != null) {
                                    Text(text = it.count.toString())
                                }
                            }
                        }
                    }
                }
            }
        },
    )
}
