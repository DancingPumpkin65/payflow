package com.dancingpumpkin.payflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ChoiceField(label: String, value: String, choices: List<String>, select: (String) -> Unit, custom: Boolean = false) {
    var expanded by remember { mutableStateOf(false) }
    var add by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    Box(Modifier.fillMaxWidth()) {
        Surface(onClick = { expanded = true }, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(Icons.Outlined.Category, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Column(Modifier.weight(1f)) {
                    Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(value)
                }
                RoundedArrow(ArrowDirection.DOWN)
            }
        }
        DropdownMenu(expanded, { expanded = false }, shape = RoundedCornerShape(24.dp), containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
            choices.distinct().forEach { option -> DropdownMenuItem(text = { Text(option, color = if (option == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }, onClick = { expanded = false; select(option) }) }
            if (custom) DropdownMenuItem(text = { Text("Custom category…") }, onClick = { expanded = false; name = ""; add = true })
        }
    }
    if (add) AlertDialog(onDismissRequest = { add = false }, title = { Text("Custom category") }, text = {
        OutlinedTextField(name, { name = it.take(40) }, singleLine = true, label = { Text("Name") })
    }, confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { select(choices.firstOrNull { it.equals(name.trim(), true) } ?: name.trim()); add = false }) { Text("Add") } }, dismissButton = { TextButton(onClick = { add = false }) { Text("Cancel") } })
}

internal val DefaultSpendingCategories = listOf("Transport", "Food", "Shopping", "Bills", "Uncategorized")
