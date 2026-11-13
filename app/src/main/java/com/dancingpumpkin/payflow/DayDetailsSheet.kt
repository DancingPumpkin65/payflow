package com.dancingpumpkin.payflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.dancingpumpkin.payflow.data.DayEventEntity
import com.dancingpumpkin.payflow.data.ShopIncomeEntity
import java.math.BigDecimal
import java.text.DecimalFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayDetailsSheet(date: LocalDate, state: PayflowState, model: PayflowViewModel, dismiss: () -> Unit) {
    if (!state.loaded) return
    val key = date.toString()
    val existing = state.shopIncome.firstOrNull { it.date == key }
    val events = state.events.filter { it.date == key }
    var editor by rememberSaveable(key) { mutableStateOf<String?>(null) }
    var title by rememberSaveable(key) { mutableStateOf("") }
    var eventNotes by rememberSaveable(key) { mutableStateOf("") }
    var amount by rememberSaveable(key) { mutableStateOf(existing?.let { BigDecimal.valueOf(it.amountCents, 2).toPlainString() } ?: "") }
    var notes by rememberSaveable(key) { mutableStateOf(existing?.notes ?: "") }
    val cents = runCatching { amount.replace(',', '.').toBigDecimal().movePointRight(2).longValueExact() }.getOrNull()?.takeIf { it in 0..100_000_000_000L }
    ModalBottomSheet(onDismissRequest = dismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 24.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(date.format(DateTimeFormatter.ofPattern("EEEE")), color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                    Text(date.format(DateTimeFormatter.ofPattern("d MMMM yyyy")), style = MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = dismiss) { Icon(Icons.Default.Close, "Close day") }
            }
            val worked = state.workDays.any { it.date == key }
            val rate = state.rates.filter { it.effectiveDate <= key }.maxByOrNull { it.effectiveDate }
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(if (worked) "Worked" else "Not worked", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                Text(if (worked && rate != null) DecimalFormat("#,##0.00").format(BigDecimal.valueOf(rate.dailyAmountCents, 2)) + " DH earned" else if (worked) "No pay rate set" else "", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Events", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = { editor = "event" }) { Icon(Icons.Default.Add, "Add event", tint = MaterialTheme.colorScheme.primary) }
            }
            if (events.isEmpty()) Text("No events planned", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
            events.forEach { event ->
                Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(event.title, style = MaterialTheme.typography.bodyLarge)
                        if (event.notes.isNotBlank()) Text(event.notes, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                    IconButton(onClick = { model.deleteEvent(event) }) { Icon(Icons.Default.DeleteOutline, "Delete ${event.title}", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("Shop income", style = MaterialTheme.typography.titleMedium)
                    Text(existing?.let { DecimalFormat("#,##0.00").format(BigDecimal.valueOf(it.amountCents, 2)) + " DH" } ?: "Not recorded", color = if (existing == null) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary, style = if (existing == null) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleLarge)
                }
                IconButton(onClick = { editor = "income" }) { Icon(if (existing == null) Icons.Default.Add else Icons.Default.Edit, if (existing == null) "Add shop income" else "Edit shop income", tint = MaterialTheme.colorScheme.primary) }
            }
            Text("Separate from your pay", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            if (!existing?.notes.isNullOrBlank()) Text(existing!!.notes, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (editor != null) AlertDialog(
        modifier = Modifier.safeDrawingPadding().imePadding(), properties = DialogProperties(decorFitsSystemWindows = false),
        onDismissRequest = { editor = null }, title = { Text(if (editor == "event") "New event" else "Shop income") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(date.format(DateTimeFormatter.ofPattern("d MMM yyyy")), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (editor == "event") {
                    OutlinedTextField(title, { title = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(eventNotes, { eventNotes = it }, label = { Text("Notes, optional") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                } else {
                    OutlinedTextField(amount, { amount = it }, label = { Text("Total in DH") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), modifier = Modifier.fillMaxWidth(), singleLine = true, isError = amount.isNotBlank() && cents == null)
                    if (amount.isNotBlank() && cents == null) Text("Use zero or a positive amount with up to 2 decimals", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    OutlinedTextField(notes, { notes = it }, label = { Text("Notes, optional") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                }
            }
        },
        confirmButton = { Button(enabled = if (editor == "event") title.isNotBlank() else cents != null, onClick = {
            if (editor == "event") { model.addEvent(DayEventEntity(date = key, title = title.trim(), notes = eventNotes.trim())); title = ""; eventNotes = "" }
            else model.saveShopIncome(ShopIncomeEntity(key, cents!!, notes.trim()))
            editor = null
        }) { Text(if (editor == "event") "Add event" else "Save income") } }, dismissButton = { TextButton(onClick = { editor = null }) { Text("Cancel") } },
    )
}
