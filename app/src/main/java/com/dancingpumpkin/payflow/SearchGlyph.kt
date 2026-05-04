package com.dancingpumpkin.payflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun SearchGlyph(description: String? = null) {
    val color = MaterialTheme.colorScheme.onSurface
    Canvas(Modifier.size(28.dp).then(if (description == null) Modifier else Modifier.semantics { contentDescription = description })) {
        val unit = size.width / 28
        drawCircle(color, 7.2f * unit, Offset(11.5f * unit, 11.5f * unit), style = Stroke(1.8f * unit))
        drawLine(color, Offset(16.8f * unit, 16.8f * unit), Offset(23f * unit, 23f * unit), 1.8f * unit, StrokeCap.Round)
    }
}
