package com.dancingpumpkin.payflow

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Event
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import kotlinx.coroutines.delay
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import kotlin.math.roundToInt

/** Both secondary actions travel from the same origin and return to it on dismissal. */
@Composable
internal fun EntrySpeedDial(expanded: Boolean, toggle: () -> Unit, open: (DayEntryTab) -> Unit) {
    val progress = remember { Animatable(0f) }
    val actionsVisible by remember { derivedStateOf { progress.value > 0f } }
    LaunchedEffect(expanded) { progress.animateTo(if (expanded) 1f else 0f, spring(dampingRatio = .72f, stiffness = if (expanded) 420f else 650f)) }
    Box(Modifier.width(56.dp).height(192.dp), contentAlignment = Alignment.BottomCenter) {
        val liquidColor = MaterialTheme.colorScheme.surfaceContainerHigh
        Canvas(Modifier.fillMaxSize()) {
            val t = progress.value.coerceIn(0f, 1f)
            val base = Offset(size.width / 2, size.height - 28.dp.toPx())
            val neck = kotlin.math.sin(t * Math.PI).toFloat().coerceAtLeast(0f)
            if (neck > .01f) {
                val lower = base + Offset(0f, -64.dp.toPx() * t)
                val upper = base + Offset(0f, -128.dp.toPx() * t)
                liquidBridge(base, lower, 26.dp.toPx(), 24.dp.toPx(), liquidColor.copy(alpha = neck), neck)
                liquidBridge(lower, upper, 24.dp.toPx(), 24.dp.toPx(), liquidColor.copy(alpha = neck), neck)
            }
        }
        if (actionsVisible) listOf(DayEntryTab.EVENT, DayEntryTab.INCOME).forEachIndexed { index, destination ->
            val action = remember { Animatable(0f) }
            val interaction = remember { MutableInteractionSource() }
            LaunchedEffect(expanded) {
                if (expanded) delay((index * 30).toLong())
                action.animateTo(if (expanded) 1f else 0f, spring(dampingRatio = .72f, stiffness = if (expanded) 420f else 650f))
            }
            Surface(
                modifier = Modifier.size(48.dp).offset {
                    val t = action.value
                    IntOffset((kotlin.math.sin(t * Math.PI) * 6.dp.toPx()).roundToInt(), (-((2 - index) * 64).dp.toPx() * t).roundToInt())
                }.then(pressFeedback(interaction)).graphicsLayer {
                    alpha = action.value.coerceIn(0f, 1f)
                    val stretch = (kotlin.math.abs(action.velocity) * .018f).coerceAtMost(.12f)
                    scaleX = (0.95f + 0.05f * action.value) / (1f + stretch)
                    scaleY = (0.95f + 0.05f * action.value) * (1f + stretch)
                },
                shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh, shadowElevation = 2.dp,
            ) {
                IconButton(enabled = expanded, interactionSource = interaction, onClick = { open(destination) }) {
                    Icon(if (destination == DayEntryTab.EVENT) Icons.Outlined.Event else Icons.Outlined.Storefront,
                        if (destination == DayEntryTab.EVENT) "Add event" else "Record shop income", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        val interaction = remember { MutableInteractionSource() }
        FloatingActionButton(onClick = toggle, interactionSource = interaction, modifier = pressFeedback(interaction), shape = CircleShape, containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) {
            Box(Modifier.size(30.dp).semantics { contentDescription = if (expanded) "Close add menu" else "Add day entry" }) {
                Icon(Icons.Default.Add, null, Modifier.fillMaxSize().graphicsLayer { alpha = (1f - progress.value).coerceIn(0f, 1f); rotationZ = 45f * progress.value })
                Icon(Icons.Default.Close, null, Modifier.fillMaxSize().graphicsLayer { alpha = progress.value.coerceIn(0f, 1f) })
            }
        }
    }
}
