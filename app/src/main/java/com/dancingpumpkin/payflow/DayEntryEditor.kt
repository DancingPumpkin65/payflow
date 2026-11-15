package com.dancingpumpkin.payflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.ArrowRight
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
import java.time.ZoneOffset
import java.time.Instant
import java.time.format.DateTimeFormatter

internal enum class DayEntryTab(val label: String) { EVENT("Event"), INCOME("Shop income") }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DayEntryEditor(initialDate: LocalDate, state: PayflowState, model: PayflowViewModel, initialTab: DayEntryTab, initialEvent: DayEventEntity? = null, dismiss: () -> Unit) {
    val tab = initialTab
    var date by rememberSaveable { mutableStateOf(initialEvent?.date ?: initialDate.toString()) }
    val existing = state.shopIncome.firstOrNull { it.date == date }
    var title by rememberSaveable { mutableStateOf(initialEvent?.title ?: "") }
    var eventNotes by rememberSaveable { mutableStateOf(initialEvent?.notes ?: "") }
    var amount by rememberSaveable(date) { mutableStateOf(existing?.let { BigDecimal.valueOf(it.amountCents, 2).toPlainString() } ?: "") }
    var incomeNotes by rememberSaveable(date) { mutableStateOf(existing?.notes ?: "") }
    var chooseDate by rememberSaveable { mutableStateOf(false) }
    val cents = runCatching { amount.replace(',', '.').toBigDecimal().movePointRight(2).longValueExact() }.getOrNull()?.takeIf { it in 0..100_000_000_000L }
    val valid = if (tab == DayEntryTab.EVENT) title.isNotBlank() else cents != null
    FormEditor(
        title = if (tab == DayEntryTab.EVENT) if (initialEvent == null) "New event" else "Edit event" else "Shop income",
        dismiss = dismiss,
        action = {
            IconButton(enabled = valid, onClick = {
                if (tab == DayEntryTab.EVENT) {
                    val event = DayEventEntity(id = initialEvent?.id ?: 0, date = date, title = title.trim(), notes = eventNotes.trim())
                    if (initialEvent == null) model.addEvent(event) else model.updateEvent(event)
                }
                else model.saveShopIncome(ShopIncomeEntity(date, cents!!, incomeNotes.trim()))
                dismiss()
            }) { Icon(Icons.Default.Check, if (tab == DayEntryTab.EVENT) "Save event" else "Save shop income") }
        },
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                val colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    errorContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                    focusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                    unfocusedIndicatorColor = MaterialTheme.colorScheme.outlineVariant,
                )
                if (tab == DayEntryTab.EVENT) {
                    TextField(title, { title = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Title") }, leadingIcon = { Icon(Icons.Default.Title, null) }, singleLine = true, colors = colors)
                } else {
                    TextField(amount, { amount = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Total in DH") }, leadingIcon = { Icon(Icons.Default.Payments, null) }, singleLine = true, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = amount.isNotBlank() && cents == null, colors = colors)
                    if (amount.isNotBlank() && cents == null) Text("Use zero or a positive amount with up to 2 decimals", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 16.dp))
                }
                TextButton(onClick = { chooseDate = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) {
                    Icon(Icons.Default.CalendarToday, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Date", modifier = Modifier.padding(start = 16.dp).weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy")), color = MaterialTheme.colorScheme.onSurface)
                    RoundedArrow(ArrowDirection.RIGHT, "Choose entry date")
                }
            }
        }
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
            TextField(
                value = if (tab == DayEntryTab.EVENT) eventNotes else incomeNotes,
                onValueChange = { if (tab == DayEntryTab.EVENT) eventNotes = it else incomeNotes = it },
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                label = { Text("Notes, optional") }, leadingIcon = { Icon(Icons.AutoMirrored.Filled.Notes, null) }, minLines = 2,
                colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer, focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent, unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent),
            )
        }
        if (tab == DayEntryTab.INCOME) Text("Shop revenue is separate from your pay.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(horizontal = 8.dp))
    }
    if (chooseDate) {
        val picker = rememberDatePickerState(initialSelectedDateMillis = LocalDate.parse(date).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli())
        DatePickerDialog(onDismissRequest = { chooseDate = false }, confirmButton = {
            TextButton(onClick = { picker.selectedDateMillis?.let { date = Instant.ofEpochMilli(it).atOffset(ZoneOffset.UTC).toLocalDate().toString() }; chooseDate = false }) { Text("Done") }
        }, dismissButton = { TextButton(onClick = { chooseDate = false }) { Text("Cancel") } }) {
            DatePicker(picker, title = null, headline = null, showModeToggle = false)
        }
    }
}
