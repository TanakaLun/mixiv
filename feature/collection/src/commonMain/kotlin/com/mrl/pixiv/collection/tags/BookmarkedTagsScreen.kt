package com.mrl.pixiv.collection.tags

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.repository.BookmarkedTagRepository
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.bookmark_tags
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Delete
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.preference.ArrowPreference

@Composable
fun BookmarkedTagsScreen(
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val tags by BookmarkedTagRepository.bookmarkedTags.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.bookmark_tags),
                navigationIcon = {
                    IconButton(
                        onClick = { navigationManager.popBackStack() }
                    ) {
                        Icon(
                            imageVector = MiuixIcons.Back,
                            contentDescription = null
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        ) {
            if (tags.isNotEmpty()) {
                item(key = "bookmark_tags_card") {
                    Card {
                        tags.forEach { tag ->
                            val state = rememberSwipeToDismissBoxState { it / 3 }
                            SwipeToDismissBox(
                                state = state,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color.Red)
                                    ) {
                                        Icon(
                                            imageVector = MiuixIcons.Delete,
                                            contentDescription = null,
                                            modifier = Modifier
                                                .padding(horizontal = 10.dp)
                                                .align(
                                                    when (state.dismissDirection) {
                                                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                                                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                                                        SwipeToDismissBoxValue.Settled -> Alignment.Center
                                                    }
                                                ),
                                            tint = Color.White
                                        )
                                    }
                                },
                                onDismiss = {
                                    BookmarkedTagRepository.removeTag(tag)
                                }
                            ) {
                                ArrowPreference(
                                    title = tag.name,
                                    summary = tag.translatedName.ifEmpty { null },
                                    onClick = {
                                        navigationManager.navigateToSearchResultScreen(tag.name)
                                    },
                                )
                            }
                        }
                    }
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }
        }
    }
}
