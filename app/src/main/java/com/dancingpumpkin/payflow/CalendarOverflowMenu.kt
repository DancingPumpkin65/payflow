package com.dancingpumpkin.payflow

import androidx.compose.animation.*
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties

@Composable
internal fun CalendarOverflowMenu(expanded: Boolean, dismiss: () -> Unit, today: () -> Unit, rates: () -> Unit, expenses: () -> Unit) {
    val visibility = remember { MutableTransitionState(false) }
    visibility.targetState = expanded
    val offset = with(LocalDensity.current) { 52.dp.roundToPx() }
    if (visibility.currentState || visibility.targetState) Popup(
        alignment = Alignment.TopEnd, offset = IntOffset(0, offset), onDismissRequest = dismiss,
        properties = PopupProperties(focusable = true),
    ) {
        AnimatedVisibility(visibility,
            enter = fadeIn(tween(220)) + scaleIn(tween(220, easing = LinearOutSlowInEasing), initialScale = 0.95f, transformOrigin = TransformOrigin(1f, 0f)),
            exit = fadeOut(tween(176)) + scaleOut(tween(176), targetScale = 0.95f, transformOrigin = TransformOrigin(1f, 0f)),
        ) {
            Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF222326), shadowElevation = 8.dp) {
                Column(Modifier.width(184.dp).padding(vertical = 8.dp)) {
                    listOf("Go to today" to today, "Pay rates" to rates, "Expenses" to expenses).forEachIndexed { index, (label, action) ->
                        if (index > 0) HorizontalDivider(Modifier.padding(horizontal = 20.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                        Box(Modifier.fillMaxWidth().heightIn(min = 56.dp).clickable { dismiss(); action() }.padding(horizontal = 20.dp, vertical = 16.dp), contentAlignment = Alignment.CenterStart) {
                            Text(label, style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
            }
        }
    }
}
