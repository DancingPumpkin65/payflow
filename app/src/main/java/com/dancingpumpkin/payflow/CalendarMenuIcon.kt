package com.dancingpumpkin.payflow

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp

@Composable
internal fun CalendarMenuIcon() {
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Canvas(Modifier.size(28.dp).semantics { contentDescription = "Menu" }) {
        for (x in listOf(0.25f, 0.75f)) for (y in listOf(0.25f, 0.75f)) {
            drawCircle(color, 2.dp.toPx(), Offset(size.width * x, size.height * y))
        }
    }
}
