package com.dancingpumpkin.payflow

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
internal fun ExpenseCategoryPicker(category: String, select: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        Surface(onClick = { expanded = true }, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer, modifier = Modifier.fillMaxWidth()) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Icon(if (category == "shop") Icons.Outlined.Storefront else Icons.Outlined.AccountBalanceWallet, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(if (category == "shop") "Shop expenses" else "Personal expenses", modifier = Modifier.weight(1f))
                RoundedArrow(ArrowDirection.DOWN)
            }
        }
        DropdownMenu(expanded, { expanded = false }, shape = RoundedCornerShape(24.dp), containerColor = MaterialTheme.colorScheme.surfaceContainerHigh) {
            listOf("personal" to "Personal expenses", "shop" to "Shop expenses").forEach { (value, label) ->
                DropdownMenuItem(text = { Text(label, color = if (category == value) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) }, onClick = { expanded = false; select(value) })
            }
        }
    }
}
