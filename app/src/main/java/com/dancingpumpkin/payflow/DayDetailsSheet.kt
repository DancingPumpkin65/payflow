package com.dancingpumpkin.payflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.dp
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
    ModalBottomSheet(onDismissRequest = dismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(bottom = 20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                IconButton(onClick = {
                    editor = "income"
                }) { Icon(if (existing == null) Icons.Default.Add else Icons.Default.Edit, if (existing == null) "Add shop income" else "Edit shop income", tint = MaterialTheme.colorScheme.primary) }
            }
            Text("Separate from your pay", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
            if (!existing?.notes.isNullOrBlank()) Text(existing!!.notes, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
        }
    }
    if (editor != null) DayEntryEditor(date, state, model, if (editor == "event") DayEntryTab.EVENT else DayEntryTab.INCOME) { editor = null }
}
