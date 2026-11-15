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
            moveTo(w * .35f, h * .37f)
            quadraticTo(w * .27f, h * .37f, w * .32f, h * .44f)
            lineTo(w * .45f, h * .60f)
            quadraticTo(w * .50f, h * .67f, w * .55f, h * .60f)
            lineTo(w * .68f, h * .44f)
            quadraticTo(w * .73f, h * .37f, w * .65f, h * .37f)
            close()
        }
        drawPath(path, color)
    }
}
