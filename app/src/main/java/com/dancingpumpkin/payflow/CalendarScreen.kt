package com.dancingpumpkin.payflow

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

private val CalendarAccent = Color(0xFFC9A46A)
private val CalendarInk = Color(0xFFF6F4F0)
private val CalendarMuted = Color(0xFFA5A19B)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
internal fun CalendarScreen(state: PayflowState, viewModel: PayflowViewModel, openRates: () -> Unit, openExpenses: () -> Unit) {
    val now = YearMonth.now()
    val base = 1900 * 12
    val pager = rememberPagerState(initialPage = now.year * 12 + now.monthValue - 1 - base, pageCount = { 2400 })
    var yearMode by rememberSaveable { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(pager.currentPage) }
    var year by rememberSaveable { mutableIntStateOf(now.year) }
    val scope = rememberCoroutineScope()
    val month = YearMonth.of((base + pager.currentPage) / 12, (base + pager.currentPage) % 12 + 1)
    val totals = viewModel.totals()
    val worked = remember(state.workDays) { state.workDays.map { it.date }.toSet() }
    BackHandler(yearMode) { yearMode = false }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (yearMode) year.toString() else month.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                style = MaterialTheme.typography.headlineSmall,
                color = CalendarInk,
                modifier = Modifier.weight(1f).clickable {
                    selectedIndex = pager.currentPage
                    year = month.year
                    yearMode = !yearMode
                }.padding(vertical = 12.dp).semantics { role = Role.Button; contentDescription = "Choose month and year" },
            )
            Column(horizontalAlignment = Alignment.End) {
                Text(formatDh(viewModel.earnedForMonth(month.toString())), color = CalendarAccent, style = MaterialTheme.typography.titleMedium)
                Text("Month earned", color = CalendarMuted, style = MaterialTheme.typography.labelSmall)
            }
        }
        SharedTransitionLayout(Modifier.weight(1f).fillMaxWidth()) {
            AnimatedContent(yearMode, label = "Calendar hierarchy") { overview ->
                if (overview) {
                    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { if (year > 1900) year-- }) { Text("‹ ${year - 1}", color = CalendarMuted) }
                            TextButton(onClick = { if (year < 2099) year++ }) { Text("${year + 1} ›", color = CalendarMuted) }
                        }
                        repeat(4) { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                repeat(3) { column ->
                                    val item = YearMonth.of(year, row * 3 + column + 1)
                                    val index = item.year * 12 + item.monthValue - 1 - base
                                    Column(Modifier.weight(1f).padding(vertical = 8.dp).sharedBounds(
                                        rememberSharedContentState("month-$index"), this@AnimatedContent,
                                        boundsTransform = { _, _ -> tween(300) },
                                    ).clickable {
                                        selectedIndex = index
                                        scope.launch { pager.scrollToPage(index); yearMode = false }
                                    }) {
                                        Text(item.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()), color = if (item == month) CalendarAccent else CalendarInk, style = MaterialTheme.typography.labelLarge)
                                        MiniMonth(item, worked)
                                    }
                                }
                            }
                        }
                    }
                } else {
                    HorizontalPager(pager, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 1) { page ->
                        val item = YearMonth.of((base + page) / 12, (base + page) % 12 + 1)
                        Column(Modifier.fillMaxSize().sharedBounds(
                            rememberSharedContentState("month-$page"), this@AnimatedContent,
                            boundsTransform = { _, _ -> tween(300) },
                        )) {
                            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach {
                                    Text(it, color = CalendarMuted, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
                                }
                            }
                            val start = item.atDay(1).minusDays((item.atDay(1).dayOfWeek.value - 1).toLong())
                            repeat(6) { row ->
                                Row(Modifier.fillMaxWidth().weight(1f)) {
                                    repeat(7) { column ->
                                        val date = start.plusDays((row * 7 + column).toLong())
                                        val isWorked = date.toString() in worked
                                        val today = date == LocalDate.now()
                                        Box(Modifier.weight(1f).fillMaxHeight().toggleable(isWorked, role = Role.Checkbox, onValueChange = { viewModel.toggleWorkDay(date.toString()) }).semantics {
                                            contentDescription = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
                                            stateDescription = if (isWorked) "Worked" else "Not worked"
                                        }, contentAlignment = Alignment.Center) {
                                            Box(Modifier.size(36.dp).clip(CircleShape).background(if (isWorked) CalendarAccent else Color.Transparent).then(if (today) Modifier.border(1.dp, CalendarAccent, CircleShape) else Modifier), contentAlignment = Alignment.Center) {
                                                Text(date.dayOfMonth.toString(), fontSize = 20.sp, fontWeight = if (today) FontWeight.Bold else FontWeight.Normal, color = when {
                                                    isWorked -> Color.Black
                                                    date.month != item.month -> Color(0xFF85817B)
                                                    today -> CalendarAccent
                                                    column >= 5 -> CalendarMuted
                                                    else -> CalendarInk
                                                })
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Earned" to totals.earnedCents, "Spent" to totals.spentCents, "Net" to totals.netCents).forEach { (label, amount) ->
                Column { Text(label, color = CalendarMuted, style = MaterialTheme.typography.labelSmall); Text(formatDh(amount), color = if (label == "Net") CalendarAccent else CalendarInk, style = MaterialTheme.typography.bodyLarge) }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = openRates) { Text("Rates", color = CalendarMuted) }
            TextButton(onClick = { scope.launch { pager.animateScrollToPage(now.year * 12 + now.monthValue - 1 - base) }; yearMode = false }) { Text("Today", color = CalendarAccent) }
            TextButton(onClick = openExpenses) { Text("Expenses", color = CalendarMuted) }
        }
    }
}

@Composable
private fun MiniMonth(month: YearMonth, worked: Set<String>) {
    val offset = month.atDay(1).dayOfWeek.value - 1
    Column(Modifier.padding(top = 8.dp)) {
        repeat(6) { row ->
            Row(Modifier.fillMaxWidth()) {
                repeat(7) { column ->
                    val day = row * 7 + column - offset + 1
                    val valid = day in 1..month.lengthOfMonth()
                    Text(if (valid) day.toString() else "", fontSize = 9.sp, textAlign = TextAlign.Center, color = if (valid && month.atDay(day).toString() in worked) CalendarAccent else CalendarMuted, modifier = Modifier.weight(1f).height(15.dp))
                }
            }
        }
    }
}

private fun formatDh(cents: Long) = DecimalFormat("#,##0.##").format(BigDecimal.valueOf(cents, 2)) + " DH"
