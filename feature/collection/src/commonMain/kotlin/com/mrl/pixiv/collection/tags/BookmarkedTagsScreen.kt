package com.mrl.pixiv.collection.tags

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mrl.pixiv.common.compose.ui.SwipeToDeleteBox
import com.mrl.pixiv.common.compose.ui.glass.IconButton
import com.mrl.pixiv.common.compose.ui.glass.Scaffold
import com.mrl.pixiv.common.compose.ui.glass.TopAppBar
import com.mrl.pixiv.common.compose.ui.pageScrollModifiers
import com.mrl.pixiv.common.repository.BookmarkedTagRepository
import com.mrl.pixiv.common.router.NavigationManager
import com.mrl.pixiv.common.router.currentNavigationManager
import com.mrl.pixiv.common.util.RStrings
import com.mrl.pixiv.strings.bookmark_tags
import org.jetbrains.compose.resources.stringResource
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.preference.ArrowPreference

@Composable
fun BookmarkedTagsScreen(
    modifier: Modifier = Modifier,
    navigationManager: NavigationManager = currentNavigationManager(),
) {
    val tags by BookmarkedTagRepository.bookmarkedTags.collectAsStateWithLifecycle()

    val scrollBehavior = MiuixScrollBehavior()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = stringResource(RStrings.bookmark_tags),
                scrollBehavior = scrollBehavior,
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
                .fillMaxSize()
                .pageScrollModifiers(scrollBehavior),
            contentPadding = PaddingValues(
                start = 12.dp,
                top = innerPadding.calculateTopPadding() + 8.dp,
                end = 12.dp,
                bottom = innerPadding.calculateBottomPadding() + 8.dp,
            ),
        ) {
            if (tags.isNotEmpty()) {
                item(key = "bookmark_tags_card") {
                    Card {
                        tags.forEach { tag ->
                            SwipeToDeleteBox(
                                onDelete = {
                                    BookmarkedTagRepository.removeTag(tag)
                                },
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
