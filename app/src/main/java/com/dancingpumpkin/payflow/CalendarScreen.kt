package com.dancingpumpkin.payflow

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionLayout
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.relocation.BringIntoViewRequester
import androidx.compose.foundation.relocation.bringIntoViewRequester
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

@OptIn(ExperimentalSharedTransitionApi::class, ExperimentalFoundationApi::class)
@Composable
internal fun CalendarScreen(state: PayflowState, viewModel: PayflowViewModel, openRates: () -> Unit, openExpenses: () -> Unit) {
    val now = YearMonth.now()
    val base = 1900 * 12
    val pager = rememberPagerState(initialPage = now.year * 12 + now.monthValue - 1 - base, pageCount = { 2400 })
    var yearMode by rememberSaveable { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(pager.currentPage) }
    var year by rememberSaveable { mutableIntStateOf(now.year) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var showDay by rememberSaveable { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val yearColumns = if (LocalConfiguration.current.screenWidthDp < 400 || LocalDensity.current.fontScale > 1.15f) 2 else 3
    val selectedMonthPosition = remember { BringIntoViewRequester() }
    val month = YearMonth.of((base + pager.currentPage) / 12, (base + pager.currentPage) % 12 + 1)
    val totals = viewModel.totals()
    val worked = remember(state.workDays) { state.workDays.map { it.date }.toSet() }
    LaunchedEffect(pager.settledPage) {
        val visible = YearMonth.of((base + pager.settledPage) / 12, (base + pager.settledPage) % 12 + 1)
        if (YearMonth.from(LocalDate.parse(selectedDate)) != visible) selectedDate = visible.atDay(1).toString()
    }
    BackHandler(yearMode) { yearMode = false }
    if (showDay) DayDetailsSheet(LocalDate.parse(selectedDate), state, viewModel) { showDay = false }

    Column(Modifier.fillMaxSize().padding(horizontal = 16.dp)) {
        Row(Modifier.fillMaxWidth().heightIn(min = 64.dp), verticalAlignment = Alignment.CenterVertically) {
            Row(Modifier.weight(1f).clickable {
                selectedIndex = pager.currentPage
                year = month.year
                scope.launch { withFrameNanos { }; yearMode = !yearMode }
            }.heightIn(min = 48.dp).semantics { role = Role.Button; contentDescription = "Choose month and year" }, verticalAlignment = Alignment.CenterVertically) {
            Text(
                if (yearMode) year.toString() else month.format(DateTimeFormatter.ofPattern("MMM yyyy")),
                style = MaterialTheme.typography.titleLarge,
                color = CalendarInk,
                modifier = Modifier.padding(vertical = 12.dp),
            )
            Icon(if (yearMode) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = CalendarMuted, modifier = Modifier.padding(start = 4.dp).size(20.dp))
            }
            if (!yearMode) Column(horizontalAlignment = Alignment.End) {
                Text(formatDh(viewModel.earnedForMonth(month.toString())), color = CalendarAccent, style = MaterialTheme.typography.titleMedium)
                Text("Month earned", color = CalendarMuted, style = MaterialTheme.typography.labelSmall)
            }
            Box {
                IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, contentDescription = "Menu") }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Day details · ${LocalDate.parse(selectedDate).format(DateTimeFormatter.ofPattern("d MMM yyyy"))}") }, onClick = { showMenu = false; showDay = true })
                    DropdownMenuItem(text = { Text("Pay rates") }, onClick = { showMenu = false; openRates() })
                    DropdownMenuItem(text = { Text("Expenses") }, onClick = { showMenu = false; openExpenses() })
                    DropdownMenuItem(text = { Text("Today") }, onClick = { showMenu = false; scope.launch { pager.animateScrollToPage(now.year * 12 + now.monthValue - 1 - base); selectedDate = LocalDate.now().toString() }; yearMode = false })
                }
            }
        }
        if (!yearMode) Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("Swipe to change month", color = CalendarMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
            TextButton(onClick = { scope.launch { pager.animateScrollToPage(now.year * 12 + now.monthValue - 1 - base); selectedDate = LocalDate.now().toString() } }) { Text("Today") }
        }
        SharedTransitionLayout(Modifier.weight(1f).fillMaxWidth()) {
            AnimatedContent(yearMode, label = "Calendar hierarchy") { overview ->
                if (overview) {
                    LaunchedEffect(year, selectedIndex) {
                        withFrameNanos { }
                        if (year == (base + selectedIndex) / 12) selectedMonthPosition.bringIntoView()
                    }
                    Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(onClick = { if (year > 1900) year-- }) { Text("‹ ${year - 1}", color = CalendarMuted) }
                            TextButton(onClick = { if (year < 2099) year++ }) { Text("${year + 1} ›", color = CalendarMuted) }
                        }
                        repeat(12 / yearColumns) { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                repeat(yearColumns) { column ->
                                    val item = YearMonth.of(year, row * yearColumns + column + 1)
                                    val index = item.year * 12 + item.monthValue - 1 - base
                                    Column(Modifier.weight(1f).padding(vertical = 8.dp).then(if (index == selectedIndex) Modifier.bringIntoViewRequester(selectedMonthPosition).sharedBounds(
                                        rememberSharedContentState("month-$index"), this@AnimatedContent,
                                        boundsTransform = { _, _ -> tween(300) },
                                    ) else Modifier).clickable {
                                        selectedIndex = index
                                        scope.launch {
                                            // Let the selected thumbnail register its shared bounds before it exits.
                                            withFrameNanos { }
                                            pager.scrollToPage(index)
                                            withFrameNanos { }
                                            yearMode = false
                                        }
                                    }.semantics { contentDescription = "${item.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${item.year}"; role = Role.Button }.border(if (item == month) 1.dp else 0.dp, if (item == month) CalendarAccent else Color.Transparent, MaterialTheme.shapes.small).padding(6.dp)) {
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
                        Column(Modifier.fillMaxSize().then(if (page == selectedIndex) Modifier.sharedBounds(
                            rememberSharedContentState("month-$page"), this@AnimatedContent,
                            boundsTransform = { _, _ -> tween(300) },
                        ) else Modifier)) {
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
                                        val selected = date.toString() == selectedDate
                                        Box(Modifier.weight(1f).fillMaxHeight().combinedClickable(
                                            role = Role.Button,
                                            onClick = { selectedDate = date.toString() },
                                            onLongClickLabel = "Day events and shop income",
                                            onLongClick = { selectedDate = date.toString(); showDay = true },
                                        ).semantics {
                                            contentDescription = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
                                            this.selected = selected
                                            stateDescription = listOfNotNull(if (today) "Today" else null, if (isWorked) "Worked" else "Not worked").joinToString(", ")
                                        }, contentAlignment = Alignment.Center) {
                                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Box(Modifier.size(38.dp).clip(CircleShape).background(if (isWorked) CalendarAccent else if (selected) Color(0xFF30291E) else Color.Transparent).then(if (selected) Modifier.border(1.dp, CalendarAccent, CircleShape) else Modifier), contentAlignment = Alignment.Center) {
                                                Text(date.dayOfMonth.toString(), fontSize = 18.sp, fontWeight = if (today) FontWeight.Bold else FontWeight.Normal, color = when {
                                                    isWorked -> Color.Black
                                                    date.month != item.month -> Color(0xFF85817B)
                                                    today || selected -> CalendarAccent
                                                    column >= 5 -> CalendarMuted
                                                    else -> CalendarInk
                                                })
                                            }
                                            val hasDetails = state.events.any { it.date == date.toString() } || state.shopIncome.any { it.date == date.toString() }
                                            Box(Modifier.padding(top = 3.dp).size(4.dp).clip(CircleShape).background(if (hasDetails) CalendarAccent else Color.Transparent))
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
        if (!yearMode) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(Modifier.fillMaxWidth().clickable { showDay = true }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(LocalDate.parse(selectedDate).format(DateTimeFormatter.ofPattern("EEE, d MMM")), style = MaterialTheme.typography.titleSmall, color = CalendarInk)
                val eventCount = state.events.count { it.date == selectedDate }
                val shop = state.shopIncome.firstOrNull { it.date == selectedDate }
                val detail = listOfNotNull(if (eventCount == 0) "No events" else if (eventCount == 1) "1 event" else "$eventCount events", shop?.let { "Shop ${formatDh(it.amountCents)}" }).joinToString(" · ")
                Text(detail, style = MaterialTheme.typography.bodySmall, color = CalendarMuted)
            }
            Icon(Icons.Default.ChevronRight, "Open selected day", tint = CalendarMuted)
        }
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Worked", style = MaterialTheme.typography.bodyLarge, color = CalendarInk)
                val rate = state.rates.filter { it.effectiveDate <= selectedDate }.maxByOrNull { it.effectiveDate }
                Text(rate?.let { "${formatDh(it.dailyAmountCents)} per day" } ?: "Set your daily pay in Pay rates", style = MaterialTheme.typography.bodySmall, color = CalendarMuted)
            }
            Switch(checked = selectedDate in worked, onCheckedChange = { viewModel.toggleWorkDay(selectedDate) }, modifier = Modifier.semantics { contentDescription = "Worked on ${LocalDate.parse(selectedDate).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))}" })
        }
        Text("All time", style = MaterialTheme.typography.labelSmall, color = CalendarMuted, modifier = Modifier.padding(top = 12.dp))
        Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            listOf("Earned" to totals.earnedCents, "Spent" to totals.spentCents, "Net" to totals.netCents).forEach { (label, amount) ->
                Column(Modifier.weight(1f).padding(end = 8.dp)) { Text(label, color = CalendarMuted, style = MaterialTheme.typography.labelSmall); Text(formatDh(amount), color = if (label == "Net") CalendarAccent else CalendarInk, style = MaterialTheme.typography.bodyMedium) }
            }
        }
        Spacer(Modifier.height(16.dp))
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
                    Text(if (valid) day.toString() else "", fontSize = 10.sp, lineHeight = 13.sp, textAlign = TextAlign.Center, color = if (valid && month.atDay(day).toString() in worked) CalendarAccent else CalendarMuted, modifier = Modifier.weight(1f).heightIn(min = 14.dp))
                }
            }
        }
    }
}

private fun formatDh(cents: Long) = DecimalFormat("#,##0.00").format(BigDecimal.valueOf(cents, 2)) + " DH"
