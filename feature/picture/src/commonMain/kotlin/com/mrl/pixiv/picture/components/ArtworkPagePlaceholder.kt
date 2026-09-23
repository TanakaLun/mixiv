package com.mrl.pixiv.picture.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer

@Composable
internal fun rememberArtworkPagePlaceholder(page: Int, pageCount: Int): Painter {
    val textMeasurer = rememberTextMeasurer()
    val style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
    return remember(page, pageCount, textMeasurer, style) {
        val label = textMeasurer.measure("$page / $pageCount", style)
        object : Painter() {
            override val intrinsicSize = Size(240f, 160f)

            override fun DrawScope.onDraw() {
                drawText(label, topLeft = Offset(
                    (size.width - label.size.width) / 2,
                    (size.height - label.size.height) / 2,
                ))
            }
        }
    }
}
