package expo.modules.alarmconfirm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import expo.modules.alarmconfirm.data.AlarmSessionRepository
import expo.modules.alarmconfirm.data.AlarmSessionState
import expo.modules.alarmconfirm.service.AlarmSoundService
import expo.modules.alarmconfirm.util.AlarmLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return
        AlarmLog.d("alarm fired sessionId=$sessionId")
        val appContext = context.applicationContext
        val pendingResult = goAsync()
        val repository = AlarmSessionRepository(appContext)

        CoroutineScope(Dispatchers.IO).launch {
            val session = repository.getById(sessionId)
            if (session != null) {
                repository.updateState(sessionId, AlarmSessionState.ACTIVE)

                val serviceIntent = Intent(appContext, AlarmSoundService::class.java).apply {
                    action = AlarmSoundService.ACTION_START
                    putExtra(AlarmSoundService.EXTRA_SESSION_ID, session.id)
                    putExtra(AlarmSoundService.EXTRA_REQUEST_CODE, session.requestCode)
                    putExtra(AlarmSoundService.EXTRA_FACE_DOWN_MINUTES, session.faceDownMinutes)
                    putExtra(AlarmSoundService.EXTRA_LABEL, session.label)
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(serviceIntent)
                } else {
                    appContext.startService(serviceIntent)
                }
            } else {
                AlarmLog.w("alarm fired but session $sessionId missing from repository")
            }
            pendingResult.finish()
        }
    }

    companion object {
        const val EXTRA_SESSION_ID = "expo.modules.alarmconfirm.EXTRA_SESSION_ID"
    }
}
