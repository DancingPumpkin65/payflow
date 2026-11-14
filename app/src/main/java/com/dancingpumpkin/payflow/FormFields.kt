package com.dancingpumpkin.payflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp

/** Keep the dialog's actions fixed while fields scroll, with a visible continuation cue. */
@Composable
internal fun FormFields(content: @Composable ColumnScope.() -> Unit) {
    val scroll = rememberScrollState()
    val thumb = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val surface = MaterialTheme.colorScheme.background
    var viewportHeight by remember { mutableIntStateOf(0) }
    var contentHeight by remember { mutableIntStateOf(0) }
    Column(Modifier.fillMaxSize().onSizeChanged { viewportHeight = it.height }) {
        Column(
            Modifier.weight(1f, fill = false).drawWithContent {
                drawContent()
                if (scroll.maxValue > 0) {
                    val edge = 8.dp.toPx().coerceAtMost(size.height / 4)
                    if (scroll.canScrollBackward && edge > 0) drawRect(
                        Brush.verticalGradient(listOf(surface, Color.Transparent), endY = edge),
                        size = Size(size.width, edge),
                    )
                    if (scroll.canScrollForward && edge > 0) drawRect(
                        Brush.verticalGradient(listOf(Color.Transparent, surface), startY = size.height - edge, endY = size.height),
                        topLeft = Offset(0f, size.height - edge), size = Size(size.width, edge),
                    )
                    val height = (size.height * size.height / (size.height + scroll.maxValue)).coerceAtLeast(24.dp.toPx()).coerceAtMost(size.height)
                    val top = scroll.value.toFloat() / scroll.maxValue * (size.height - height)
                    drawRoundRect(thumb, Offset(size.width - 2.dp.toPx(), top), Size(2.dp.toPx(), height), CornerRadius(1.dp.toPx()))
                }
            }.verticalScroll(scroll).onSizeChanged { contentHeight = it.height }.padding(end = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = content,
        )
        if (contentHeight > viewportHeight) Text(
            "Scroll to see all fields",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 8.dp),
        )
    }
}
