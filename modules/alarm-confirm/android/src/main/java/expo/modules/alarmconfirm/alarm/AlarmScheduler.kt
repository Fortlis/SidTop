package expo.modules.alarmconfirm.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import expo.modules.alarmconfirm.data.AlarmSession
import expo.modules.alarmconfirm.data.AlarmSessionRepository
import expo.modules.alarmconfirm.data.AlarmSessionState
import expo.modules.alarmconfirm.data.isPending
import expo.modules.alarmconfirm.service.AlarmSoundService
import expo.modules.alarmconfirm.util.AlarmLog
import java.util.Calendar
import java.util.UUID

class AlarmScheduler(private val context: Context) {
    private val repository = AlarmSessionRepository(context)
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    suspend fun scheduleNew(hour: Int, minute: Int, faceDownMinutes: Int, label: String): AlarmSession {
        val session = AlarmSession(
            id = UUID.randomUUID().toString(),
            requestCode = generateRequestCode(),
            hour = hour,
            minute = minute,
            faceDownMinutes = faceDownMinutes,
            label = label,
            triggerAtMillis = nextOccurrence(hour, minute),
            state = AlarmSessionState.SCHEDULED
        )
        repository.upsert(session)
        register(session)
        AlarmLog.d("scheduleNew id=${session.id} label=$label at=${session.triggerAtMillis} requestCode=${session.requestCode}")
        return session
    }

    suspend fun cancel(alarmId: String, reason: String? = null) {
        val session = repository.getById(alarmId)
        if (session == null) {
            AlarmLog.w("cancel: session $alarmId not found")
            return
        }
        unregister(session)
        if (session.state == AlarmSessionState.ACTIVE) {
            repository.updateState(alarmId, AlarmSessionState.CANCELLED, reason)
            stopRingingSession(alarmId)
        } else {
            repository.remove(alarmId)
        }
        AlarmLog.d("cancel id=$alarmId (was ${session.state})" + (reason?.let { " reason=\"$it\"" } ?: ""))
    }

    suspend fun cancelAll() {
        val pending = repository.getAll().filter { it.state.isPending }
        AlarmLog.d("cancelAll: ${pending.size} pending session(s)")
        pending.forEach { cancel(it.id) }
    }

    suspend fun rescheduleAllPending() {
        val now = System.currentTimeMillis()
        val pending = repository.getAll().filter { it.state.isPending }
        AlarmLog.d("rescheduleAllPending: ${pending.size} session(s) to restore")
        pending.forEach { session ->
            val triggerAtMillis = if (session.triggerAtMillis <= now) {
                now + BOOT_RESCHEDULE_DELAY_MS
            } else {
                session.triggerAtMillis
            }
            val restored = session.copy(triggerAtMillis = triggerAtMillis, state = AlarmSessionState.SCHEDULED)
            repository.upsert(restored)
            register(restored)
        }
    }

    private fun register(session: AlarmSession) {
        val pendingIntent = alarmPendingIntent(session)
        alarmManager.setAlarmClock(AlarmManager.AlarmClockInfo(session.triggerAtMillis, pendingIntent), pendingIntent)
    }

    private fun unregister(session: AlarmSession) {
        alarmManager.cancel(alarmPendingIntent(session))
    }

    private fun stopRingingSession(sessionId: String) {
        val intent = Intent(context, AlarmSoundService::class.java).apply {
            action = AlarmSoundService.ACTION_STOP
            putExtra(AlarmSoundService.EXTRA_SESSION_ID, sessionId)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    private fun alarmPendingIntent(session: AlarmSession): PendingIntent {
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_SESSION_ID, session.id)
        }
        return PendingIntent.getBroadcast(
            context,
            session.requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextOccurrence(hour: Int, minute: Int): Long {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_MONTH, 1)
        }
        return calendar.timeInMillis
    }

    private fun generateRequestCode(): Int = (System.nanoTime() and 0x7FFFFFFF).toInt()

    companion object {
        private const val BOOT_RESCHEDULE_DELAY_MS = 5_000L
    }
}
