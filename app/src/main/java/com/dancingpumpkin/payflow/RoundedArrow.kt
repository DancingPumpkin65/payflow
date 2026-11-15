package com.dancingpumpkin.payflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection

internal enum class ArrowDirection { DOWN, UP, RIGHT }

@Composable
internal fun RoundedArrow(direction: ArrowDirection, description: String? = null) {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    val rtl = LocalLayoutDirection.current == LayoutDirection.Rtl
    Canvas(Modifier.size(30.dp).then(if (description == null) Modifier else Modifier.semantics { contentDescription = description }).graphicsLayer {
        rotationZ = when (direction) { ArrowDirection.DOWN -> 0f; ArrowDirection.UP -> 180f; ArrowDirection.RIGHT -> if (rtl) 90f else -90f }
    }) {
        val w = size.width; val h = size.height
        val path = Path().apply {
            moveTo(w * .24f, h * .36f)
            quadraticTo(w * .18f, h * .36f, w * .22f, h * .42f)
            lineTo(w * .45f, h * .63f)
            quadraticTo(w * .50f, h * .69f, w * .55f, h * .63f)
            lineTo(w * .78f, h * .42f)
            quadraticTo(w * .82f, h * .36f, w * .76f, h * .36f)
            close()
        }
        drawPath(path, color)
    }
}
