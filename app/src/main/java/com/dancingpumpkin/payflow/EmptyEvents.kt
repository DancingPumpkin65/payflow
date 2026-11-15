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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.dp

@Composable
internal fun EmptyEvents(modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier) {
    val availableHeight = maxHeight
    Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        val iconSize = (availableHeight - 32.dp).coerceIn(0.dp, 96.dp)
        if (iconSize > 0.dp) Canvas(Modifier.size(iconSize)) {
            val unit = size.width / 96f
            drawRoundRect(Color(0xFF81858C), Offset(10 * unit, 23 * unit), Size(76 * unit, 67 * unit), CornerRadius(7 * unit))
            drawRoundRect(Color(0xFF565B64), Offset(14 * unit, 17 * unit), Size(68 * unit, 69 * unit), CornerRadius(6 * unit))
            drawRoundRect(Color(0xFF25272D), Offset(17 * unit, 15 * unit), Size(62 * unit, 66 * unit), CornerRadius(6 * unit))
            drawRoundRect(Color(0xFF4D5158), Offset(17 * unit, 15 * unit), Size(62 * unit, 23 * unit), CornerRadius(6 * unit))
            drawRect(Color(0xFF4D5158), Offset(17 * unit, 29 * unit), Size(62 * unit, 10 * unit))
            val page = Path().apply {
                moveTo(17 * unit, 39 * unit); lineTo(79 * unit, 39 * unit)
                cubicTo(79 * unit, 56 * unit, 76 * unit, 67 * unit, 71 * unit, 77 * unit)
                lineTo(14 * unit, 77 * unit)
                cubicTo(18 * unit, 65 * unit, 19 * unit, 53 * unit, 17 * unit, 39 * unit)
                close()
            }
            drawPath(page, Color(0xFF202228))
            for (x in listOf(31f, 65f)) {
                drawLine(Color(0xFF30333A), Offset(x * unit, 12 * unit), Offset(x * unit, 25 * unit), 10 * unit, StrokeCap.Round)
                drawLine(Color(0xFF777D87), Offset(x * unit, 9 * unit), Offset(x * unit, 23 * unit), 6 * unit, StrokeCap.Round)
            }
        }
        if (availableHeight >= 24.dp) Text("No events", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = if (iconSize > 0.dp) 8.dp else 0.dp))
    }
    }
}
