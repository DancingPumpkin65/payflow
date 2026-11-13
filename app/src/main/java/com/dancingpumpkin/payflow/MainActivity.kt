package com.dancingpumpkin.payflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.toggleable
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.saveable.SaveableStateHolder
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.dancingpumpkin.payflow.data.ExpenseEntity
import com.dancingpumpkin.payflow.data.PayRateEntity
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

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
    LaunchedEffect(Unit) { launchOpacity.animateTo(1f, tween(220)) }
    val state by viewModel.state.collectAsState()
    var destination by rememberSaveable { mutableStateOf(Destination.CALENDAR) }
    val savedScreens = rememberSaveableStateHolder()
    BackHandler(destination != Destination.CALENDAR) { destination = Destination.CALENDAR }
    MaterialTheme(colorScheme = darkColorScheme(
        background = Black, surface = Color(0xFF151515), onBackground = Ink, onSurface = Ink,
        surfaceContainer = Color(0xFF1C1C1C), surfaceContainerHigh = Color(0xFF242424), surfaceContainerHighest = Color(0xFF2B2B2B),
        primary = Accent, onPrimary = Black, secondary = Accent, onSecondary = Black,
        onSurfaceVariant = Color(0xFFC1BDB7), outline = Color(0xFF8A8680),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
    )) {
        Surface(color = Black, modifier = Modifier.fillMaxSize().graphicsLayer { alpha = launchOpacity.value }) {
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

@Composable private fun LegacyCalendarScreen(state: PayflowState, viewModel: PayflowViewModel, openRates: () -> Unit, openExpenses: () -> Unit) {
    var monthIndex by rememberSaveable { mutableStateOf(YearMonth.now().year * 12 + YearMonth.now().monthValue - 1) }
    val month = YearMonth.of(Math.floorDiv(monthIndex, 12), Math.floorMod(monthIndex, 12) + 1)
    var yearMode by rememberSaveable { mutableStateOf(false) }
    BackHandler(yearMode) { yearMode = false }
    val totals = viewModel.totals()
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Payflow", color = Accent, style = MaterialTheme.typography.labelMedium)
        }
        AnimatedContent(yearMode, transitionSpec = { (scaleIn(tween(220)) + fadeIn(tween(180))).togetherWith(scaleOut(tween(180)) + fadeOut(tween(140))) }, label = "calendar hierarchy") { showYear ->
            if (showYear) YearPicker(month.year, { selected -> monthIndex = month.year * 12 + selected - 1; yearMode = false }, { yearMode = false }) else MonthCalendar(
                month, state.workDays.map { it.date }.toSet(), viewModel.earnedForMonth(month.toString()), totals, { monthIndex-- }, { monthIndex++ }, { yearMode = true }, viewModel::toggleWorkDay, openRates
            )
        }
        Spacer(Modifier.weight(1f))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextButton(onClick = openRates) { Text("Rates", color = Muted) }
            TextButton(onClick = openExpenses) { Text("Expenses", color = Muted) }
        }
    }
}

@Composable private fun MonthCalendar(month: YearMonth, workedDates: Set<String>, monthIncome: Long, totals: com.dancingpumpkin.payflow.domain.Totals, previous: () -> Unit, next: () -> Unit, zoomOut: () -> Unit, toggle: (String) -> Unit, openRates: () -> Unit) {
    Column {
    val title = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + month.year
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = zoomOut, modifier = Modifier.weight(1f)) { Text(title, color = Ink, style = MaterialTheme.typography.titleMedium) }
        Column(horizontalAlignment = Alignment.End) { Text(money(monthIncome), color = Accent, style = MaterialTheme.typography.titleMedium); Text("this month", color = Muted, style = MaterialTheme.typography.labelSmall) }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = previous) { Text("‹ Previous", color = Muted) }
        TextButton(onClick = next) { Text("Next ›", color = Muted) }
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        MoneyColumn("Earned", totals.earnedCents, Ink); MoneyColumn("Spent", totals.spentCents, Ink); MoneyColumn("Net", totals.netCents, Accent)
    }
    Row(Modifier.fillMaxWidth()) { listOf("S", "M", "T", "W", "T", "F", "S").forEach { Text(it, color = Muted, textAlign = TextAlign.Center, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall) } }
    AnimatedContent(month, transitionSpec = {
        val direction = if (targetState > initialState) 1 else -1
        slideInHorizontally(tween(200)) { it * direction }.togetherWith(slideOutHorizontally(tween(200)) { -it * direction })
    }, label = "Month navigation") { displayed ->
        val gridStart = displayed.atDay(1).minusDays((displayed.atDay(1).dayOfWeek.value % 7).toLong())
        Column(Modifier.padding(top = 6.dp)) { repeat(6) { row -> Row(Modifier.fillMaxWidth()) { repeat(7) { column -> val date = gridStart.plusDays((row * 7 + column).toLong()); DayCell(date, date.month == displayed.month, date.dayOfWeek, date.toString() in workedDates, { toggle(date.toString()) }) } } } }
    }
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
        TextButton(onClick = openRates) { Text("Set daily rate", color = Accent) }
    }
    }
}

@Composable private fun MoneyColumn(label: String, cents: Long, color: Color) = Column { Text(label, color = Muted, style = MaterialTheme.typography.labelSmall); Text(money(cents), color = color, style = MaterialTheme.typography.bodyLarge) }

@Composable private fun RowScope.DayCell(date: LocalDate, current: Boolean, day: DayOfWeek, worked: Boolean, onClick: () -> Unit) {
    val today = date == LocalDate.now(); val weekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
    Box(Modifier.weight(1f).height(50.dp).semantics { contentDescription = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy")) }.toggleable(value = worked, role = Role.Checkbox, onValueChange = { onClick() }).padding(6.dp).clip(CircleShape).background(if (worked) Accent else Color.Transparent), contentAlignment = Alignment.Center) {
        Text(date.dayOfMonth.toString(), color = when { worked -> Black; !current -> Faint; weekend -> Muted; else -> Ink }, style = MaterialTheme.typography.bodyMedium, fontWeight = if (today) FontWeight.Bold else FontWeight.Normal)
        if (today) Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp).size(4.dp).clip(CircleShape).background(if (worked) Black else Accent))
    }
}

@Composable private fun YearPicker(year: Int, select: (Int) -> Unit, close: () -> Unit) {
    Column(Modifier.fillMaxWidth().padding(top = 28.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(year.toString(), color = Ink, style = MaterialTheme.typography.headlineMedium)
            TextButton(onClick = close) { Text("Close", color = Muted) }
        }
        repeat(4) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(3) { column ->
                    val month = row * 3 + column + 1
                    TextButton(onClick = { select(month) }, modifier = Modifier.weight(1f).height(70.dp)) {
                        Text(java.time.Month.of(month).getDisplayName(TextStyle.FULL, Locale.getDefault()), color = Ink, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable private fun RatesScreen(rates: List<PayRateEntity>, back: () -> Unit, add: (String, Long) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }; SimpleScreen("Pay rates", back, { showDialog = true }) { rates.forEach { Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(it.effectiveDate, color = Ink); Text(money(it.dailyAmountCents) + " / day", color = Accent) } }; if (rates.isEmpty()) Text("Add the first daily rate to calculate income.", color = Muted) }; if (showDialog) RateDialog({ date, amount -> add(date, amount); showDialog = false }, { showDialog = false })
}

@Composable private fun ExpensesScreen(expenses: List<ExpenseEntity>, back: () -> Unit, add: (String, String, Long) -> Unit, delete: (ExpenseEntity) -> Unit, update: (ExpenseEntity) -> Unit) {
    var editingId by rememberSaveable { mutableStateOf<Long?>(null) }
    val editing = expenses.firstOrNull { it.id == editingId }
    editing?.let { record -> MoneyDialog("Edit expense", true, record, { date, title, cents -> update(record.copy(date = date, title = title, amountCents = cents)); editingId = null }, { editingId = null }) }
    var showDialog by rememberSaveable { mutableStateOf(false) }
    SimpleScreen("Expenses", back, { showDialog = true }) {
        expenses.forEach { expense ->
            Column(Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(expense.title, color = Ink, modifier = Modifier.weight(1f))
                    Text(money(expense.amountCents), color = Accent)
                }
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Text(expense.date, color = Muted, modifier = Modifier.weight(1f))
                    TextButton(onClick = { editingId = expense.id }) { Text("Edit") }
                    TextButton(onClick = { delete(expense) }) { Text("Delete", color = Muted) }
                }
            }
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
    MoneyDialog("Daily rate", false, null, { date, _, amount -> save(date, amount) }, dismiss)
}

@Composable private fun ExpenseDialog(save: (String, String, Long) -> Unit, dismiss: () -> Unit) {
    MoneyDialog("Expense", true, null, save, dismiss)
}

@Composable private fun MoneyDialog(heading: String, expense: Boolean, initial: ExpenseEntity?, save: (String, String, Long) -> Unit, dismiss: () -> Unit) {
    var date by rememberSaveable { mutableStateOf(initial?.date ?: LocalDate.now().toString()) }
    var title by rememberSaveable { mutableStateOf(initial?.title ?: "") }
    var amount by rememberSaveable { mutableStateOf(initial?.let { java.math.BigDecimal.valueOf(it.amountCents, 2).toPlainString() } ?: "") }
    val parsedDate = runCatching { LocalDate.parse(date) }.getOrNull()
    val cents = runCatching { amount.replace(',', '.').toBigDecimal().movePointRight(2).longValueExact() }.getOrNull()?.takeIf { it in 1..100_000_000_000L }
    AlertDialog(onDismissRequest = dismiss, title = { Text(heading) }, text = {
        Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            if (expense) OutlinedTextField(title, { title = it }, label = { Text("Title") }, singleLine = true)
            OutlinedTextField(amount, { amount = it }, label = { Text(if (expense) "Amount in DH" else "DH per day") }, singleLine = true, isError = amount.isNotEmpty() && cents == null, supportingText = { if (amount.isNotEmpty() && cents == null) Text("Enter a positive amount with up to 2 decimals") })
            OutlinedTextField(date, { date = it }, label = { Text(if (expense) "Date" else "Starting date") }, singleLine = true, isError = parsedDate == null, supportingText = { Text("YYYY-MM-DD") })
        }
    }, confirmButton = { Button(enabled = parsedDate != null && cents != null && (!expense || title.isNotBlank()), onClick = { save(parsedDate!!.toString(), title.trim(), cents!!) }) { Text("Save") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } })
}

private fun money(cents: Long) = java.text.DecimalFormat("#,##0.##").format(java.math.BigDecimal.valueOf(cents, 2)) + " DH"
