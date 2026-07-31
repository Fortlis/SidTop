package expo.modules.alarmconfirm.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import expo.modules.alarmconfirm.util.AlarmLog
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.alarmSessionsDataStore: DataStore<Preferences> by preferencesDataStore(name = "alarm_sessions")

class AlarmSessionRepository(private val context: Context) {
    private val sessionsKey = stringPreferencesKey("sessions")

    val sessionsFlow: Flow<List<AlarmSession>> =
        context.alarmSessionsDataStore.data
            .catch { e ->
                if (e is IOException) {
                    AlarmLog.w("sessionsFlow: failed to read DataStore, using empty state", e)
                    emit(emptyPreferences())
                } else {
                    throw e
                }
            }
            .map { prefs -> decode(prefs[sessionsKey] ?: "[]") }
            .distinctUntilChanged()

    suspend fun getAll(): List<AlarmSession> = sessionsFlow.first()

    suspend fun getById(id: String): AlarmSession? = getAll().firstOrNull { it.id == id }

    suspend fun upsert(session: AlarmSession) {
        context.alarmSessionsDataStore.edit { prefs ->
            val sessions = decode(prefs[sessionsKey] ?: "[]").toMutableList()
            val index = sessions.indexOfFirst { it.id == session.id }
            if (index >= 0) sessions[index] = session else sessions.add(session)
            prefs[sessionsKey] = encode(sessions)
        }
        AlarmLog.d("upsert id=${session.id} state=${session.state} trigger=${session.triggerAtMillis}")
    }

    suspend fun updateState(id: String, state: AlarmSessionState, cancelReason: String? = null) {
        var found = false
        var previousState: AlarmSessionState? = null
        context.alarmSessionsDataStore.edit { prefs ->
            val sessions = decode(prefs[sessionsKey] ?: "[]").toMutableList()
            val index = sessions.indexOfFirst { it.id == id }
            if (index < 0) return@edit
            val current = sessions[index]
            previousState = current.state
            sessions[index] = current.copy(state = state, cancelReason = cancelReason ?: current.cancelReason)
            prefs[sessionsKey] = encode(sessions)
            found = true
        }
        if (!found) {
            AlarmLog.w("updateState: session $id not found")
        } else {
            AlarmLog.d("updateState id=$id $previousState -> $state" + (cancelReason?.let { " reason=\"$it\"" } ?: ""))
        }
    }

    suspend fun remove(id: String) {
        context.alarmSessionsDataStore.edit { prefs ->
            val sessions = decode(prefs[sessionsKey] ?: "[]").filterNot { it.id == id }
            prefs[sessionsKey] = encode(sessions)
        }
        AlarmLog.d("remove id=$id")
    }

    suspend fun removeFinished() {
        context.alarmSessionsDataStore.edit { prefs ->
            val sessions = decode(prefs[sessionsKey] ?: "[]").filter { it.state.isPending }
            prefs[sessionsKey] = encode(sessions)
        }
        AlarmLog.d("removeFinished")
    }

    private fun encode(sessions: List<AlarmSession>): String {
        val array = JSONArray()
        sessions.forEach { array.put(it.toJson()) }
        return array.toString()
    }

    private fun decode(raw: String): List<AlarmSession> {
        val array = JSONArray(raw)
        return (0 until array.length()).map { AlarmSession.fromJson(array.getJSONObject(it)) }
    }
}
