package com.mrl.pixiv.setting.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.ListPopupColumn
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.window.WindowListPopup

@Composable
fun DropDownSelector(
    modifier: Modifier = Modifier,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    current: String,
    items: @Composable ColumnScope.() -> Unit
) {
    Box {
        Row(
            modifier = modifier
                .background(
                    MiuixTheme.colorScheme.primary.copy(alpha = 0.5f),
                    RoundedCornerShape(8.dp)
                )
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = current, style = MiuixTheme.textStyles.body1)
            Icon(imageVector = Icons.Rounded.ArrowDropDown, contentDescription = null)
        }
        WindowListPopup(
            show = expanded,
            onDismissRequest = onDismissRequest,
        ) {
            ListPopupColumn {
                Column {
                    items()
                }
            }
        }
    }
}
