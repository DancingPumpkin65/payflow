package com.dancingpumpkin.payflow

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

@Composable
internal fun pressFeedback(interaction: MutableInteractionSource): Modifier {
    val pressed by interaction.collectIsPressedAsState()
    val scale = remember { Animatable(1f) }
    LaunchedEffect(pressed) {
        if (pressed) scale.snapTo(0.97f)
        else scale.animateTo(1f, tween(120, easing = LinearOutSlowInEasing))
    }
    return Modifier.graphicsLayer { scaleX = scale.value; scaleY = scale.value }
}
