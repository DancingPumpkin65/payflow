package com.dancingpumpkin.payflow

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import com.dancingpumpkin.payflow.domain.*
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.*
import java.time.format.DateTimeFormatter

internal fun formatMoney(cents: Long) = DecimalFormat("#,##0.00").format(BigDecimal.valueOf(cents, 2)) + " DH"
internal fun PayflowState.analyze(start: LocalDate, end: LocalDate, ledger: String) = periodAnalytics(start, end, ledger, workDays, rates, expenses, shopIncome, transfers)

@Composable
internal fun ScreenHeader(title: String, back: () -> Unit, actions: @Composable RowScope.() -> Unit = {}) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp).heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
        Text(title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        actions()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DateField(label: String, date: String, change: (String) -> Unit) {
    var picker by remember { mutableStateOf(false) }
    TextButton(onClick = { picker = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
        Icon(Icons.Outlined.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(label, Modifier.padding(start = 16.dp).weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy")), color = MaterialTheme.colorScheme.onSurface)
        RoundedArrow(ArrowDirection.RIGHT)
    }
    if (picker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { picker = false }, confirmButton = { TextButton(onClick = {
            state.selectedDateMillis?.let { change(Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate().toString()) }; picker = false
        }) { Text("Done") } }, dismissButton = { TextButton(onClick = { picker = false }) { Text("Cancel") } }) { DatePicker(state, title = null, headline = null, showModeToggle = false) }
    }
}

@Composable
internal fun AnalyticsScreen(state: PayflowState, back: () -> Unit) {
    var period by rememberSaveable { mutableStateOf("7 days") }
    var ledger by rememberSaveable { mutableStateOf("Personal") }
    var start by rememberSaveable { mutableStateOf(LocalDate.now().minusDays(6).toString()) }
    var end by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    Column(Modifier.fillMaxSize()) {
        ScreenHeader("Date range", back)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
            ChoiceField("Ledger", ledger, listOf("Personal", "Shop"), { ledger = it })
            ChoiceField("Period", period, listOf("7 days", "15 days", "90 days", "Custom"), { chosen ->
                period = chosen
                if (chosen != "Custom") { end = LocalDate.now().toString(); start = LocalDate.now().minusDays(chosen.substringBefore(' ').toLong() - 1).toString() }
            })
            if (period == "Custom") {
                DateField("Start", start) { start = it }; DateField("End", end) { end = it }
            } else Text("${LocalDate.parse(start).format(DateTimeFormatter.ofPattern("d MMM yyyy"))} – ${LocalDate.parse(end).format(DateTimeFormatter.ofPattern("d MMM yyyy"))}", style = MaterialTheme.typography.bodyMedium)
            if (start > end) Text("End date must be on or after start date", color = MaterialTheme.colorScheme.error)
            else {
                val data = remember(state, start, end, ledger) { state.analyze(LocalDate.parse(start), LocalDate.parse(end), if (ledger == "Shop") "shop" else "personal") }
                Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        AmountRow(if (ledger == "Shop") "Revenue" else "Work pay", data.income)
                        AmountRow("Expenses", data.expenses)
                        HorizontalDivider()
                        AmountRow("Net", data.net)
                        AmountRow("Recorded savings", data.transfers)
                    }
                }
                CategoryBreakdown(data.categoryTotals)
            }
        }
    }
}

@Composable internal fun AmountRow(label: String, amount: Long) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(formatMoney(amount), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable
internal fun CategoryBreakdown(categories: Map<String, Long>) {
    val total = categories.values.sum()
    val fill = remember { Animatable(0f) }
    LaunchedEffect(categories) { fill.snapTo(0f); fill.animateTo(1f, tween(650, easing = LinearOutSlowInEasing)) }
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("SPENDING CATEGORIES", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (total == 0L) Text("No expenses in this period", color = MaterialTheme.colorScheme.onSurfaceVariant)
            else {
                Canvas(Modifier.size(120.dp).align(Alignment.CenterHorizontally).semantics { contentDescription = "Expense categories, total ${formatMoney(total)}" }) {
                    val stroke = 14.dp.toPx(); val inset = stroke / 2; var angle = -90f
                    drawArc(Color(0xFF414246), -90f, 360f, false, Offset(inset, inset), Size(size.width - stroke, size.height - stroke), style = Stroke(stroke))
                    categories.forEach { (name, amount) ->
                        val sweep = (amount.toDouble() / total * 360 * fill.value).toFloat()
                        drawArc(spendingColor(name), angle, sweep, false, Offset(inset, inset), Size(size.width - stroke, size.height - stroke), style = Stroke(stroke)); angle += sweep
                    }
                }
                categories.forEach { (name, amount) ->
                    val percent = DecimalFormat("0.#").format(amount.toDouble() * 100 / total)
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.padding(top = 8.dp).size(7.dp).background(spendingColor(name), RoundedCornerShape(4.dp)))
                            Text(name, Modifier.weight(1f)); Text("$percent%", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatMoney(amount), style = MaterialTheme.typography.bodyMedium)
                        LinearProgressIndicator(progress = { amount.toFloat() / total * fill.value }, modifier = Modifier.fillMaxWidth(), color = spendingColor(name))
                    }
                }
            }
        }
    }
}

private fun spendingColor(name: String) = when (name) {
    "Transport" -> Color(0xFF72A7FF)
    "Food" -> Color(0xFF78D6BE)
    "Shopping" -> Color(0xFFFFB4AB)
    "Bills" -> Color(0xFFEFC970)
    "Uncategorized" -> Color(0xFFA5A19B)
    else -> Color.hsl(((name.hashCode().toLong() and 0x7fffffff) % 360).toFloat(), .55f, .7f)
}
