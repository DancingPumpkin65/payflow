package com.dancingpumpkin.payflow

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationManagerCompat
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun EventTimingFields(time: String?, minutes: Int?, setTime: (String?) -> Unit, setReminder: (Int?) -> Unit) {
    val context = LocalContext.current
    var picker by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(NotificationManagerCompat.from(context).areNotificationsEnabled()) }
    var exact by remember { mutableStateOf(EventReminders.exactAllowed(context)) }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { notifications = it }
    val settings = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { notifications = NotificationManagerCompat.from(context).areNotificationsEnabled(); exact = EventReminders.exactAllowed(context) }
    Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer) {
        Column(Modifier.fillMaxWidth().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.Schedule, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("All day", Modifier.padding(start = 16.dp).weight(1f))
                Switch(checked = time == null, modifier = Modifier.semantics { contentDescription = "All day event" }, onCheckedChange = { allDay -> setTime(if (allDay) null else LocalTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("HH:mm"))); if (allDay) setReminder(null) })
            }
            if (time != null) {
                TextButton(onClick = { picker = true }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                    Text("Time", Modifier.weight(1f), color = MaterialTheme.colorScheme.onSurfaceVariant); Text(time); RoundedArrow(ArrowDirection.RIGHT)
                }
                val options = linkedMapOf("None" to null, "At event time" to 0, "5 minutes before" to 5, "10 minutes before" to 10, "30 minutes before" to 30, "1 hour before" to 60, "1 day before" to 1440)
                ChoiceField("Reminder", options.entries.firstOrNull { it.value == minutes }?.key ?: "None", options.keys.toList(), { selected ->
                    setReminder(options[selected])
                    if (options[selected] != null && Build.VERSION.SDK_INT >= 33 && !notifications) permission.launch(Manifest.permission.POST_NOTIFICATIONS)
                })
                if (minutes != null && !notifications) TextButton(onClick = { settings.launch(Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)) }) { Text("Enable notifications in Settings") }
                if (minutes != null && !exact && Build.VERSION.SDK_INT >= 31) {
                    Text("Android may delay this reminder without precise alarm access.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    TextButton(onClick = { settings.launch(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM, Uri.parse("package:${context.packageName}"))) }) { Text("Allow precise reminders") }
                }
            }
        }
    }
    if (picker) {
        val initial = LocalTime.parse(time ?: "09:00")
        val state = rememberTimePickerState(initialHour = initial.hour, initialMinute = initial.minute, is24Hour = true)
        AlertDialog(onDismissRequest = { picker = false }, title = { Text("Event time") }, text = { TimeInput(state) }, confirmButton = { TextButton(onClick = { setTime(LocalTime.of(state.hour, state.minute).format(DateTimeFormatter.ofPattern("HH:mm"))); picker = false }) { Text("Done") } }, dismissButton = { TextButton(onClick = { picker = false }) { Text("Cancel") } })
    }
}
