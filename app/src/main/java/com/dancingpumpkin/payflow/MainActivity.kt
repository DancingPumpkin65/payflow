package com.dancingpumpkin.payflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dancingpumpkin.payflow.data.ExpenseEntity
import com.dancingpumpkin.payflow.data.PayRateEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val Black = Color(0xFF000000)
private val Ink = Color(0xFFF6F4F0)
private val Muted = Color(0xFF9C9994)
private val Faint = Color(0xFF85817B)
private val Accent = Color(0xFFC9A46A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {
        setContent { PayflowApp() }
    }
}

private enum class Destination { CALENDAR, RATES, EXPENSES }

@Composable private fun PayflowApp(viewModel: PayflowViewModel = viewModel()) {
    val launchOpacity = remember { Animatable(0f) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.loaded) { if (state.loaded) launchOpacity.animateTo(1f, tween(220)) }
    var destination by rememberSaveable { mutableStateOf(Destination.CALENDAR) }
    val savedScreens = rememberSaveableStateHolder()
    BackHandler(destination != Destination.CALENDAR) { destination = Destination.CALENDAR }
    MaterialTheme(colorScheme = darkColorScheme(
        background = Black, surface = Color(0xFF151515), onBackground = Ink, onSurface = Ink,
        surfaceContainer = Color(0xFF1C1C1C), surfaceContainerHigh = Color(0xFF242424), surfaceContainerHighest = Color(0xFF2B2B2B),
        primary = Accent, onPrimary = Black, secondary = Accent, onSecondary = Black,
        primaryContainer = Color(0xFF30291E), onPrimaryContainer = Color(0xFFEBD1A8),
        secondaryContainer = Color(0xFF30291E), onSecondaryContainer = Color(0xFFEBD1A8),
        surfaceContainerLow = Color(0xFF191919), surfaceContainerLowest = Black, surfaceTint = Color.Transparent,
        onSurfaceVariant = Color(0xFFC1BDB7), outline = Color(0xFF8A8680),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    )) {
        Surface(color = Black, modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            if (!state.loaded) Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.ic_payflow), null, modifier = Modifier.size(108.dp))
                Text("Payflow", style = MaterialTheme.typography.titleLarge, color = Ink)
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(Modifier.size(24.dp), color = Accent, strokeWidth = 2.dp)
                Text("Opening your calendar", style = MaterialTheme.typography.bodySmall, color = Muted, modifier = Modifier.padding(top = 12.dp))
            } else Box(Modifier.fillMaxSize().graphicsLayer { alpha = launchOpacity.value }) {
            savedScreens.SaveableStateProvider(destination.name) {
            when (destination) {
                Destination.CALENDAR -> CalendarScreen(state, viewModel, { destination = Destination.RATES }, { destination = Destination.EXPENSES })
                Destination.RATES -> RatesScreen(state.rates, { destination = Destination.CALENDAR }, viewModel::addRate)
                Destination.EXPENSES -> ExpensesScreen(state.expenses, { destination = Destination.CALENDAR }, viewModel::addExpense, viewModel::deleteExpense, viewModel::updateExpense)
            }
            }
            }
        }
    }
}


@Composable private fun RatesScreen(rates: List<PayRateEntity>, back: () -> Unit, add: (String, Long) -> Unit) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    SimpleScreen("Pay rates", back, { showDialog = true }) {
        rates.asReversed().forEach { rate ->
            Row(Modifier.fillMaxWidth().padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Effective from", style = MaterialTheme.typography.labelMedium, color = Muted)
                    Text(displayDate(rate.effectiveDate), style = MaterialTheme.typography.bodyLarge, color = Ink)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(money(rate.dailyAmountCents), style = MaterialTheme.typography.titleMedium, color = Accent)
                    Text("per day", style = MaterialTheme.typography.labelMedium, color = Muted)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        }
        if (rates.isEmpty()) Text("Add a daily rate to calculate your pay.", modifier = Modifier.padding(vertical = 24.dp), color = Muted)
    }
    if (showDialog) RateDialog({ date, amount -> add(date, amount); showDialog = false }, { showDialog = false })
}

@Composable private fun ExpensesScreen(expenses: List<ExpenseEntity>, back: () -> Unit, add: (String, String, Long) -> Unit, delete: (ExpenseEntity) -> Unit, update: (ExpenseEntity) -> Unit) {
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editing = expenses.firstOrNull { it.id == editingId }
    editing?.let { record -> MoneyDialog("Edit expense", true, record, { date, title, cents -> update(record.copy(date = date, title = title, amountCents = cents)); editingId = null }, { editingId = null }) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    SimpleScreen("Expenses", back, { showDialog = true }) {
        expenses.forEach { expense ->
            var actions by remember { mutableStateOf(false) }
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text(expense.title, color = Ink, style = MaterialTheme.typography.bodyLarge)
                        Text(displayDate(expense.date), color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                    Text(money(expense.amountCents), color = Accent, style = MaterialTheme.typography.titleMedium)
                    Box {
                        IconButton(onClick = { actions = true }) { Icon(Icons.Default.MoreVert, "Actions for ${expense.title}") }
                        DropdownMenu(expanded = actions, onDismissRequest = { actions = false }) {
                            DropdownMenuItem(text = { Text("Edit") }, onClick = { actions = false; editingId = expense.id })
                            DropdownMenuItem(text = { Text("Delete") }, onClick = { actions = false; delete(expense) })
                        }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
        }
        if (expenses.isEmpty()) Text("No expenses yet.", color = Muted)
    }
    if (showDialog) ExpenseDialog({ date, title, amount -> add(date, title, amount); showDialog = false }, { showDialog = false })
}

@Composable private fun SimpleScreen(title: String, back: () -> Unit, add: () -> Unit, content: @Composable () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text(title, color = Ink, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = add) { Text("Add") }
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState())) { content() }
    }
}

@Composable private fun RateDialog(save: (String, Long) -> Unit, dismiss: () -> Unit) {
    MoneyDialog("Set daily pay", false, null, { date, _, amount -> save(date, amount) }, dismiss)
}

@Composable private fun ExpenseDialog(save: (String, String, Long) -> Unit, dismiss: () -> Unit) {
    MoneyDialog("New expense", true, null, save, dismiss)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun MoneyDialog(heading: String, expense: Boolean, initial: ExpenseEntity?, save: (String, String, Long) -> Unit, dismiss: () -> Unit) {
    var date by rememberSaveable { mutableStateOf(initial?.date ?: LocalDate.now().toString()) }
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var amount by rememberSaveable { mutableStateOf(initial?.let { java.math.BigDecimal.valueOf(it.amountCents, 2).toPlainString() } ?: "") }
    val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
    val cents = runCatching { amount.replace(',', '.').toBigDecimal().movePointRight(2).longValueExact() }.getOrNull()?.takeIf { it in 1..100_000_000_000L }
    var chooseDate by rememberSaveable { mutableStateOf(false) }
    AlertDialog(onDismissRequest = dismiss, title = { Text(heading) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (expense) OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true)
            OutlinedTextField(amount, { amount = it }, label = { Text(if (expense) "Amount in DH" else "DH per day") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, isError = amount.isNotEmpty() && cents == null)
            if (amount.isNotEmpty() && cents == null) Text("Enter a positive amount with up to 2 decimals", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            OutlinedButton(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.small, onClick = {
                chooseDate = true
            }) {
                Column(Modifier.weight(1f).padding(vertical = 4.dp), horizontalAlignment = Alignment.Start) {
                    Text(if (expense) "Date" else "Starting date", color = Muted, style = MaterialTheme.typography.labelMedium)
                    Text(displayDate(date), color = Ink, style = MaterialTheme.typography.bodyLarge)
                }
                Icon(Icons.Default.CalendarToday, contentDescription = "Choose date", tint = Accent)
            }
        }
    }, confirmButton = { Button(enabled = parsedDate != null && cents != null && (!expense || title.isNotBlank()), onClick = { save(parsedDate!!.toString(), title.trim(), cents!!) }) { Text(if (!expense) "Save rate" else if (initial == null) "Add expense" else "Save changes") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
    if (chooseDate) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = (parsedDate ?: LocalDate.now()).atStartOfDay(java.time.ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { chooseDate = false }, confirmButton = {
            TextButton(onClick = {
                picker.selectedDateMillis?.let { date = java.time.Instant.ofEpochMilli(it).atOffset(java.time.ZoneOffset.UTC).toLocalDate().toString() }
                chooseDate = false
            }) { Text("Done") }
        }, dismissButton = { TextButton(onClick = { chooseDate = false }) { Text("Cancel") } }) {
            Text("Select date", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(20.dp))
            DatePicker(state = picker, title = null, headline = null, showModeToggle = false)
        }
    }
}

private fun money(cents: Long) = java.text.DecimalFormat("#,##0.00").format(java.math.BigDecimal.valueOf(cents, 2)) + " DH"
private fun displayDate(date: String) = LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy"))
