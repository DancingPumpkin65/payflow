package com.dancingpumpkin.payflow

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dancingpumpkin.payflow.domain.monthlyStats
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val RevenueBlue = Color(0xFF72A7FF)
private val ExpenseCoral = Color(0xFFFFB4AB)
private val RetainedTeal = Color(0xFF78D6BE)

@Composable
@OptIn(ExperimentalMaterial3Api::class)
internal fun DashboardScreen(state: PayflowState, back: () -> Unit) {
    val now = remember { YearMonth.now() }
    val pager = rememberPagerState(initialPage = (now.year - 1900) * 12 + now.monthValue - 1, pageCount = { 2400 })
    var shop by rememberSaveable { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp).heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
            Text("Dashboard", style = MaterialTheme.typography.titleLarge)
        }
        PrimaryTabRow(selectedTabIndex = if (shop) 1 else 0, containerColor = Color.Transparent) {
            Tab(selected = !shop, onClick = { shop = false }, selectedContentColor = RevenueBlue, unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant, text = { Text("Work pay") })
            Tab(selected = shop, onClick = { shop = true }, selectedContentColor = RevenueBlue, unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant, text = { Text("Shop revenue") })
        }
        HorizontalPager(pager, Modifier.weight(1f), verticalAlignment = Alignment.Top) { page ->
            val month = YearMonth.of(1900 + page / 12, page % 12 + 1)
            val stats = remember(month, state.workDays, state.rates, state.expenses, state.shopIncome) {
                monthlyStats(month, state.workDays, state.rates, state.expenses, state.shopIncome)
            }
            val income = if (shop) stats.revenue else stats.workPay
            val net = income - stats.expenses
            val motion = remember { Animatable(0f) }
            LaunchedEffect(page == pager.settledPage, shop, income, stats.expenses) {
                motion.snapTo(0f)
                if (page == pager.settledPage) motion.animateTo(1f, tween(650, easing = LinearOutSlowInEasing))
            }
            Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp).padding(top = 20.dp, bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(month.format(DateTimeFormatter.ofPattern("MMMM yyyy")), style = MaterialTheme.typography.headlineSmall)
                Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF222326)) {
                    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(if (shop) "REVENUE" else "WORK PAY", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(dashboardMoney(income), style = MaterialTheme.typography.headlineMedium)
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                            val basis = maxOf(income, stats.expenses)
                            val spent = if (basis == 0L) 0f else stats.expenses.toFloat() / basis
                            Canvas(Modifier.size(116.dp).semantics { contentDescription = "Expenses ${dashboardMoney(stats.expenses)}; net ${dashboardMoney(net)}" }) {
                                val stroke = 14.dp.toPx()
                                val inset = stroke / 2
                                val arcSize = Size(size.width - stroke, size.height - stroke)
                                drawArc(Color(0xFF414246), -90f, 360f, false, Offset(inset, inset), arcSize, style = Stroke(stroke))
                                if (basis > 0) {
                                    if (spent < 1f) drawArc(RetainedTeal, -90f + 360f * spent * motion.value, 360f * (1f - spent) * motion.value, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Butt))
                                    if (spent > 0f) drawArc(ExpenseCoral, -90f, 360f * spent * motion.value, false, Offset(inset, inset), arcSize, style = Stroke(stroke, cap = StrokeCap.Butt))
                                }
                            }
                            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                StatLegend("Expenses", stats.expenses, ExpenseCoral)
                                StatLegend("Net", net, if (net < 0) ExpenseCoral else RetainedTeal)
                            }
                        }
                        if (income == 0L && stats.expenses == 0L) Text("No records this month", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("DAILY TOTALS", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val max = stats.days.maxOf { maxOf(if (shop) it.revenue else it.workPay, it.expenses) }
                        Text(dashboardMoney(max), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    val max = stats.days.maxOf { maxOf(if (shop) it.revenue else it.workPay, it.expenses) }.coerceAtLeast(1)
                    Canvas(Modifier.fillMaxWidth().height(144.dp).semantics { contentDescription = "Daily ${if (shop) "revenue" else "work pay"} in blue and expenses in coral, ${month.lengthOfMonth()} days. Total ${dashboardMoney(income)}, expenses ${dashboardMoney(stats.expenses)}." }) {
                        val cell = size.width / stats.days.size
                        drawLine(Color(0xFF414246), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
                        stats.days.forEachIndexed { index, day ->
                            listOf((if (shop) day.revenue else day.workPay) to RevenueBlue, day.expenses to ExpenseCoral).forEachIndexed { series, (amount, color) ->
                                val height = (amount.toDouble() / max * size.height * motion.value).toFloat()
                                if (height > 0) drawRoundRect(color, Offset(index * cell + cell * (.08f + series * .44f), size.height - height), Size(cell * .34f, height), androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx()))
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        listOf(1, 8, 15, 22, month.lengthOfMonth()).forEach { Text(it.toString(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        ChartKey(if (shop) "Revenue" else "Work pay", RevenueBlue)
                        ChartKey("Expenses", ExpenseCoral)
                    }
                }
            }
        }
    }
}

@Composable private fun StatLegend(label: String, cents: Long, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        ChartKey(label, color)
        Text(dashboardMoney(cents), style = MaterialTheme.typography.titleMedium)
    }
}

@Composable private fun ChartKey(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Box(Modifier.size(7.dp).background(color, RoundedCornerShape(4.dp)))
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun dashboardMoney(cents: Long) = DecimalFormat("#,##0.00").format(BigDecimal.valueOf(cents, 2)) + " DH"
