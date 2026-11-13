package com.dancingpumpkin.payflow

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.gestures.scrollBy
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

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex

private val CalendarAccent = Color(0xFFC9A46A)
private val CalendarInk = Color(0xFFF6F4F0)
private val CalendarMuted = Color(0xFFA5A19B)

private fun monthIndex(month: YearMonth) = (month.year - 1900) * 12 + month.monthValue - 1
private fun monthAt(index: Int) = YearMonth.of(1900 + index / 12, index % 12 + 1)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CalendarScreen(state: PayflowState, viewModel: PayflowViewModel, openRates: () -> Unit, openExpenses: () -> Unit) {
    val now = YearMonth.now()
    val pager = rememberPagerState(initialPage = monthIndex(now), pageCount = { 2400 })
    var yearMode by rememberSaveable { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(pager.currentPage) }
    var year by rememberSaveable { mutableIntStateOf(now.year) }
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var showDay by rememberSaveable { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val columns = if (LocalConfiguration.current.screenWidthDp < 400 || density.fontScale > 1.15f) 2 else 3
    val month = monthAt(pager.currentPage)
    val worked = remember(state.workDays) { state.workDays.map { it.date }.toSet() }
    val progress = remember { Animatable(if (yearMode) 0f else 1f) }
    val thumbnails = remember { mutableMapOf<Int, Rect>() }
    var gridBounds by remember { mutableStateOf(Rect.Zero) }
    var yearBounds by remember { mutableStateOf(Rect.Zero) }
    val yearScroll = rememberScrollState()
    val monthEnabled = !yearMode && !progress.isRunning
    val yearEnabled = yearMode && !progress.isRunning
    LaunchedEffect(yearMode) { progress.animateTo(if (yearMode) 0f else 1f, tween(320)) }
    LaunchedEffect(pager.settledPage) {
        val visible = monthAt(pager.settledPage)
        if (YearMonth.from(LocalDate.parse(selectedDate)) != visible) selectedDate = visible.atDay(1).toString()
    }
    BackHandler(yearMode) { yearMode = false }
    if (showDay) DayDetailsSheet(LocalDate.parse(selectedDate), state, viewModel) { showDay = false }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val pageHeight = maxOf(maxHeight, if (density.fontScale > 1.2f) 760.dp else 680.dp)
        val yearHeight = (maxHeight - 64.dp).coerceAtLeast(120.dp)
        fun chooseYear() {
            if (progress.isRunning) return
            if (yearMode) { yearMode = false; return }
            selectedIndex = pager.currentPage
            year = month.year
            scope.launch {
                withFrameNanos { }
                val thumb = thumbnails[selectedIndex]
                val margin = with(density) { 8.dp.toPx() }
                if (thumb != null) {
                    val delta = when {
                        thumb.bottom > yearBounds.bottom - margin -> thumb.bottom - yearBounds.bottom + margin
                        thumb.top < yearBounds.top + margin -> thumb.top - yearBounds.top - margin
                        else -> 0f
                    }
                    yearScroll.scrollBy(delta)
                }
                withFrameNanos { }
                yearMode = true
            }
        }
        fun today() {
            scope.launch {
                selectedIndex = monthIndex(now)
                pager.requestScrollToPage(selectedIndex)
                selectedDate = LocalDate.now().toString()
                yearMode = false
            }
        }
        // Short windows scroll the page instead of compressing date touch targets.
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            Column(Modifier.fillMaxWidth().height(pageHeight).padding(horizontal = 12.dp)) {
                Row(Modifier.fillMaxWidth().height(64.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f).clickable(onClick = ::chooseYear).heightIn(min = 48.dp).semantics {
                        role = Role.Button
                        contentDescription = "Choose month and year"
                    }, verticalAlignment = Alignment.CenterVertically) {
                        Text(if (yearMode) year.toString() else month.format(DateTimeFormatter.ofPattern("MMM yyyy")), color = CalendarInk, style = MaterialTheme.typography.titleLarge)
                        Icon(if (yearMode) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, null, tint = CalendarMuted, modifier = Modifier.padding(start = 4.dp).size(20.dp))
                    }
                    Column(Modifier.widthIn(max = 168.dp).graphicsLayer { alpha = progress.value }.then(if (yearMode) Modifier.clearAndSetSemantics { } else Modifier), horizontalAlignment = Alignment.End) {
                        Text(formatDh(viewModel.earnedForMonth(month.toString())), color = CalendarAccent, style = MaterialTheme.typography.titleMedium)
                        Text("Month earned", color = CalendarMuted, style = MaterialTheme.typography.labelSmall)
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) { Icon(Icons.Default.MoreVert, "Menu") }
                        DropdownMenu(showMenu, { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Pay rates") }, onClick = { showMenu = false; openRates() })
                            DropdownMenuItem(text = { Text("Expenses") }, onClick = { showMenu = false; openExpenses() })
                            if (yearMode) DropdownMenuItem(text = { Text("Today") }, onClick = { showMenu = false; today() })
                        }
                    }
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    // Keep both endpoints measured. Only the month layer moves, so
                    // neither lazy composition nor parent reflow stalls the first frame.
                    Column(Modifier.fillMaxWidth().height(yearHeight).onGloballyPositioned { yearBounds = Rect(it.positionInRoot(), it.size.toSize()) }.zIndex(if (yearEnabled) 2f else 0f).graphicsLayer { alpha = 1f - progress.value }
                        .then(if (!yearMode) Modifier.clearAndSetSemantics { } else Modifier).verticalScroll(yearScroll, enabled = yearEnabled)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            TextButton(enabled = yearEnabled && year > 1900, onClick = { year-- }) { Text("‹ ${year - 1}", color = CalendarMuted) }
                            TextButton(enabled = yearEnabled && year < 2099, onClick = { year++ }) { Text("${year + 1} ›", color = CalendarMuted) }
                        }
                        repeat(12 / columns) { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                repeat(columns) { column ->
                                    val item = YearMonth.of(year, row * columns + column + 1)
                                    val index = monthIndex(item)
                                    Column(Modifier.weight(1f).onGloballyPositioned { thumbnails[index] = Rect(it.positionInRoot(), it.size.toSize()) }
                                        .padding(vertical = 8.dp).clickable(enabled = yearEnabled) {
                                            selectedIndex = index
                                            scope.launch {
                                                pager.requestScrollToPage(index)
                                                withFrameNanos { }
                                                withFrameNanos { }
                                                yearMode = false
                                            }
                                        }.semantics {
                                            contentDescription = "${item.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${item.year}"
                                            role = Role.Button
                                            selected = item == month
                                            val count = worked.count { it.startsWith(item.toString()) }
                                            stateDescription = if (count == 1) "1 worked day" else "$count worked days"
                                        }.border(if (item == month) 1.dp else 0.dp, if (item == month) CalendarAccent else Color.Transparent, MaterialTheme.shapes.small).padding(6.dp)) {
                                        Text(item.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()), color = if (item == month) CalendarAccent else CalendarInk, style = MaterialTheme.typography.labelLarge)
                                        MiniMonth(item, worked)
                                    }
                                }
                            }
                        }
                    }
                    Column(Modifier.fillMaxSize().zIndex(1f).then(if (yearMode) Modifier.clearAndSetSemantics { } else Modifier)) {
                        val chrome = Modifier.graphicsLayer { alpha = ((progress.value - 0.8f) / 0.2f).coerceIn(0f, 1f) }
                        Row(chrome.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text("Swipe to change month", color = CalendarMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
                            TextButton(enabled = monthEnabled, onClick = ::today) { Text("Today") }
                        }
                        Box(Modifier.weight(1f).fillMaxWidth().onGloballyPositioned { gridBounds = Rect(it.positionInRoot(), it.size.toSize()) }) {
                            Box(Modifier.fillMaxSize().graphicsLayer {
                                val t = progress.value
                                val thumb = thumbnails[selectedIndex] ?: gridBounds
                                transformOrigin = TransformOrigin(0f, 0f)
                                if (gridBounds.width > 0 && gridBounds.height > 0) {
                                    scaleX = thumb.width / gridBounds.width + (1f - thumb.width / gridBounds.width) * t
                                    scaleY = thumb.height / gridBounds.height + (1f - thumb.height / gridBounds.height) * t
                                    translationX = (thumb.left - gridBounds.left) * (1f - t)
                                    translationY = (thumb.top - gridBounds.top) * (1f - t)
                                }
                                alpha = if (t == 0f) 0f else 1f
                            }.background(Color.Black)) {
                                HorizontalPager(pager, modifier = Modifier.fillMaxSize(), userScrollEnabled = monthEnabled) { page ->
                                    MonthGrid(monthAt(page), worked, state, selectedDate, monthEnabled, { date ->
                                        selectedDate = date.toString()
                                        if (YearMonth.from(date) != monthAt(page)) scope.launch { pager.animateScrollToPage(monthIndex(YearMonth.from(date))) }
                                    }, { date -> selectedDate = date.toString(); showDay = true })
                                }
                            }
                        }
                        Column(chrome) { DaySummary(state, viewModel, selectedDate, worked, monthEnabled, openRates) { showDay = true } }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MonthGrid(item: YearMonth, worked: Set<String>, state: PayflowState, selectedDate: String, enabled: Boolean, select: (LocalDate) -> Unit, details: (LocalDate) -> Unit) {
    Column(Modifier.fillMaxSize()) {
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
                        enabled = enabled, role = Role.Button,
                        onClick = { select(date) },
                        onLongClickLabel = "Day events and shop income",
                        onLongClick = { details(date) },
                    ).semantics {
                        contentDescription = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
                        this.selected = selected
                        stateDescription = listOfNotNull(if (today) "Today" else null, if (isWorked) "Worked" else "Not worked").joinToString(", ")
                    }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(42.dp).then(if (selected) Modifier.border(1.dp, CalendarAccent, CircleShape) else Modifier).padding(3.dp).clip(CircleShape).background(if (isWorked) CalendarAccent else if (selected) Color(0xFF30291E) else Color.Transparent), contentAlignment = Alignment.Center) {
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

@Composable
private fun DaySummary(state: PayflowState, viewModel: PayflowViewModel, selectedDate: String, worked: Set<String>, enabled: Boolean, openRates: () -> Unit, openDay: () -> Unit) {
    val totals = viewModel.totals()
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(Modifier.fillMaxWidth().clickable(enabled = enabled) { openDay() }.padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(LocalDate.parse(selectedDate).format(DateTimeFormatter.ofPattern("EEE, d MMM")), style = MaterialTheme.typography.titleSmall, color = CalendarInk)
            val eventCount = state.events.count { it.date == selectedDate }
            val shop = state.shopIncome.firstOrNull { it.date == selectedDate }
            val detail = listOf(if (eventCount == 0) "No events" else if (eventCount == 1) "1 event" else "$eventCount events", shop?.let { "Shop ${formatDh(it.amountCents)}" } ?: "No shop income").joinToString(" · ")
            Text(detail, style = MaterialTheme.typography.bodySmall, color = CalendarMuted)
        }
        Icon(Icons.Default.ChevronRight, "Open selected day", tint = CalendarMuted)
    }
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text("Worked", style = MaterialTheme.typography.bodyLarge, color = CalendarInk)
            val rate = state.rates.filter { it.effectiveDate <= selectedDate }.maxByOrNull { it.effectiveDate }
            if (rate != null) Text("${formatDh(rate.dailyAmountCents)} per day", style = MaterialTheme.typography.bodySmall, color = CalendarMuted)
            else Text("Set daily pay", style = MaterialTheme.typography.bodySmall, color = CalendarAccent, modifier = Modifier.clickable(enabled = enabled, onClick = openRates).heightIn(min = 48.dp).wrapContentHeight())
        }
        Switch(checked = selectedDate in worked, enabled = enabled, onCheckedChange = { viewModel.toggleWorkDay(selectedDate) }, modifier = Modifier.semantics { contentDescription = "Worked on ${LocalDate.parse(selectedDate).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))}" })
    }
    Text("All time", style = MaterialTheme.typography.labelSmall, color = CalendarMuted, modifier = Modifier.padding(top = 12.dp))
    Row(Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        listOf("Earned" to totals.earnedCents, "Spent" to totals.spentCents, "Net" to totals.netCents).forEach { (label, amount) ->
            Column(Modifier.weight(1f).padding(end = 8.dp)) { Text(label, color = CalendarMuted, style = MaterialTheme.typography.labelSmall); Text(formatDh(amount), color = if (label == "Net") CalendarAccent else CalendarInk, style = MaterialTheme.typography.bodyMedium) }
        }
    }
    Spacer(Modifier.height(16.dp))
}

@Composable
private fun MiniMonth(month: YearMonth, worked: Set<String>) {
    val offset = month.atDay(1).dayOfWeek.value - 1
    val fontScale = LocalDensity.current.fontScale
    val paint = remember { android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { textAlign = android.graphics.Paint.Align.CENTER } }
    // One drawing per thumbnail keeps opening the year from composing 588 tiny Text nodes.
    Canvas(Modifier.fillMaxWidth().padding(top = 8.dp).height((112 * fontScale).dp)) {
        val columnWidth = size.width / 7
        val rowHeight = size.height / 7
        paint.textSize = 10.sp.toPx()
        val baselineOffset = -(paint.ascent() + paint.descent()) / 2
        listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { column, label ->
            paint.color = android.graphics.Color.rgb(246, 244, 240)
            drawContext.canvas.nativeCanvas.drawText(label, columnWidth * (column + 0.5f), rowHeight * 0.5f + baselineOffset, paint)
        }
        repeat(month.lengthOfMonth()) { index ->
            val slot = index + offset
            paint.color = if (month.atDay(index + 1).toString() in worked) android.graphics.Color.rgb(201, 164, 106) else android.graphics.Color.rgb(165, 161, 155)
            drawContext.canvas.nativeCanvas.drawText((index + 1).toString(), columnWidth * (slot % 7 + 0.5f), rowHeight * (slot / 7 + 1.5f) + baselineOffset, paint)
        }
    }
}

private fun formatDh(cents: Long) = DecimalFormat("#,##0.00").format(BigDecimal.valueOf(cents, 2)) + " DH"
