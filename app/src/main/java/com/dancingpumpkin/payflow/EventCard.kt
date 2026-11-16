package com.dancingpumpkin.payflow

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.layout.onSizeChanged
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
    RecordCard(event.title, listOfNotNull(if (showDate) LocalDate.parse(event.date).format(DateTimeFormatter.ofPattern("d MMM yyyy")) else null, event.time).joinToString(" · ").ifBlank { null },
        notes = event.notes, kind = "event", enabled = enabled, edit = edit, delete = delete)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun RecordCard(title: String, subtitle: String? = null, notes: String = "", amount: String? = null, kind: String, enabled: Boolean = true, edit: () -> Unit, delete: () -> Unit) {
    var width by remember { mutableIntStateOf(0) }
    var deleting by remember { mutableStateOf(false) }
    val interaction = remember { MutableInteractionSource() }
    lateinit var swipe: SwipeToDismissBoxState
    swipe = rememberSwipeToDismissBoxState(confirmValueChange = { value ->
        when (value) {
            SwipeToDismissBoxValue.Settled -> true
            SwipeToDismissBoxValue.StartToEnd -> {
                // A fast short fling must not bypass the user's distance threshold.
                if (enabled && !deleting && width > 0 && swipe.requireOffset() > width * .8f) {
                    deleting = true
                    delete()
                    true
                } else false
            }
            else -> false
        }
    }, positionalThreshold = { it * .8f })
    SwipeToDismissBox(state = swipe, enableDismissFromStartToEnd = enabled, enableDismissFromEndToStart = false,
        backgroundContent = {
            Surface(Modifier.fillMaxSize(), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.errorContainer) {
                Box(Modifier.padding(20.dp), contentAlignment = Alignment.CenterStart) { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.onErrorContainer) }
            }
        }, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).onSizeChanged { width = it.width },
    ) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF222326), modifier = Modifier.fillMaxWidth().then(pressFeedback(interaction)).clip(RoundedCornerShape(20.dp)).semantics {
            customActions = if (enabled) listOf(CustomAccessibilityAction("Delete $kind") { delete(); true }) else emptyList()
        }.clickable(enabled = enabled && !deleting, interactionSource = interaction, indication = androidx.compose.material3.ripple(), onClickLabel = "Edit $kind", onClick = edit)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 18.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.width(3.dp).height(28.dp).background(MaterialTheme.colorScheme.primary, RoundedCornerShape(2.dp)))
                Column(Modifier.padding(start = 12.dp).weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(title, style = MaterialTheme.typography.bodyLarge)
                    if (notes.isNotBlank()) Text(notes, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (amount != null) Text(amount, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
