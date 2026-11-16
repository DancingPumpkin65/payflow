package com.dancingpumpkin.payflow

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
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
    val weeks = remember(year) { activityWeeks(year) }
    val worked = remember(state.workDays) { state.workDays.map { it.date }.toSet() }
    val blue = MaterialTheme.colorScheme.primary
    val empty = MaterialTheme.colorScheme.surfaceContainerHighest
    val scroll = rememberLazyListState()
    var positionedYear by rememberSaveable { mutableIntStateOf(-1) }
    LaunchedEffect(year) {
        if (positionedYear != year) {
            scroll.scrollToItem(if (year == LocalDate.now().year) (LocalDate.now().dayOfYear / 7 - 2).coerceAtLeast(0) else 0)
            positionedYear = year
        }
    }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Yearly activity", back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                IconButton(enabled = year > 1900, onClick = { year-- }, modifier = Modifier.semantics { contentDescription = "Previous year" }) { Box(Modifier.graphicsLayer { rotationZ = 180f }) { RoundedArrow(ArrowDirection.RIGHT) } }
                Text(year.toString(), Modifier.weight(1f), style = MaterialTheme.typography.headlineSmall)
                IconButton(enabled = year < 2099, onClick = { year++ }, modifier = Modifier.semantics { contentDescription = "Next year" }) { RoundedArrow(ArrowDirection.RIGHT) }
            }
            Text("${worked.count { it.startsWith("$year-") }} worked days", style = MaterialTheme.typography.titleMedium)
            Canvas(Modifier.fillMaxWidth().height(72.dp).semantics { contentDescription = "Full year overview, $year" }) {
                val w = size.width / weeks.size; val h = size.height / 7
                weeks.forEachIndexed { x, week -> week.forEachIndexed { y, date ->
                    if (date != null) drawRoundRect(if (date.toString() in worked) blue else empty, Offset(x * w + 1, y * h + 1), Size((w - 2).coerceAtLeast(1f), h - 2), CornerRadius(1f))
                } }
            }
            Text("Tap a date below to open its calendar details", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.width(28.dp)) {
                    Spacer(Modifier.height(24.dp))
                    listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                        Box(Modifier.height(48.dp), contentAlignment = Alignment.Center) { Text(day, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
                LazyRow(state = scroll, modifier = Modifier.weight(1f)) {
                itemsIndexed(weeks, key = { index, _ -> "$year-$index" }) { _, week ->
                    Column {
                        Text(week.firstOrNull { it?.dayOfMonth == 1 }?.format(DateTimeFormatter.ofPattern("MMM")) ?: "", Modifier.width(48.dp).height(24.dp), style = MaterialTheme.typography.labelSmall)
                        week.forEach { date ->
                            Box(Modifier.size(48.dp).then(if (date != null) Modifier.clickable(role = Role.Button) { open(date.toString()) }.semantics(mergeDescendants = true) {
                                    contentDescription = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
                                    stateDescription = if (date.toString() in worked) "Worked" else "Not worked"
                                } else Modifier).padding(4.dp)) {
                                if (date != null) Surface(shape = RoundedCornerShape(6.dp), color = if (date.toString() in worked) blue else empty, modifier = Modifier.fillMaxSize()) {
                                    Box(contentAlignment = Alignment.Center) { Text(date.dayOfMonth.toString(), fontSize = 12.sp, color = if (date.toString() in worked) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant) }
                                }
                            }
                        }
                    }
                }
            }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                Text("■ Worked", color = blue, style = MaterialTheme.typography.labelMedium)
                Text("□ Not worked", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}
