package com.dancingpumpkin.payflow

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.animation.core.LinearOutSlowInEasing
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
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
private val Accent = Color(0xFF72A7FF)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {
        setContent { PayflowApp() }
    }
}

private enum class Destination { CALENDAR, RATES, EXPENSES }

@Composable private fun PayflowApp(viewModel: PayflowViewModel = viewModel()) {
    val launchOpacity = remember { Animatable(0f) }
    val state by viewModel.state.collectAsState()
    LaunchedEffect(state.loaded) { if (state.loaded) launchOpacity.animateTo(1f, tween(220, easing = LinearOutSlowInEasing)) }
    var destination by rememberSaveable { mutableStateOf(Destination.CALENDAR) }
    val savedScreens = rememberSaveableStateHolder()
    val typography = remember {
        val base = Typography()
        Typography(
            titleLarge = base.titleLarge.copy(fontFeatureSettings = "tnum"),
            titleMedium = base.titleMedium.copy(fontFeatureSettings = "tnum"),
            titleSmall = base.titleSmall.copy(fontFeatureSettings = "tnum"),
            bodyLarge = base.bodyLarge.copy(fontFeatureSettings = "tnum"),
            bodyMedium = base.bodyMedium.copy(fontFeatureSettings = "tnum"),
            bodySmall = base.bodySmall.copy(fontFeatureSettings = "tnum"),
            labelLarge = base.labelLarge.copy(fontFeatureSettings = "tnum"),
            labelMedium = base.labelMedium.copy(fontFeatureSettings = "tnum"),
            labelSmall = base.labelSmall.copy(fontFeatureSettings = "tnum"),
        )
    }
    BackHandler(destination != Destination.CALENDAR) { destination = Destination.CALENDAR }
    MaterialTheme(colorScheme = darkColorScheme(
        background = Black, surface = Color(0xFF151515), onBackground = Ink, onSurface = Ink,
        surfaceContainer = Color(0xFF1C1C1C), surfaceContainerHigh = Color(0xFF242424), surfaceContainerHighest = Color(0xFF2B2B2B),
        primary = Accent, onPrimary = Color(0xFF001B3F), secondary = Accent, onSecondary = Color(0xFF001B3F),
        primaryContainer = Color(0xFF152B4D), onPrimaryContainer = Color(0xFFD6E3FF),
        secondaryContainer = Color(0xFF152B4D), onSecondaryContainer = Color(0xFFD6E3FF),
        surfaceContainerLow = Color(0xFF191919), surfaceContainerLowest = Black, surfaceTint = Color.Transparent,
        onSurfaceVariant = Color(0xFFC1BDB7), outline = Color(0xFF8A8680),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    ), typography = typography) {
        Surface(color = Black, modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
            if (!state.loaded) Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                Image(painterResource(R.drawable.ic_payflow), null, modifier = Modifier.size(108.dp))
                Text("Payflow", style = MaterialTheme.typography.titleLarge, color = Ink)
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(Modifier.size(24.dp), color = Accent, strokeWidth = 2.dp)
                Text("Opening your calendar", style = MaterialTheme.typography.bodySmall, color = Muted, modifier = Modifier.padding(top = 12.dp))
            } else Box(Modifier.fillMaxSize().graphicsLayer { alpha = launchOpacity.value }) {
            AnimatedContent(destination, modifier = Modifier.fillMaxSize(), transitionSpec = {
                val direction = if (targetState == Destination.CALENDAR) -1 else 1
                (fadeIn(tween(300, easing = LinearOutSlowInEasing)) + slideInHorizontally(tween(300, easing = FastOutSlowInEasing)) { direction * it / 12 }) togetherWith
                    (fadeOut(tween(240)) + slideOutHorizontally(tween(240, easing = FastOutSlowInEasing)) { -direction * it / 12 }) using SizeTransform(clip = true, sizeAnimationSpec = { _, _ -> tween(0) })
            }, label = "Destination") { screen ->
            savedScreens.SaveableStateProvider(screen.name) {
            when (screen) {
                Destination.CALENDAR -> CalendarScreen(state, viewModel, { destination = Destination.RATES }, { destination = Destination.EXPENSES })
                Destination.RATES -> RatesScreen(state.rates, { destination = Destination.CALENDAR }, viewModel::addRate, viewModel::updateRate, viewModel::deleteRate)
                Destination.EXPENSES -> ExpensesScreen(state.expenses, { destination = Destination.CALENDAR }, viewModel::addExpense, viewModel::deleteExpense, viewModel::updateExpense)
            }
            }
            }
            }
        }
    }
}


@Composable private fun RatesScreen(rates: List<PayRateEntity>, back: () -> Unit, add: (String, Long) -> Unit, update: (PayRateEntity) -> Unit, delete: (PayRateEntity) -> Unit) {
    var showDialog by rememberSaveable { mutableStateOf(false) }
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    rates.firstOrNull { it.id == editingId }?.let { rate ->
        MoneyDialog("Edit pay rate", false, null, { date, _, cents -> update(rate.copy(effectiveDate = date, dailyAmountCents = cents)); editingId = null }, { editingId = null }, initialRate = rate)
    }
    SimpleScreen("Pay rates", back, { showDialog = true }) {
        rates.asReversed().forEach { rate ->
            key(rate.id) {
                RecordCard("${money(rate.dailyAmountCents)} per day", "Effective from ${displayDate(rate.effectiveDate)}", kind = "pay rate", edit = { editingId = rate.id }, delete = { delete(rate) })
            }
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
            key(expense.id) {
                RecordCard(expense.title, displayDate(expense.date), amount = money(expense.amountCents), kind = "expense", edit = { editingId = expense.id }, delete = { delete(expense) })
            }
        }
        if (expenses.isEmpty()) Text("No expenses yet.", color = Muted)
    }
    if (showDialog) ExpenseDialog({ date, title, amount -> add(date, title, amount); showDialog = false }, { showDialog = false })
}

@Composable private fun SimpleScreen(title: String, back: () -> Unit, add: () -> Unit, content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize()) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp).heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back") }
            Text(title, color = Ink, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
        }
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(bottom = 96.dp)) { content() }
    }
    FloatingActionButton(onClick = add, modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp), shape = CircleShape, containerColor = Accent) {
        Icon(Icons.Default.Add, if (title == "Expenses") "Add expense" else "Add pay rate", modifier = Modifier.size(30.dp))
    }
    }
}

@Composable private fun RateDialog(save: (String, Long) -> Unit, dismiss: () -> Unit) {
    MoneyDialog("Set daily pay", false, null, { date, _, amount -> save(date, amount) }, dismiss)
}

@Composable private fun ExpenseDialog(save: (String, String, Long) -> Unit, dismiss: () -> Unit) {
    MoneyDialog("New expense", true, null, save, dismiss)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable private fun MoneyDialog(heading: String, expense: Boolean, initial: ExpenseEntity?, save: (String, String, Long) -> Unit, dismiss: () -> Unit, initialRate: PayRateEntity? = null) {
    var date by rememberSaveable { mutableStateOf(initial?.date ?: initialRate?.effectiveDate ?: LocalDate.now().toString()) }
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var amount by rememberSaveable { mutableStateOf((initial?.amountCents ?: initialRate?.dailyAmountCents)?.let { java.math.BigDecimal.valueOf(it, 2).toPlainString() } ?: "") }
    val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
    val cents = runCatching { amount.replace(',', '.').toBigDecimal().movePointRight(2).longValueExact() }.getOrNull()?.takeIf { it in 1..100_000_000_000L }
    var chooseDate by rememberSaveable { mutableStateOf(false) }
    FormEditor(heading, dismiss, action = { finish ->
        IconButton(enabled = parsedDate != null && cents != null && (!expense || title.isNotBlank()), onClick = { finish { save(parsedDate!!.toString(), title.trim(), cents!!) } }) { Icon(Icons.Default.Check, if (!expense) "Save rate" else if (initial == null) "Save expense" else "Save changes") }
    }) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            val colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer, errorContainerColor = MaterialTheme.colorScheme.surfaceContainer, focusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant, unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant)
            if (expense) TextField(title, { title = it }, label = { Text("Title") }, leadingIcon = { Icon(Icons.Default.Title, null) }, modifier = Modifier.fillMaxWidth(), singleLine = true, colors = colors)
            TextField(amount, { amount = it }, label = { Text(if (expense) "Amount in DH" else "DH per day") }, leadingIcon = { Icon(Icons.Default.Payments, null) }, modifier = Modifier.fillMaxWidth(), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), singleLine = true, isError = amount.isNotEmpty() && cents == null, colors = colors)
            if (amount.isNotEmpty() && cents == null) Text("Enter a positive amount with up to 2 decimals", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            TextButton(modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp), onClick = {
                chooseDate = true
            }) {
                Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (expense) "Date" else "Starting date", modifier = Modifier.padding(start = 16.dp).weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(displayDate(date), color = Ink)
                RoundedArrow(ArrowDirection.RIGHT, "Choose date")
            }
        }
        }
    }
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
