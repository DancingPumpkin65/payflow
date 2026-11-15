package com.dancingpumpkin.payflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dancingpumpkin.payflow.data.DayEventEntity
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventCard(event: DayEventEntity, showDate: Boolean = false, enabled: Boolean = true, edit: () -> Unit, delete: () -> Unit) {
    val swipe = rememberSwipeToDismissBoxState(confirmValueChange = { value ->
        if (value == SwipeToDismissBoxValue.StartToEnd) { delete(); true } else value == SwipeToDismissBoxValue.Settled
    }, positionalThreshold = { it * .35f })
    SwipeToDismissBox(state = swipe, enableDismissFromStartToEnd = enabled, enableDismissFromEndToStart = false,
        backgroundContent = {
            Surface(Modifier.fillMaxSize(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.errorContainer) {
                Box(Modifier.padding(20.dp), contentAlignment = Alignment.CenterStart) { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.onErrorContainer) }
            }
        }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF222326), modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).semantics {
            customActions = if (enabled) listOf(CustomAccessibilityAction("Delete event") { delete(); true }) else emptyList()
        }.clickable(enabled = enabled, onClickLabel = "Edit event", onClick = edit)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(3.dp).height(28.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                Column(Modifier.padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (showDate) Text(LocalDate.parse(event.date).format(DateTimeFormatter.ofPattern("d MMM yyyy")), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(event.title, style = MaterialTheme.typography.bodyLarge)
                    if (event.notes.isNotBlank()) Text(event.notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
