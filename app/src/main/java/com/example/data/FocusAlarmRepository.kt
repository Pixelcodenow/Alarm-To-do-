package com.example.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class FocusAlarmRepository(private val database: AppDatabase) {
    private val alarmDao = database.alarmDao()
    private val taskDao = database.taskDao()
    private val focusDao = database.focusSessionDao()
    private val breathDao = database.breathSessionDao()
    private val userProfileDao = database.userProfileDao()

    // Alarms
    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarmsFlow()
    val enabledAlarms: Flow<List<AlarmEntity>> = alarmDao.getEnabledAlarmsFlow()

    suspend fun getAlarmById(id: Long): AlarmEntity? = alarmDao.getAlarmById(id)
    suspend fun insertAlarm(alarm: AlarmEntity): Long = alarmDao.insertAlarm(alarm)
    suspend fun updateAlarm(alarm: AlarmEntity) = alarmDao.updateAlarm(alarm)
    suspend fun deleteAlarmById(id: Long) = alarmDao.deleteAlarmById(id)
    suspend fun getEnabledAlarmsList(): List<AlarmEntity> = alarmDao.getEnabledAlarmsList()

    // Tasks
    val activeTasks: Flow<List<TaskEntity>> = taskDao.getActiveTasksFlow()
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getCompletedTasksFlow()
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasksFlow()

    suspend fun getTaskById(id: Long): TaskEntity? = taskDao.getTaskById(id)
    suspend fun insertTask(task: TaskEntity): Long = taskDao.insertTask(task)
    suspend fun updateTask(task: TaskEntity) = taskDao.updateTask(task)
    suspend fun setTaskCompleted(id: Long, completed: Boolean) {
        val completedAt = if (completed) System.currentTimeMillis() else null
        taskDao.updateTaskCompletion(id, completed, completedAt)
    }
    suspend fun deleteTaskById(id: Long) = taskDao.deleteTaskById(id)

    // Focus Sessions
    val allFocusSessions: Flow<List<FocusSessionEntity>> = focusDao.getAllSessionsFlow()
    suspend fun recordFocusSession(
        taskTitle: String?,
        durationMinutes: Int,
        mode: String,
        tag: String = "Work",
        taskId: Long? = null
    ): Long {
        if (taskId != null) {
            val task = taskDao.getTaskById(taskId)
            if (task != null) {
                taskDao.updateTask(task.copy(completedSessions = task.completedSessions + 1))
            }
        }
        return focusDao.insertSession(
            FocusSessionEntity(
                taskId = taskId,
                taskTitle = taskTitle,
                durationMinutes = durationMinutes,
                completedAt = System.currentTimeMillis(),
                mode = mode,
                tag = tag
            )
        )
    }
    suspend fun clearFocusHistory() = focusDao.clearAllFocusSessions()

    // Breath Sessions
    val allBreathSessions: Flow<List<BreathSessionEntity>> = breathDao.getAllBreathSessionsFlow()
    suspend fun recordBreathSession(
        patternName: String,
        durationSeconds: Int,
        completedCycles: Int,
        avgBreathSeconds: Int,
        soundName: String
    ): Long {
        return breathDao.insertBreathSession(
            BreathSessionEntity(
                patternName = patternName,
                durationSeconds = durationSeconds,
                completedCycles = completedCycles,
                avgBreathSeconds = avgBreathSeconds,
                soundName = soundName
            )
        )
    }

    // Profile & Settings
    val userProfile: Flow<UserProfileEntity?> = userProfileDao.getUserProfileFlow()
    suspend fun updateProfile(profile: UserProfileEntity) = userProfileDao.insertOrUpdateProfile(profile)
}
