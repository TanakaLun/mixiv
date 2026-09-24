package com.mrl.pixiv.common.compose.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.compose.FavoriteDualColor
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Favorites
import top.yukonga.miuix.kmp.icon.extended.FavoritesFill
import top.yukonga.miuix.kmp.icon.extended.Lock
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun BookmarkIcon(
    isBookmarked: Boolean,
    isPrivate: Boolean,
    modifier: Modifier = Modifier,
    iconSize: Dp = 24.dp,
    bookmarkedImageVector: ImageVector = MiuixIcons.FavoritesFill,
    unbookmarkedImageVector: ImageVector = MiuixIcons.Favorites,
    tint: Color = FavoriteDualColor(isBookmarked),
    contentDescription: String? = null,
) {
    Box(modifier = modifier.size(iconSize)) {
        Icon(
            imageVector = if (isBookmarked) bookmarkedImageVector else unbookmarkedImageVector,
            contentDescription = contentDescription,
            modifier = Modifier
                .align(Alignment.Center)
                .size(iconSize),
            tint = tint,
        )
        if (isBookmarked && isPrivate) {
            Icon(
                imageVector = MiuixIcons.Lock,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(iconSize * 0.46f)
                    .background(MiuixTheme.colorScheme.surface, CircleShape)
                    .padding(1.dp),
                tint = MiuixTheme.colorScheme.onSurface,
            )
        }
    }
}
