package com.dancingpumpkin.payflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp

@Composable
internal fun EmptyEvents(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
    val availableHeight = maxHeight
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        val iconSize = (availableHeight - 32.dp).coerceIn(0.dp, 64.dp)
        if (iconSize > 0.dp) Canvas(Modifier.size(iconSize)) {
            val unit = size.width / 72f
            drawRoundRect(Color(0xFF3F444C), Offset(8 * unit, 15 * unit), Size(56 * unit, 51 * unit), CornerRadius(6 * unit))
            drawRoundRect(Color(0xFF25282E), Offset(11 * unit, 25 * unit), Size(50 * unit, 37 * unit), CornerRadius(3 * unit))
            drawLine(Color(0xFF747A84), Offset(23 * unit, 10 * unit), Offset(23 * unit, 22 * unit), 5 * unit, StrokeCap.Round)
            drawLine(Color(0xFF747A84), Offset(49 * unit, 10 * unit), Offset(49 * unit, 22 * unit), 5 * unit, StrokeCap.Round)
        }
        if (availableHeight >= 24.dp) Text("No events", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = if (iconSize > 0.dp) 8.dp else 0.dp))
    }
    }
}
