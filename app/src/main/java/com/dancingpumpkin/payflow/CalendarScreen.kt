package com.dancingpumpkin.payflow

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.animation.core.spring
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.DayOfWeek
import java.time.temporal.WeekFields
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlinx.coroutines.launch

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.zIndex
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.dancingpumpkin.payflow.domain.workDayRange
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.material.icons.filled.Search
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalFocusManager
import com.dancingpumpkin.payflow.data.DayEventEntity

private val CalendarAccent = Color(0xFF72A7FF)
private val CalendarInk = Color(0xFFF6F4F0)
private val CalendarMuted = Color(0xFFA5A19B)

private fun monthIndex(month: YearMonth) = (month.year - 1900) * 12 + month.monthValue - 1
private fun monthAt(index: Int) = YearMonth.of(1900 + index / 12, index % 12 + 1)

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun CalendarScreen(state: PayflowState, viewModel: PayflowViewModel, openRates: () -> Unit, openExpenses: () -> Unit, openDashboard: () -> Unit, requestedDate: String? = null, requestedEventId: Long? = null, consumed: () -> Unit = {}) {
    val now = YearMonth.now()
    val pager = rememberPagerState(initialPage = monthIndex(now), pageCount = { 2400 })
    var yearMode by rememberSaveable { mutableStateOf(false) }
    var selectedIndex by rememberSaveable { mutableIntStateOf(pager.currentPage) }
    val yearPager = rememberPagerState(initialPage = now.year - 1900, pageCount = { 200 })
    val year = 1900 + yearPager.currentPage
    var selectedDate by rememberSaveable { mutableStateOf(LocalDate.now().toString()) }
    var editor by rememberSaveable { mutableStateOf<DayEntryTab?>(null) }
    var editingEventId by rememberSaveable { mutableStateOf<Long?>(null) }
    var searchOpen by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val searchProgress = remember { Animatable(if (searchOpen) 1f else 0f) }
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    fun closeSearch() { searchOpen = false; searchQuery = ""; focusManager.clearFocus(); keyboard?.hide() }
    LaunchedEffect(searchOpen) { searchProgress.animateTo(if (searchOpen) 1f else 0f, tween(if (searchOpen) 220 else 176, easing = LinearOutSlowInEasing)) }
    var showActions by rememberSaveable { mutableStateOf(false) }
    var rangeSelecting by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val columns = 3
    val month = monthAt(pager.currentPage)
    val worked = remember(state.workDays) { state.workDays.map { it.date }.toSet() }
    val progress = remember { Animatable(if (yearMode) 0f else 1f) }
    val agendaProgress = remember { Animatable(if (yearMode) 0f else 1f) }
    val agendaVisible by remember { derivedStateOf { agendaProgress.value > 0f } }
    val thumbnails = remember { mutableMapOf<Int, Rect>() }
    var gridBounds by remember { mutableStateOf(Rect.Zero) }
    var yearBounds by remember { mutableStateOf(Rect.Zero) }
    var summaryHeight by remember { mutableIntStateOf(0) }
    var headerHeight by remember { mutableIntStateOf(0) }
    val yearScroll = rememberScrollState()
    val monthEnabled = state.loaded && !yearMode && !progress.isRunning && !searchOpen
    val yearEnabled = state.loaded && yearMode && !progress.isRunning && !searchOpen
    LaunchedEffect(yearMode) {
        showActions = false
        launch { agendaProgress.animateTo(if (yearMode) 0f else 1f, tween(if (yearMode) 256 else 320, easing = if (yearMode) FastOutLinearInEasing else LinearOutSlowInEasing)) }
        progress.animateTo(if (yearMode) 0f else 1f, tween(if (yearMode) 256 else 320, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(pager.settledPage) {
        val visible = monthAt(pager.settledPage)
        if (YearMonth.from(LocalDate.parse(selectedDate)) != visible) selectedDate = visible.atDay(1).toString()
    }
    BackHandler(yearMode) { yearMode = false }
    BackHandler(showActions) { showActions = false }
    BackHandler(searchOpen) { closeSearch() }
    fun editEvent(event: DayEventEntity) {
        if (searchOpen) { focusManager.clearFocus(); keyboard?.hide() } else closeSearch()
        yearMode = false
        editingEventId = event.id
        selectedDate = event.date
        pager.requestScrollToPage(monthIndex(YearMonth.from(LocalDate.parse(event.date))))
        editor = DayEntryTab.EVENT
    }
    LaunchedEffect(requestedDate, state.loaded) {
        if (requestedDate != null && state.loaded) {
            val date = LocalDate.parse(requestedDate)
            selectedDate = requestedDate
            pager.requestScrollToPage(monthIndex(YearMonth.from(date)).coerceIn(0, 2399))
            yearMode = false
            if (requestedEventId != null && state.events.any { it.id == requestedEventId }) { editingEventId = requestedEventId; editor = DayEntryTab.EVENT }
            consumed()
        }
    }
    editor?.let { destination ->
        val activeId = editingEventId
        key(destination, activeId) {
            DayEntryEditor(LocalDate.parse(selectedDate), state, viewModel, destination, state.events.firstOrNull { it.id == activeId }) {
                if (editor == destination && editingEventId == activeId) { editor = null; editingEventId = null }
            }
        }
    }

    BoxWithConstraints(Modifier.fillMaxSize().clipToBounds()) {
        val weekHeight = with(density) { 14.sp.toDp() } + 8.dp
        val toolbarHeight = with(density) { headerHeight.toDp() }.coerceAtLeast(56.dp)
        val pageHeight = maxOf(maxHeight, toolbarHeight + weekHeight + 288.dp + 80.dp + with(density) { summaryHeight.toDp() })
        val yearHeight = (maxHeight - toolbarHeight).coerceAtLeast(120.dp)
        fun chooseYear() {
            if (progress.isRunning) return
            if (yearMode) { yearMode = false; return }
            selectedIndex = pager.currentPage
            yearPager.requestScrollToPage(month.year - 1900)
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
        // Short windows scroll the page instead of compressing date touch targets.
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState(), enabled = !rangeSelecting).then(if (searchOpen && searchQuery.isNotBlank()) Modifier.clearAndSetSemantics { } else Modifier)) {
            Column(Modifier.fillMaxWidth().height(pageHeight).padding(horizontal = 12.dp)) {
                Row(Modifier.fillMaxWidth().padding(start = 12.dp).heightIn(min = 56.dp).onSizeChanged { headerHeight = it.height }.graphicsLayer { alpha = 1f - searchProgress.value; translationY = -12.dp.toPx() * searchProgress.value }.then(if (searchOpen) Modifier.clearAndSetSemantics { } else Modifier), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.weight(1f).clickable(enabled = !searchOpen, onClick = ::chooseYear).heightIn(min = 48.dp).semantics {
                        role = Role.Button
                        contentDescription = "Choose month and year"
                    }, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (yearMode) year.toString() else month.format(DateTimeFormatter.ofPattern("MMM yyyy")), color = CalendarInk, style = MaterialTheme.typography.titleLarge)
                        RoundedArrow(if (yearMode) ArrowDirection.UP else ArrowDirection.DOWN)
                        }
                        }
                    }
                    IconButton(enabled = !searchOpen && !progress.isRunning, onClick = { showActions = false; showMenu = false; searchOpen = true }) { SearchGlyph("Search events") }
                    Box {
                        IconButton(enabled = !searchOpen, onClick = { showMenu = true }) { CalendarMenuIcon() }
                        CalendarOverflowMenu(showMenu, { showMenu = false }, openRates, openExpenses, openDashboard)
                    }
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    // Keep both endpoints measured. Only the month layer moves, so
                    // neither lazy composition nor parent reflow stalls the first frame.
                    Column(Modifier.fillMaxWidth().height(yearHeight).onGloballyPositioned { yearBounds = Rect(it.positionInRoot(), it.size.toSize()) }.zIndex(if (yearEnabled) 2f else 0f).graphicsLayer { alpha = 1f - progress.value }
                        .then(if (!yearMode) Modifier.clearAndSetSemantics { } else Modifier)) {
                        HorizontalPager(yearPager, userScrollEnabled = yearEnabled, modifier = Modifier.fillMaxSize()) { yearPage ->
                        Column(Modifier.fillMaxSize().verticalScroll(yearScroll, enabled = yearEnabled)) {
                        repeat(12 / columns) { row ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                repeat(columns) { column ->
                                    val item = YearMonth.of(1900 + yearPage, row * columns + column + 1)
                                    val index = monthIndex(item)
                                    Column(Modifier.weight(1f).onGloballyPositioned { thumbnails[index] = Rect(it.positionInRoot(), it.size.toSize()) }
                                        .padding(vertical = 6.dp).clickable(enabled = yearEnabled) {
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
                                        }.border(if (item == month) 1.dp else 0.dp, if (item == month) CalendarAccent else Color.Transparent, MaterialTheme.shapes.small).padding(4.dp)) {
                                        Text(item.month.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase(Locale.getDefault()), color = if (item == month) CalendarAccent else CalendarInk, style = MaterialTheme.typography.labelLarge)
                                        MiniMonth(item, worked)
                                    }
                                }
                            }
                        }
                        }
                        }
                    }
                    Column(Modifier.fillMaxSize().zIndex(1f).then(if (yearMode) Modifier.clearAndSetSemantics { } else Modifier)) {
                        val chrome = Modifier.graphicsLayer { alpha = ((progress.value - 0.8f) / 0.2f).coerceIn(0f, 1f) }
                        val agendaMotion = Modifier.graphicsLayer { alpha = agendaProgress.value; translationY = (1f - agendaProgress.value) * 160.dp.toPx() }
                        Column(Modifier.weight(1f)) {
                        Box(Modifier.height(weekHeight + 288.dp).fillMaxWidth().onGloballyPositioned { gridBounds = Rect(it.positionInRoot(), it.size.toSize()) }) {
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
                                HorizontalPager(pager, modifier = Modifier.fillMaxSize(), userScrollEnabled = monthEnabled && !rangeSelecting) { page ->
                                    MonthGrid(monthAt(page), worked, state, selectedDate, monthEnabled, { date ->
                                        selectedDate = date.toString()
                                        if (YearMonth.from(date) != monthAt(page)) scope.launch { pager.animateScrollToPage(monthIndex(YearMonth.from(date))) }
                                    }, { dates, mark -> viewModel.setWorkDays(dates, mark) }, { rangeSelecting = it })
                                }
                            }
                        }
                        Column(agendaMotion.padding(horizontal = 12.dp).onSizeChanged { summaryHeight = it.height }) {
                            DaySummary(state, viewModel, selectedDate, worked, monthEnabled, openRates)
                        }
                        DayAgenda(state, viewModel, selectedDate, monthEnabled, ::editEvent, agendaMotion.weight(1f).fillMaxWidth())
                        }
                    }
                }
            }
        }
        if (showActions && monthEnabled) Box(Modifier.fillMaxSize().zIndex(3f).clickable(
            interactionSource = remember { MutableInteractionSource() }, indication = null,
        ) { showActions = false })
        if (agendaVisible && searchProgress.value < 1f) Box(Modifier.fillMaxSize().padding(24.dp).zIndex(4f).graphicsLayer { alpha = agendaProgress.value * (1f - searchProgress.value); translationY = (1f - agendaProgress.value) * 160.dp.toPx() }, contentAlignment = Alignment.BottomEnd) {
            EntrySpeedDial(showActions, { if (monthEnabled && !searchOpen) showActions = !showActions }) { destination -> showActions = false; editingEventId = null; editor = destination }
        }
        if (searchOpen && searchQuery.isNotBlank()) {
            val matches = state.events.filter { it.title.contains(searchQuery.trim(), ignoreCase = true) || it.notes.contains(searchQuery.trim(), ignoreCase = true) }
            Column(Modifier.fillMaxSize().padding(top = toolbarHeight).background(Color.Black).imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 24.dp, vertical = 12.dp).zIndex(2f)) {
                if (matches.isEmpty()) Text("No matching events", color = CalendarMuted, modifier = Modifier.padding(vertical = 20.dp))
                matches.forEach { event -> key(event.id) { EventCard(event, showDate = true, edit = { editEvent(event) }, delete = { viewModel.deleteEvent(event) }) } }
            }
        }
        if (searchOpen || searchProgress.value > 0f) {
            CalendarSearchHeader(searchOpen, searchQuery, { searchQuery = it }, ::closeSearch,
                Modifier.align(Alignment.TopCenter).zIndex(5f).graphicsLayer { alpha = searchProgress.value; translationY = -16.dp.toPx() * (1f - searchProgress.value) })
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MonthGrid(item: YearMonth, worked: Set<String>, state: PayflowState, selectedDate: String, enabled: Boolean, select: (LocalDate) -> Unit, commitRange: (List<String>, Boolean) -> Unit, selecting: (Boolean) -> Unit) {
    val locale = Locale.getDefault()
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val start = item.atDay(1).minusDays(((item.atDay(1).dayOfWeek.value - firstDay.value + 7) % 7).toLong())
    val haptics = LocalHapticFeedback.current
    val currentWorked by rememberUpdatedState(worked)
    val currentCommit by rememberUpdatedState(commitRange)
    val currentSelecting by rememberUpdatedState(selecting)
    var preview by remember(item) { mutableStateOf<Set<String>?>(null) }
    var dragging by remember(item) { mutableStateOf(false) }
    var liquidPair by remember(item) { mutableStateOf<Pair<LocalDate, LocalDate>?>(null) }
    var liquidEndpoint by remember(item) { mutableStateOf("") }
    val liquid = remember(item) { Animatable(0f) }
    LaunchedEffect(liquidPair) {
        liquid.snapTo(0f)
        if (liquidPair != null) liquid.animateTo(1f, tween(280, easing = FastOutSlowInEasing))
    }
    LaunchedEffect(worked) { if (!dragging) preview = null }
    val displayedWorked = preview ?: worked
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            repeat(7) { column ->
                Text(firstDay.plus(column.toLong()).getDisplayName(TextStyle.SHORT, locale).uppercase(locale), color = CalendarMuted, fontSize = 12.sp, lineHeight = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        Column(Modifier.weight(1f).fillMaxWidth().drawBehind {
            val tension = kotlin.math.sin(liquid.value.coerceIn(0f, 1f) * Math.PI).toFloat().coerceAtLeast(0f)
            if (tension > 0f) repeat(6) { row ->
                repeat(6) { column ->
                    val date = start.plusDays((row * 7 + column).toLong())
                    if (liquidPair?.let { (a, b) -> minOf(a, b) == date && maxOf(a, b) == date.plusDays(1) } == true) {
                        val a = Offset(size.width / 7 * (column + .5f), size.height / 6 * (row + .5f) - 3.dp.toPx())
                        val b = a + Offset(size.width / 7, 0f)
                        liquidBridge(a, b, 18.dp.toPx(), 18.dp.toPx(), CalendarAccent.copy(alpha = tension), tension)
                    }
                }
            }
        }.pointerInput(item, enabled) {
            if (!enabled) return@pointerInput
            var anchor = start
            var endpoint = start
            var mark = true
            var baseline = emptySet<String>()
            fun dateAt(position: Offset): LocalDate {
                val column = (position.x / (size.width / 7f)).toInt().coerceIn(0, 6)
                val row = (position.y / (size.height / 6f)).toInt().coerceIn(0, 5)
                return start.plusDays((row * 7 + column).toLong())
            }
            fun update() {
                val range = workDayRange(anchor, endpoint, item).toSet()
                preview = if (mark) baseline + range else baseline - range
                liquidEndpoint = endpoint.toString()
            }
            detectDragGesturesAfterLongPress(
                onDragStart = startDrag@{
                    baseline = currentWorked
                    anchor = dateAt(it)
                    if (YearMonth.from(anchor) != item) return@startDrag
                    endpoint = anchor
                    mark = anchor.toString() !in baseline
                    currentSelecting(true)
                    dragging = true
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    update()
                },
                onDrag = { change, _ ->
                    if (dragging) {
                        change.consume()
                        val next = dateAt(change.position).coerceIn(item.atDay(1), item.atEndOfMonth())
                        if (next != endpoint) {
                            val previous = if (next > endpoint) next.minusDays(1) else next.plusDays(1)
                            liquidPair = previous to next
                            endpoint = next
                            update()
                        }
                    }
                },
                onDragEnd = {
                    if (dragging) currentCommit(workDayRange(anchor, endpoint, item), mark)
                    liquidPair = null
                    dragging = false
                    currentSelecting(false)
                },
                onDragCancel = { preview = null; liquidPair = null; liquidEndpoint = ""; dragging = false; currentSelecting(false) },
            )
        }) {
        repeat(6) { row ->
            Row(Modifier.fillMaxWidth().weight(1f)) {
                repeat(7) { column ->
                    val date = start.plusDays((row * 7 + column).toLong())
                    val isWorked = YearMonth.from(date) == item && date.toString() in displayedWorked
                    val today = date == LocalDate.now()
                    val selected = date.toString() == selectedDate
                    val interaction = remember { MutableInteractionSource() }
                    Box(Modifier.weight(1f).fillMaxHeight().clickable(
                        enabled = enabled, role = Role.Button,
                        interactionSource = interaction, indication = null,
                        onClick = { select(date) },
                    ).semantics {
                        contentDescription = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy"))
                        this.selected = selected
                        stateDescription = listOfNotNull(if (today) "Today" else null, if (isWorked) "Worked" else "Not worked").joinToString(", ")
                    }, contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(Modifier.size(42.dp).then(pressFeedback(interaction)).graphicsLayer {
                            val squish = if (liquidPair != null && date.toString() == liquidEndpoint) kotlin.math.sin(liquid.value.coerceIn(0f, 1f) * Math.PI).toFloat() else 0f
                            scaleX = 1f + .14f * squish
                            scaleY = 1f - .08f * squish
                        }.clip(CircleShape).indication(interaction, ripple(bounded = true)).then(if (selected) Modifier.border(1.dp, CalendarAccent, CircleShape) else Modifier).padding(3.dp).clip(CircleShape).background(if (isWorked) MaterialTheme.colorScheme.primary else if (selected) Color(0xFF152B4D) else Color.Transparent), contentAlignment = Alignment.Center) {
        Text(date.dayOfMonth.toString(), style = MaterialTheme.typography.bodyLarge, fontSize = 18.sp, fontWeight = if (today) FontWeight.Bold else FontWeight.Normal, color = when {
                                isWorked && date.month != item.month -> CalendarInk.copy(alpha = 0.8f)
                                isWorked -> MaterialTheme.colorScheme.onPrimary
                                date.month != item.month -> CalendarInk.copy(alpha = 0.5f)
                                today || selected -> CalendarAccent
                            date.dayOfWeek == DayOfWeek.SATURDAY || date.dayOfWeek == DayOfWeek.SUNDAY -> CalendarInk.copy(alpha = 0.65f)
                                else -> CalendarInk
                            })
                        }
                        val hasDetails = state.events.any { it.date == date.toString() } || state.shopIncome.any { it.date == date.toString() }
                        Box(Modifier.padding(top = 2.dp).size(4.dp).clip(CircleShape).background(if (hasDetails) CalendarAccent else Color.Transparent))
                        }
                    }
                }
            }
        }
        }
    }
}

@Composable
private fun DaySummary(state: PayflowState, viewModel: PayflowViewModel, selectedDate: String, worked: Set<String>, enabled: Boolean, openRates: () -> Unit) {
    val events = state.events.filter { it.date == selectedDate }
    val shop = state.shopIncome.firstOrNull { it.date == selectedDate }
    val rate = state.rates.filter { it.effectiveDate <= selectedDate }.maxByOrNull { it.effectiveDate }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
    Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f).heightIn(min = 48.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(LocalDate.parse(selectedDate).format(DateTimeFormatter.ofPattern("EEE, d MMM")), style = MaterialTheme.typography.titleSmall, color = CalendarInk)
            }
            if (rate != null) Text("${formatDh(rate.dailyAmountCents)} per day", style = MaterialTheme.typography.bodySmall, color = CalendarMuted)
            else Text("Set daily pay", style = MaterialTheme.typography.bodySmall, color = CalendarAccent, modifier = Modifier.clickable(enabled = enabled, onClick = openRates).heightIn(min = 48.dp).wrapContentHeight())
            val details = listOfNotNull(if (events.isEmpty()) null else if (events.size == 1) "1 event" else "${events.size} events", shop?.let { "Shop ${formatDh(it.amountCents)}" }).joinToString(" · ")
            if (details.isNotEmpty()) Text(details, style = MaterialTheme.typography.bodySmall, color = CalendarMuted)
        }
        Column(Modifier.padding(start = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Worked", style = MaterialTheme.typography.labelSmall, color = CalendarMuted)
            LiquidWorkedSwitch(selectedDate in worked, enabled, "Worked on ${LocalDate.parse(selectedDate).format(DateTimeFormatter.ofPattern("d MMMM yyyy"))}") { viewModel.toggleWorkDay(selectedDate) }
        }
    }
}

@Composable
private fun DayAgenda(state: PayflowState, model: PayflowViewModel, date: String, enabled: Boolean, edit: (DayEventEntity) -> Unit, modifier: Modifier) {
    val events = state.events.filter { it.date == date }
    val notes = state.shopIncome.firstOrNull { it.date == date }?.notes.orEmpty()
    if (events.isEmpty() && notes.isBlank()) EmptyEvents(modifier)
    else Column(modifier.verticalScroll(rememberScrollState(), enabled = enabled).padding(horizontal = 12.dp).padding(bottom = 96.dp)) {
        if (notes.isNotBlank()) Text(notes, color = CalendarMuted, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(bottom = 8.dp))
        events.forEach { event -> key(event.id) { EventCard(event, enabled = enabled, edit = { edit(event) }, delete = { model.deleteEvent(event) }) } }
        if (events.isEmpty()) EmptyEvents(Modifier.fillMaxWidth().height(112.dp))
    }
}

@Composable
private fun MiniMonth(month: YearMonth, worked: Set<String>) {
    val locale = Locale.getDefault()
    val firstDay = WeekFields.of(locale).firstDayOfWeek
    val offset = (month.atDay(1).dayOfWeek.value - firstDay.value + 7) % 7
    val fontScale = LocalDensity.current.fontScale
    val paint = remember { android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply { textAlign = android.graphics.Paint.Align.CENTER; fontFeatureSettings = "tnum" } }
    // One drawing per thumbnail keeps opening the year from composing 588 tiny Text nodes.
    Canvas(Modifier.fillMaxWidth().padding(top = 6.dp).height((98 * fontScale).dp)) {
        val columnWidth = size.width / 7
        val rowHeight = size.height / 7
        paint.textSize = 10.sp.toPx()
        val baselineOffset = -(paint.ascent() + paint.descent()) / 2
        repeat(7) { column ->
            paint.color = android.graphics.Color.rgb(246, 244, 240)
            drawContext.canvas.nativeCanvas.drawText(firstDay.plus(column.toLong()).getDisplayName(TextStyle.NARROW, locale), columnWidth * (column + 0.5f), rowHeight * 0.5f + baselineOffset, paint)
        }
        repeat(month.lengthOfMonth()) { index ->
            val slot = index + offset
            paint.color = if (month.atDay(index + 1).toString() in worked) android.graphics.Color.rgb(114, 167, 255) else android.graphics.Color.rgb(165, 161, 155)
            drawContext.canvas.nativeCanvas.drawText((index + 1).toString(), columnWidth * (slot % 7 + 0.5f), rowHeight * (slot / 7 + 1.5f) + baselineOffset, paint)
        }
    }
}

private fun formatDh(cents: Long) = DecimalFormat("#,##0.00").format(BigDecimal.valueOf(cents, 2)) + " DH"
