package com.dancingpumpkin.payflow

import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dancingpumpkin.payflow.domain.activityWeeks
import java.time.*
import java.time.format.DateTimeFormatter

@Composable
internal fun ActivityScreen(state: PayflowState, back: () -> Unit, open: (String) -> Unit) {
    var year by rememberSaveable { mutableIntStateOf(LocalDate.now().year) }
    var selected by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    val weeks = remember(year) { activityWeeks(year) }
    val worked = remember(state.workDays) { state.workDays.map { it.date }.toSet() }
    val blue = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.surfaceContainerHighest
    val ink = MaterialTheme.colorScheme.onSurfaceVariant
    val scroll = rememberScrollState()
    val density = LocalDensity.current
    var positionedYear by rememberSaveable { mutableIntStateOf(-1) }
    val cell = 18.dp
    val captionHeight = with(density) { 16.sp.toDp() + 8.dp }
    LaunchedEffect(year) {
        if (positionedYear != year) {
            selected = if (year == LocalDate.now().year) LocalDate.now().toString() else LocalDate.of(year, 1, 1).toString()
            scroll.scrollTo(if (year == LocalDate.now().year) with(density) { ((LocalDate.now().dayOfYear / 7 - 10).coerceAtLeast(0) * 18).dp.roundToPx() } else 0)
            positionedYear = year
        }
    }
    val paint = remember { android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Yearly activity", back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(enabled = year > 1900, onClick = { year-- }, modifier = Modifier.semantics { contentDescription = "Previous year" }) { Box(Modifier.graphicsLayer { rotationZ = 180f }) { RoundedArrow(ArrowDirection.RIGHT) } }
                Text(year.toString(), Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                IconButton(enabled = year < 2099, onClick = { year++ }, modifier = Modifier.semantics { contentDescription = "Next year" }) { RoundedArrow(ArrowDirection.RIGHT) }
            }
            Text("${worked.count { it.startsWith("$year-") }} worked days", style = MaterialTheme.typography.titleMedium)
            Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row {
                        Column(Modifier.width(28.dp).padding(top = captionHeight)) {
                            repeat(7) { day -> Box(Modifier.height(cell), contentAlignment = Alignment.CenterStart) {
                                if (day in listOf(0, 2, 4)) Text(listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")[day], fontSize = 10.sp, color = ink)
                            } }
                        }
                        Canvas(Modifier.weight(1f).horizontalScroll(scroll).width(cell * weeks.size).height(captionHeight + cell * 7).pointerInput(year, density) {
                            detectTapGestures { point ->
                                val column = (point.x / with(density) { cell.toPx() }).toInt()
                                val row = ((point.y - with(density) { captionHeight.toPx() }) / with(density) { cell.toPx() }).toInt()
                                if (point.y >= with(density) { captionHeight.toPx() }) weeks.getOrNull(column)?.getOrNull(row)?.let { selected = it.toString(); open(selected) }
                            }
                        }.semantics {
                            contentDescription = "Year activity heatmap, $year"
                            stateDescription = "$selected, ${if (selected in worked) "Worked" else "Not worked"}"
                            onClick("Open selected date") { open(selected); true }
                            customActions = listOf(
                                CustomAccessibilityAction("Previous date") { selected = LocalDate.parse(selected).minusDays(1).coerceAtLeast(LocalDate.of(year, 1, 1)).toString(); true },
                                CustomAccessibilityAction("Next date") { selected = LocalDate.parse(selected).plusDays(1).coerceAtMost(LocalDate.of(year, 12, 31)).toString(); true })
                        }) {
                            val unit = cell.toPx(); val top = captionHeight.toPx(); val gap = 2.dp.toPx()
                            paint.textSize = 11.sp.toPx(); paint.color = ink.toArgb()
                            weeks.forEachIndexed { x, week ->
                                week.firstOrNull { it?.dayOfMonth == 1 }?.let { date -> drawContext.canvas.nativeCanvas.drawText(date.format(DateTimeFormatter.ofPattern("MMM")), x * unit, 16.sp.toPx(), paint) }
                                week.forEachIndexed { y, date -> if (date != null) {
                                    val offset = Offset(x * unit + gap, top + y * unit + gap)
                                    val square = Size(unit - gap * 2, unit - gap * 2)
                                    drawRoundRect(if (date.toString() in worked) blue else empty, offset, square, CornerRadius(3.dp.toPx()))
                                    if (date.toString() == selected) drawRoundRect(blue, offset, square, CornerRadius(3.dp.toPx()), style = Stroke(1.dp.toPx()))
                                } }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text("■ Worked", color = blue, style = MaterialTheme.typography.labelSmall)
                        Text("■ Not worked", color = ink, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
            DateField("Open date", selected) { selected = it; open(it) }
        }
    }
}
