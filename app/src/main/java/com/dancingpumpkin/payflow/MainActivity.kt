package com.dancingpumpkin.payflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
private val Faint = Color(0xFF55524E)
private val Accent = Color(0xFFC9A46A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) = super.onCreate(savedInstanceState).also {
        setContent { PayflowApp() }
    }
}

private enum class Destination { CALENDAR, RATES, EXPENSES }

@Composable private fun PayflowApp(viewModel: PayflowViewModel = viewModel()) {
    val state by viewModel.state.collectAsState()
    var destination by remember { mutableStateOf(Destination.CALENDAR) }
    MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(background = Black, surface = Black, onBackground = Ink, onSurface = Ink, primary = Accent)) {
        Surface(color = Black, modifier = Modifier.fillMaxSize()) {
            when (destination) {
                Destination.CALENDAR -> CalendarScreen(state, viewModel, { destination = Destination.RATES }, { destination = Destination.EXPENSES })
                Destination.RATES -> RatesScreen(state.rates, { destination = Destination.CALENDAR }, viewModel::addRate)
                Destination.EXPENSES -> ExpensesScreen(state.expenses, { destination = Destination.CALENDAR }, viewModel::addExpense, viewModel::deleteExpense)
            }
        }
    }
}

@Composable private fun CalendarScreen(state: PayflowState, viewModel: PayflowViewModel, openRates: () -> Unit, openExpenses: () -> Unit) {
    var month by remember { mutableStateOf(YearMonth.now()) }
    var yearMode by remember { mutableStateOf(false) }
    val totals = viewModel.totals()
    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 18.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Payflow", color = Accent, style = MaterialTheme.typography.labelMedium)
            TextButton(onClick = openExpenses) { Text("Expenses", color = Ink) }
        }
        AnimatedContent(yearMode, transitionSpec = { (scaleIn(tween(220)) + fadeIn(tween(180))).togetherWith(scaleOut(tween(180)) + fadeOut(tween(140))) }, label = "calendar hierarchy") { showYear ->
            if (showYear) YearPicker(month.year, { selected -> month = YearMonth.of(month.year, selected); yearMode = false }, { yearMode = false }) else MonthCalendar(
                month, state.workDays.map { it.date }.toSet(), viewModel.earnedForMonth(month.toString()), totals, { month = month.minusMonths(1) }, { month = month.plusMonths(1) }, { yearMode = true }, viewModel::toggleWorkDay, openRates
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
    val title = month.month.getDisplayName(TextStyle.FULL, Locale.getDefault()) + " " + month.year
    Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = previous) { Text("‹", color = Ink, style = MaterialTheme.typography.headlineMedium) }
        TextButton(onClick = zoomOut, modifier = Modifier.weight(1f)) { Text(title, color = Ink, style = MaterialTheme.typography.titleLarge) }
        Column(horizontalAlignment = Alignment.End) { Text(money(monthIncome), color = Accent, style = MaterialTheme.typography.titleMedium); Text("this month", color = Muted, style = MaterialTheme.typography.labelSmall) }
        TextButton(onClick = next) { Text("›", color = Ink, style = MaterialTheme.typography.headlineMedium) }
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        MoneyColumn("Earned", totals.earnedCents, Ink); MoneyColumn("Spent", totals.spentCents, Ink); MoneyColumn("Net", totals.netCents, Accent)
    }
    Row(Modifier.fillMaxWidth()) { listOf("S", "M", "T", "W", "T", "F", "S").forEach { Text(it, color = Muted, textAlign = TextAlign.Center, modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelSmall) } }
    val first = month.atDay(1); val start = first.minusDays((first.dayOfWeek.value % 7).toLong())
    Column(Modifier.padding(top = 6.dp)) { repeat(6) { row -> Row(Modifier.fillMaxWidth()) { repeat(7) { column -> val date = start.plusDays((row * 7 + column).toLong()); DayCell(date, date.month == month.month, date.dayOfWeek, date.toString() in workedDates, { toggle(date.toString()) }) } } } }
    TextButton(onClick = openRates, modifier = Modifier.align(Alignment.End)) { Text("Set daily rate", color = Accent) }
}

@Composable private fun MoneyColumn(label: String, cents: Long, color: Color) = Column { Text(label, color = Muted, style = MaterialTheme.typography.labelSmall); Text(money(cents), color = color, style = MaterialTheme.typography.bodyLarge) }

@Composable private fun DayCell(date: LocalDate, current: Boolean, day: DayOfWeek, worked: Boolean, onClick: () -> Unit) {
    val today = date == LocalDate.now(); val weekend = day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY
    Box(Modifier.weight(1f).height(50.dp).padding(4.dp).clip(CircleShape).background(if (worked) Accent else Color.Transparent).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(date.dayOfMonth.toString(), color = when { worked -> Black; !current -> Faint; weekend -> Muted; else -> Ink }, style = MaterialTheme.typography.bodyMedium, fontWeight = if (today) FontWeight.Bold else FontWeight.Normal)
        if (today && !worked) Box(Modifier.align(Alignment.BottomCenter).padding(bottom = 3.dp).size(4.dp).clip(CircleShape).background(Accent))
    }
}

@Composable private fun YearPicker(year: Int, select: (Int) -> Unit, close: () -> Unit) { Column(Modifier.fillMaxWidth().padding(top = 28.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text(year.toString(), color = Ink, style = MaterialTheme.typography.headlineMedium); TextButton(onClick = close) { Text("Close", color = Muted) } }; repeat(4) { row -> Row(Modifier.fillMaxWidth()) { repeat(3) { col -> val number = row * 3 + col + 1; TextButton(onClick = { select(number) }, modifier = Modifier.weight(1f).height(70.dp)) { Text(java.time.Month.of(number).getDisplayName(TextStyle.FULL, Locale.getDefault()), color = Ink, textAlign = TextAlign.Center) } } } } }

@Composable private fun RatesScreen(rates: List<PayRateEntity>, back: () -> Unit, add: (String, Long) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }; SimpleScreen("Pay rates", back, { showDialog = true }) { rates.forEach { Row(Modifier.fillMaxWidth().padding(vertical = 14.dp), horizontalArrangement = Arrangement.SpaceBetween) { Text(it.effectiveDate, color = Ink); Text(money(it.dailyAmountCents) + " / day", color = Accent) } }; if (rates.isEmpty()) Text("Add the first daily rate to calculate income.", color = Muted) }; if (showDialog) RateDialog({ date, amount -> add(date, amount); showDialog = false }, { showDialog = false })
}

@Composable private fun ExpensesScreen(expenses: List<ExpenseEntity>, back: () -> Unit, add: (String, String, Long) -> Unit, delete: (ExpenseEntity) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }; SimpleScreen("Expenses", back, { showDialog = true }) { expenses.forEach { expense -> Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Column { Text(expense.title, color = Ink); Text(expense.date, color = Muted, style = MaterialTheme.typography.labelSmall) }; Row(verticalAlignment = Alignment.CenterVertically) { Text(money(expense.amountCents), color = Accent); TextButton(onClick = { delete(expense) }) { Text("Delete", color = Muted) } } } }; if (expenses.isEmpty()) Text("No expenses yet.", color = Muted) }; if (showDialog) ExpenseDialog({ date, title, amount -> add(date, title, amount); showDialog = false }, { showDialog = false })
}

@Composable private fun SimpleScreen(title: String, back: () -> Unit, add: () -> Unit, content: @Composable () -> Unit) { Column(Modifier.fillMaxSize().padding(20.dp)) { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { TextButton(onClick = back) { Text("‹ Calendar", color = Muted) }; TextButton(onClick = add) { Text("Add", color = Accent) } }; Text(title, color = Ink, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(vertical = 20.dp)); content() } }

@Composable private fun RateDialog(save: (String, Long) -> Unit, dismiss: () -> Unit) { var date by remember { mutableStateOf(LocalDate.now().toString()) }; var amount by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = dismiss, title = { Text("Daily rate") }, text = { Column { OutlinedTextField(date, { date = it }, label = { Text("Starting date") }); OutlinedTextField(amount, { amount = it }, label = { Text("DH per day") }) } }, confirmButton = { Button(onClick = { amount.toLongOrNull()?.let { save(date, it * 100) } }) { Text("Save") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } }) }

@Composable private fun ExpenseDialog(save: (String, String, Long) -> Unit, dismiss: () -> Unit) { var date by remember { mutableStateOf(LocalDate.now().toString()) }; var title by remember { mutableStateOf("") }; var amount by remember { mutableStateOf("") }; AlertDialog(onDismissRequest = dismiss, title = { Text("Expense") }, text = { Column { OutlinedTextField(title, { title = it }, label = { Text("What was it?") }); OutlinedTextField(amount, { amount = it }, label = { Text("DH") }); OutlinedTextField(date, { date = it }, label = { Text("Date") }) } }, confirmButton = { Button(onClick = { amount.toLongOrNull()?.let { save(date, title, it * 100) } }) { Text("Save") } }, dismissButton = { TextButton(onClick = dismiss) { Text("Cancel") } }) }

private fun money(cents: Long) = String.format(Locale.US, "%,d DH", cents / 100)
