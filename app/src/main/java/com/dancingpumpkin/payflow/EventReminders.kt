package com.dancingpumpkin.payflow

import android.Manifest
import android.app.*
import android.content.*
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.dancingpumpkin.payflow.data.*
import com.dancingpumpkin.payflow.domain.reminderInstant
import kotlinx.coroutines.*
import java.time.*

object EventReminders {
    private const val CHANNEL = "event_reminders"
    private fun pending(context: Context, id: Long) = PendingIntent.getBroadcast(context, 0,
        Intent(context, EventReminderReceiver::class.java).setData(Uri.parse("payflow://reminder/$id")).putExtra("eventId", id), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
    fun cancel(context: Context, id: Long) {
        context.getSystemService(AlarmManager::class.java).cancel(pending(context, id))
        NotificationManagerCompat.from(context).cancel("event:$id", 0)
    }
    fun exactAllowed(context: Context) = Build.VERSION.SDK_INT < 31 || context.getSystemService(AlarmManager::class.java).canScheduleExactAlarms()
    fun schedule(context: Context, event: DayEventEntity) {
        cancel(context, event.id)
        val at = reminderInstant(event, ZoneId.systemDefault()) ?: return
        if (!at.isAfter(Instant.now())) return
        val alarms = context.getSystemService(AlarmManager::class.java)
        try {
            if (exactAllowed(context)) alarms.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toEpochMilli(), pending(context, event.id))
            else alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toEpochMilli(), pending(context, event.id))
        } catch (_: SecurityException) {
            alarms.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, at.toEpochMilli(), pending(context, event.id))
        }
    }
    fun notify(context: Context, event: DayEventEntity) {
        if (Build.VERSION.SDK_INT >= 33 && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) return
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel(CHANNEL, "Event reminders", NotificationManager.IMPORTANCE_HIGH))
        val open = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java).setData(Uri.parse("payflow://event/${event.id}"))
            .putExtra("openDate", event.date).putExtra("openEventId", event.id).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP), PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
        val notification = NotificationCompat.Builder(context, CHANNEL).setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(event.title).setContentText("${event.date} · ${event.time}")
            .setStyle(NotificationCompat.BigTextStyle().bigText("${event.date} · ${event.time}\n${event.notes}"))
            .setContentIntent(open).setAutoCancel(true).setCategory(NotificationCompat.CATEGORY_EVENT).build()
        NotificationManagerCompat.from(context).notify("event:${event.id}", 0, notification)
    }
}

class EventReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            val db = PayflowDatabase.create(context)
            try {
                if (intent.action in setOf(Intent.ACTION_BOOT_COMPLETED, Intent.ACTION_TIME_CHANGED, Intent.ACTION_TIMEZONE_CHANGED, AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED)) {
                    db.dao().allEvents().forEach { EventReminders.schedule(context, it) }
                } else {
                    val event = db.dao().eventById(intent.getLongExtra("eventId", -1))
                    val due = event?.let { reminderInstant(it, ZoneId.systemDefault()) }
                    if (event != null && due != null && !due.isAfter(Instant.now()) && due.isAfter(Instant.now().minusSeconds(86400))) EventReminders.notify(context, event)
                }
            } finally { db.close(); pending.finish() }
        }
    }
}
