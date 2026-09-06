package com.example.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat
import com.example.FocusAlarmApplication
import com.example.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    companion object {
        const val CHANNEL_ID = "focus_alarm_critical_channel"
        const val CHANNEL_NAME = "FocusAlarm Critical Alerts"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule all active alarms
            val app = context.applicationContext as? FocusAlarmApplication ?: return
            CoroutineScope(Dispatchers.IO).launch {
                val alarms = app.repository.getEnabledAlarmsList()
                for (alarm in alarms) {
                    AlarmScheduler.scheduleAlarm(context, alarm)
                }
            }
            return
        }

        val alarmId = intent.getLongExtra(AlarmScheduler.EXTRA_ALARM_ID, 0L)
        val alarmLabel = intent.getStringExtra(AlarmScheduler.EXTRA_ALARM_LABEL) ?: "Focus Alarm"
        val missionType = intent.getStringExtra(AlarmScheduler.EXTRA_MISSION_TYPE) ?: "MATH"
        val missionCount = intent.getIntExtra(AlarmScheduler.EXTRA_MISSION_COUNT, 3)

        // Trigger vibration
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 500, 200, 500), 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 500, 200, 500), 0)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Show Notification
        showAlarmNotification(context, alarmId, alarmLabel, missionType, missionCount)
    }

    private fun showAlarmNotification(
        context: Context,
        alarmId: Long,
        alarmLabel: String,
        missionType: String,
        missionCount: Int
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Critical alarms requiring wake-up mission completion"
                enableVibration(true)
                val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                setSound(
                    alarmSound,
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
            }
            notificationManager.createNotificationChannel(channel)
        }

        val fullScreenIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(AlarmScheduler.EXTRA_ALARM_ID, alarmId)
            putExtra(AlarmScheduler.EXTRA_ALARM_LABEL, alarmLabel)
            putExtra(AlarmScheduler.EXTRA_MISSION_TYPE, missionType)
            putExtra(AlarmScheduler.EXTRA_MISSION_COUNT, missionCount)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            context,
            alarmId.toInt(),
            fullScreenIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(alarmLabel)
            .setContentText("Wake-up Mission Required: Complete $missionType to dismiss")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setOngoing(true)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .build()

        notificationManager.notify(alarmId.toInt(), notification)
    }
}
