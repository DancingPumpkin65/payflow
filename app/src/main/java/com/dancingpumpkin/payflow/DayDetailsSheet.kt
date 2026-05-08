package com.dancingpumpkin.payflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dancingpumpkin.payflow.data.DayEventEntity
import com.dancingpumpkin.payflow.data.ShopIncomeEntity
import java.math.BigDecimal
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayDetailsSheet(date: LocalDate, state: PayflowState, model: PayflowViewModel, dismiss: () -> Unit) {
    val key = date.toString()
    val existing = state.shopIncome.firstOrNull { it.date == key }
    var title by rememberSaveable(key) { mutableStateOf("") }
    var eventNotes by rememberSaveable(key) { mutableStateOf("") }
    var amount by rememberSaveable(key) { mutableStateOf(existing?.let { BigDecimal.valueOf(it.amountCents, 2).toPlainString() } ?: "") }
    var notes by rememberSaveable(key) { mutableStateOf(existing?.notes ?: "") }
    var saved by remember { mutableStateOf(false) }
    val cents = runCatching { amount.replace(',', '.').toBigDecimal().movePointRight(2).longValueExact() }.getOrNull()?.takeIf { it in 0..100_000_000_000L }
    ModalBottomSheet(onDismissRequest = dismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM")), style = MaterialTheme.typography.titleLarge)
            Text("Events", style = MaterialTheme.typography.titleMedium)
            val events = state.events.filter { it.date == key }
            if (events.isEmpty()) Text("No events", style = MaterialTheme.typography.bodyMedium)
            events.forEach { event ->
                Row(Modifier.fillMaxWidth()) {
                    Column(Modifier.weight(1f)) { Text(event.title); if (event.notes.isNotBlank()) Text(event.notes, style = MaterialTheme.typography.bodySmall) }
                    TextButton(onClick = { model.deleteEvent(event) }) { Text("Delete") }
                }
            }
            OutlinedTextField(title, { title = it }, label = { Text("Event title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            OutlinedTextField(eventNotes, { eventNotes = it }, label = { Text("Event notes") }, modifier = Modifier.fillMaxWidth())
            TextButton(enabled = title.isNotBlank(), onClick = { model.addEvent(DayEventEntity(date = key, title = title.trim(), notes = eventNotes.trim())); title = ""; eventNotes = "" }) { Text("Add event") }
            HorizontalDivider()
            Text("Shop income", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(amount, { amount = it; saved = false }, label = { Text("Day total in DH") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true, isError = amount.isNotBlank() && cents == null, supportingText = { if (amount.isNotBlank() && cents == null) Text("Enter zero or a positive amount, up to 2 decimals") })
            OutlinedTextField(notes, { notes = it; saved = false }, label = { Text("Income notes") }, modifier = Modifier.fillMaxWidth())
            Button(enabled = cents != null, onClick = { model.saveShopIncome(ShopIncomeEntity(key, cents!!, notes.trim())); saved = true }) { Text(if (saved) "Saved" else "Save shop income") }
        }
    }
}
