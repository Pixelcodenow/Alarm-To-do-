package com.example.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.AlarmEntity
import java.util.Calendar

object AlarmScheduler {
    const val EXTRA_ALARM_ID = "extra_alarm_id"
    const val EXTRA_ALARM_LABEL = "extra_alarm_label"
    const val EXTRA_MISSION_TYPE = "extra_mission_type"
    const val EXTRA_MISSION_COUNT = "extra_mission_count"
    const val ACTION_ALARM_TRIGGER = "com.example.focusalarm.ACTION_ALARM_TRIGGER"

    fun scheduleAlarm(context: Context, alarm: AlarmEntity) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val pendingIntent = createPendingIntent(context, alarm)

        // Cancel previous schedule first
        alarmManager.cancel(pendingIntent)

        if (!alarm.isEnabled) return

        val triggerTimeMs = calculateNextTriggerTime(alarm.hour, alarm.minute, alarm.repeatDays)

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val showIntent = PendingIntent.getActivity(
                    context,
                    alarm.id.toInt(),
                    Intent(context, Class.forName("com.example.MainActivity")).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        putExtra(EXTRA_ALARM_ID, alarm.id)
                    },
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                val alarmClockInfo = AlarmManager.AlarmClockInfo(triggerTimeMs, showIntent)
                alarmManager.setAlarmClock(alarmClockInfo, pendingIntent)
            } else {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
            }
            Log.d("AlarmScheduler", "Scheduled alarm ${alarm.id} for $triggerTimeMs")
        } catch (e: SecurityException) {
            // If exact alarm permission not granted on Android 12+, fallback to inexact
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTimeMs, pendingIntent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun cancelAlarm(context: Context, alarmId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val dummyIntent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            dummyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun createPendingIntent(context: Context, alarm: AlarmEntity): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            action = ACTION_ALARM_TRIGGER
            putExtra(EXTRA_ALARM_ID, alarm.id)
            putExtra(EXTRA_ALARM_LABEL, alarm.label)
            putExtra(EXTRA_MISSION_TYPE, alarm.missionType)
            putExtra(EXTRA_MISSION_COUNT, alarm.missionCount)
        }
        return PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun calculateNextTriggerTime(hour: Int, minute: Int, repeatDays: String): Long {
        val now = Calendar.getInstance()
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val activeDays = parseDays(repeatDays)

        if (activeDays.isEmpty()) {
            // Once-off alarm: if past today, schedule for tomorrow
            if (calendar.before(now)) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        }

        // Check up to 7 days ahead for matching repeat day
        for (i in 0..7) {
            val candidateDayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) - 1 + 7) % 7 // 0=Sun, 1=Mon, ..., 6=Sat
            if (activeDays.contains(candidateDayOfWeek) && calendar.after(now)) {
                return calendar.timeInMillis
            }
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        return calendar.timeInMillis
    }

    fun getTimeRemaining(hour: Int, minute: Int, repeatDays: String): Pair<Int, Int> {
        val nextTime = calculateNextTriggerTime(hour, minute, repeatDays)
        val diffMs = nextTime - System.currentTimeMillis()
        if (diffMs <= 0) return Pair(0, 0)

        val totalMinutes = (diffMs / (1000 * 60)).toInt()
        val hours = totalMinutes / 60
        val mins = totalMinutes % 60
        return Pair(hours, mins)
    }

    fun parseDays(repeatDays: String): Set<Int> {
        if (repeatDays.isBlank()) return emptySet()
        return repeatDays.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .toSet()
    }
}
