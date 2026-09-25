package com.mrl.pixiv.common.compose.ui.image

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import com.mrl.pixiv.common.util.allowRgb565
import com.mrl.pixiv.common.util.throttleClick
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator

@Composable
fun UserAvatar(
    url: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    contentDescription: String = "",
    contentScale: ContentScale = ContentScale.Crop,
) {
    if (url.isEmpty()) {
        CircularProgressIndicator(modifier)
    } else {
        LoadingImage(
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .data(url)
                .allowRgb565(true)
                .build(),
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
                .clip(CircleShape)
                .throttleClick(onClick = onClick),
            loadingContent = {
                CircularProgressIndicator(modifier)
            }
        )
    }
}
