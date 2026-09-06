package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AlarmEntity::class,
        TaskEntity::class,
        FocusSessionEntity::class,
        BreathSessionEntity::class,
        UserProfileEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun taskDao(): TaskDao
    abstract fun focusSessionDao(): FocusSessionDao
    abstract fun breathSessionDao(): BreathSessionDao
    abstract fun userProfileDao(): UserProfileDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "focus_alarm_database"
                )
                .fallbackToDestructiveMigration()
                .addCallback(DatabaseCallback(context.applicationContext))
                .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback(
        private val context: Context
    ) : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                populateInitialData(getInstance(context))
            }
        }

        private suspend fun populateInitialData(database: AppDatabase) {
            val alarmDao = database.alarmDao()
            val taskDao = database.taskDao()
            val userProfileDao = database.userProfileDao()
            val focusDao = database.focusSessionDao()

            // 1. Initial Alarms matching mockups
            alarmDao.insertAlarm(
                AlarmEntity(
                    hour = 5,
                    minute = 33,
                    label = "Work Day",
                    isEnabled = true,
                    repeatDays = "1,2,3,4,5", // Mon - Fri
                    missionType = "MATH",
                    missionDifficulty = "EASY",
                    missionCount = 3,
                    soundName = "Zen Forest (Progressive)",
                    isSnoozeEnabled = true,
                    snoozeMinutes = 3,
                    snoozeMaxTimes = 3
                )
            )
            alarmDao.insertAlarm(
                AlarmEntity(
                    hour = 7,
                    minute = 30,
                    label = "Morning Gym",
                    isEnabled = true,
                    repeatDays = "1,3,5", // Mon, Wed, Fri
                    missionType = "QR",
                    missionDifficulty = "MEDIUM",
                    missionCount = 1,
                    soundName = "Sunrise Radiance",
                    isSnoozeEnabled = true,
                    snoozeMinutes = 5,
                    snoozeMaxTimes = 2
                )
            )
            alarmDao.insertAlarm(
                AlarmEntity(
                    hour = 9,
                    minute = 0,
                    label = "Weekend Sleep",
                    isEnabled = false,
                    repeatDays = "0,6", // Sun, Sat
                    missionType = "SHAKE",
                    missionDifficulty = "EASY",
                    missionCount = 20,
                    soundName = "Gentle Flow",
                    isSnoozeEnabled = true,
                    snoozeMinutes = 10,
                    snoozeMaxTimes = 1
                )
            )

            // 2. Initial Tasks matching mockups
            taskDao.insertTask(
                TaskEntity(
                    title = "Define wake-up mission rules",
                    isCompleted = false,
                    dueDate = "Today",
                    priority = "HIGH",
                    tag = "Work",
                    estimatedSessions = 3,
                    completedSessions = 1
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    title = "Setup daily alarms",
                    isCompleted = false,
                    dueDate = "Today",
                    priority = "MEDIUM",
                    tag = "Work",
                    estimatedSessions = 2,
                    completedSessions = 0
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    title = "Design settings screen options",
                    isCompleted = false,
                    dueDate = "Tomorrow",
                    priority = "LOW",
                    tag = "Study",
                    estimatedSessions = 2,
                    completedSessions = 0
                )
            )
            taskDao.insertTask(
                TaskEntity(
                    title = "Review App Store guidelines",
                    isCompleted = true,
                    completedAt = System.currentTimeMillis() - 3600000,
                    dueDate = "Today",
                    priority = "MEDIUM",
                    tag = "Work",
                    estimatedSessions = 1,
                    completedSessions = 1
                )
            )

            // 3. Initial Profile
            userProfileDao.insertOrUpdateProfile(
                UserProfileEntity(
                    id = 1,
                    name = "Alex Chen",
                    email = "alex.chen@focusalarm.io",
                    isPro = true,
                    smartAlarmReminders = true,
                    focusDailySummary = false,
                    accentTheme = "VIOLET",
                    hapticFeedback = true,
                    streakDays = 14
                )
            )

            // 4. Initial Focus Sessions to populate chart
            val now = System.currentTimeMillis()
            val dayMs = 86400000L
            val days = listOf(4, 3, 2, 1, 0)
            val durations = listOf(45, 60, 90, 50, 25)
            for (i in days.indices) {
                focusDao.insertSession(
                    FocusSessionEntity(
                        taskTitle = "Deep Work Session",
                        durationMinutes = durations[i],
                        completedAt = now - days[i] * dayMs,
                        mode = "DEEP_WORK",
                        tag = if (i % 2 == 0) "Work" else "Study"
                    )
                )
            }
        }
    }
}
