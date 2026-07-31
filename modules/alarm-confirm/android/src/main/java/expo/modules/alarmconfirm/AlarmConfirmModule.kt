package expo.modules.alarmconfirm

import expo.modules.alarmconfirm.alarm.AlarmScheduler
import expo.modules.alarmconfirm.data.AlarmSessionRepository
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class SetAlarmOptions : Record {
    @Field val hour: Int = 0
    @Field val minute: Int = 0
    @Field val faceDownMinutes: Int = 0
    @Field val label: String = ""
}

class AlarmConfirmModule : Module() {
    private val context
        get() = appContext.reactContext
            ?: throw IllegalStateException("React context is not available")

    private val scheduler by lazy { AlarmScheduler(context) }
    private val repository by lazy { AlarmSessionRepository(context) }

    private val moduleScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var observingJob: Job? = null

    override fun definition() = ModuleDefinition {
        Name("AlarmConfirm")

        Events(EVENT_SESSIONS_CHANGED)

        AsyncFunction("setAlarm") { options: SetAlarmOptions ->
            runBlocking {
                scheduler.scheduleNew(options.hour, options.minute, options.faceDownMinutes, options.label).toMap()
            }
        }

        AsyncFunction("cancelAlarm") { alarmId: String, reason: String? ->
            runBlocking { scheduler.cancel(alarmId, reason) }
        }

        AsyncFunction("cancelAllAlarms") {
            runBlocking { scheduler.cancelAll() }
        }

        AsyncFunction("clearSession") { alarmId: String ->
            runBlocking { repository.remove(alarmId) }
        }

        AsyncFunction("clearFinishedSessions") {
            runBlocking { repository.removeFinished() }
        }

        OnStartObserving {
            observingJob?.cancel()
            observingJob = moduleScope.launch {
                repository.sessionsFlow.collect { sessions ->
                    sendEvent(EVENT_SESSIONS_CHANGED, mapOf("sessions" to sessions.map { it.toMap() }))
                }
            }
        }

        OnStopObserving {
            observingJob?.cancel()
            observingJob = null
        }

        OnDestroy {
            observingJob?.cancel()
            moduleScope.cancel()
        }
    }

    companion object {
        private const val EVENT_SESSIONS_CHANGED = "onSessionsChanged"
    }
}
