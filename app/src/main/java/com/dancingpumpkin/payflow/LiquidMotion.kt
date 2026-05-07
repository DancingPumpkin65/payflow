package com.dancingpumpkin.payflow

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import kotlin.math.*

/** A pinched neck overlaps both circles so their boundaries join without blur. */
internal fun DrawScope.liquidBridge(a: Offset, b: Offset, ra: Float, rb: Float, color: Color, tension: Float = 1f, neckRatio: Float = .3f) {
    val distance = (b - a).getDistance()
    if (distance < 1f || tension <= 0f) return
    val u = (b - a) / distance
    val n = Offset(-u.y, u.x)
    val neck = min(ra, rb) * neckRatio * tension
    val p = Path().apply {
        moveTo((a + n * ra * .8f).x, (a + n * ra * .8f).y)
        val c1 = a + u * distance * .48f + n * neck
        val c2 = b - u * distance * .48f + n * neck
        val end = b + n * rb * .8f
        cubicTo(c1.x, c1.y, c2.x, c2.y, end.x, end.y)
        val bottom = b - n * rb * .8f
        lineTo(bottom.x, bottom.y)
        val c3 = b - u * distance * .48f - n * neck
        val c4 = a + u * distance * .48f - n * neck
        val start = a - n * ra * .8f
        cubicTo(c3.x, c3.y, c4.x, c4.y, start.x, start.y)
        close()
    }
    drawPath(p, color)
}

@Composable
internal fun LiquidWorkedSwitch(checked: Boolean, enabled: Boolean, description: String, change: () -> Unit) {
    val position by animateFloatAsState(if (checked) 1f else 0f, spring(dampingRatio = .72f, stiffness = 480f), label = "Liquid toggle")
    val interaction = remember { MutableInteractionSource() }
    val on = MaterialTheme.colorScheme.primary
    val off = MaterialTheme.colorScheme.surfaceContainerHighest
    val thumb = MaterialTheme.colorScheme.onSurface
    Canvas(Modifier.size(64.dp, 48.dp).then(pressFeedback(interaction)).toggleable(checked, enabled = enabled, role = Role.Switch, interactionSource = interaction, indication = null, onValueChange = { change() }).semantics { contentDescription = description }) {
        val t = position.coerceIn(0f, 1f)
        val h = 30.dp.toPx(); val w = 52.dp.toPx()
        val origin = Offset((size.width - w) / 2, (size.height - h) / 2)
        val alpha = if (enabled) 1f else .38f
        drawRoundRect(lerp(off, on, t).copy(alpha = alpha), origin, Size(w, h), CornerRadius(h / 2))
        val stretch = 1f + .12f * sin(t * PI).toFloat()
        val r = 12.dp.toPx()
        val center = origin + Offset(h / 2 + (w - h) * t, h / 2)
        drawOval(thumb.copy(alpha = alpha), center - Offset(r * stretch, r / stretch), Size(2 * r * stretch, 2 * r / stretch))
    }
}
