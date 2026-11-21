package com.mrl.pixiv.setting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mrl.pixiv.common.compose.ui.glass.GlassListPopup
import com.mrl.pixiv.common.compose.ui.glass.glassPopupAnchorBounds
import com.mrl.pixiv.common.compose.ui.glass.rememberGlassPopupBounds
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ExpandMore
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun DropDownSelector(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    current: String,
    items: @Composable ColumnScope.() -> Unit
) {
    val menuBounds = rememberGlassPopupBounds()
    Box {
        Row(
            modifier = modifier
                .glassPopupAnchorBounds(menuBounds)
                .background(
                    MiuixTheme.colorScheme.primary.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = current, style = MiuixTheme.textStyles.body1)
            Icon(imageVector = MiuixIcons.ExpandMore, contentDescription = null)
        }
        GlassListPopup(
            show = expanded,
            onDismissRequest = onDismissRequest,
            anchorBounds = menuBounds.value,
        ) {
            Column {
                items()
            }
        }
    }
}
