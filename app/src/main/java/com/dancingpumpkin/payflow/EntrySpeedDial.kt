package com.dancingpumpkin.payflow

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
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
    LaunchedEffect(expanded) { progress.animateTo(if (expanded) 1f else 0f, tween(if (expanded) 220 else 176, easing = LinearOutSlowInEasing)) }
    Box(Modifier.width(56.dp).height(192.dp), contentAlignment = Alignment.BottomCenter) {
        if (actionsVisible) listOf(DayEntryTab.EVENT, DayEntryTab.INCOME).forEachIndexed { index, destination ->
            val action = remember { Animatable(0f) }
            val interaction = remember { MutableInteractionSource() }
            LaunchedEffect(expanded) {
                if (expanded) delay((index * 30).toLong())
                action.animateTo(if (expanded) 1f else 0f, tween(if (expanded) 200 else 160, easing = LinearOutSlowInEasing))
            }
            Surface(
                modifier = Modifier.size(48.dp).offset {
                    val t = action.value
                    IntOffset((kotlin.math.sin(t * Math.PI) * 6.dp.toPx()).roundToInt(), (-((2 - index) * 64).dp.toPx() * t).roundToInt())
                }.then(pressFeedback(interaction)).graphicsLayer {
                    alpha = action.value
                    scaleX = 0.95f + 0.05f * action.value
                    scaleY = scaleX
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
                Icon(Icons.Default.Add, null, Modifier.fillMaxSize().graphicsLayer { alpha = 1f - progress.value; rotationZ = 45f * progress.value })
                Icon(Icons.Default.Close, null, Modifier.fillMaxSize().graphicsLayer { alpha = progress.value })
            }
        }
    }
}
