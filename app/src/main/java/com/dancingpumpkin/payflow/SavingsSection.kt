package com.dancingpumpkin.payflow

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.dancingpumpkin.payflow.data.*
import java.math.BigDecimal
import java.time.*

internal fun parsePositiveMoney(value: String): Long? = runCatching { value.replace(',', '.').toBigDecimal().movePointRight(2).longValueExact() }.getOrNull()?.takeIf { it in 1..100_000_000_000L }

@Composable private fun SavingsAmountField(value: String, label: String, change: (String) -> Unit) {
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        TextField(value, change, label = { Text(label) }, leadingIcon = { Icon(Icons.Default.Savings, null) }, modifier = Modifier.fillMaxWidth().padding(8.dp), singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal), isError = value.isNotEmpty() && parsePositiveMoney(value) == null,
            colors = TextFieldDefaults.colors(focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer, unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer, errorContainerColor = MaterialTheme.colorScheme.surfaceContainer))
    }
}

@Composable
internal fun SavingsSection(state: PayflowState, model: PayflowViewModel, month: YearMonth, ledger: String) {
    val data = state.analyze(month.atDay(1), month.atEndOfMonth(), ledger)
    val goal = state.goals.firstOrNull { it.month == month.toString() && it.ledger == ledger }
    var goalEditor by rememberSaveable(month, ledger) { mutableStateOf(false) }
    var transferEditor by rememberSaveable(month, ledger) { mutableStateOf(false) }
    var editId by rememberSaveable(month, ledger) { mutableStateOf<Long?>(null) }
    val progress by animateFloatAsState(data.progress(goal?.targetCents ?: 0, goal?.basis ?: "net"), tween(500, easing = LinearOutSlowInEasing), label = "Savings progress")
    Surface(shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("SAVINGS", Modifier.weight(1f), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                IconButton(onClick = { goalEditor = true }) { Icon(Icons.Default.Edit, "Set savings target") }
            }
            AmountRow("Available net", data.net)
            AmountRow("Recorded savings", data.transfers)
            if (goal != null) {
                Text("${formatMoney(data.saved(goal.basis))} of ${formatMoney(goal.targetCents)}", style = MaterialTheme.typography.titleMedium)
                LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
                Text(if (goal.basis == "transfers") "Target uses recorded transfers" else "Target uses positive net income", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else TextButton(onClick = { goalEditor = true }) { Text("Set monthly target") }
            TextButton(onClick = { editId = null; transferEditor = true }) { Icon(Icons.Default.Add, null); Text("Record savings transfer", Modifier.padding(start = 8.dp)) }
        }
    }
    state.transfers.filter { it.ledger == ledger && it.date.startsWith(month.toString()) }.forEach { transfer -> key(transfer.id) {
        RecordCard("Savings transfer", transfer.date, amount = formatMoney(transfer.amountCents), kind = "savings transfer", edit = { editId = transfer.id; transferEditor = true }, delete = { model.deleteTransfer(transfer) })
    } }
    if (goalEditor) {
        var amount by rememberSaveable { mutableStateOf(goal?.let { BigDecimal.valueOf(it.targetCents, 2).toPlainString() } ?: "") }
        var basis by rememberSaveable { mutableStateOf(if (goal?.basis == "transfers") "Recorded transfers" else "Net income") }
        val cents = parsePositiveMoney(amount)
        FormEditor("Monthly savings target", { goalEditor = false }, action = { finish ->
            if (goal != null) IconButton(onClick = { finish { model.deleteGoal(goal); goalEditor = false } }) { Icon(Icons.Default.Delete, "Remove savings target") }
            IconButton(enabled = cents != null, onClick = { finish { model.saveGoal(SavingsGoalEntity(month.toString(), ledger, cents!!, if (basis == "Recorded transfers") "transfers" else "net")); goalEditor = false } }) { Icon(Icons.Default.Check, "Save savings target") }
        }) {
            Text("$month · ${if (ledger == "shop") "Shop" else "Personal"}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            SavingsAmountField(amount, "Target in DH") { amount = it }
            ChoiceField("Track progress using", basis, listOf("Net income", "Recorded transfers"), { basis = it })
            Text("Transfers move money into savings; they do not reduce net income and are never added to it.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (transferEditor) {
        val initial = state.transfers.firstOrNull { it.id == editId }
        var amount by rememberSaveable { mutableStateOf(initial?.let { BigDecimal.valueOf(it.amountCents, 2).toPlainString() } ?: "") }
        var date by rememberSaveable { mutableStateOf(initial?.date ?: if (YearMonth.now() == month) LocalDate.now().toString() else month.atDay(1).toString()) }
        val cents = parsePositiveMoney(amount)
        FormEditor(if (initial == null) "Savings transfer" else "Edit savings transfer", { transferEditor = false }, action = { finish ->
            IconButton(enabled = cents != null, onClick = { finish { model.saveTransfer(SavingsTransferEntity(initial?.id ?: 0, date, ledger, cents!!)); transferEditor = false } }) { Icon(Icons.Default.Check, "Save savings transfer") }
        }) {
            SavingsAmountField(amount, "Amount in DH") { amount = it }
            DateField("Date", date) { date = it }
        }
    }
}
