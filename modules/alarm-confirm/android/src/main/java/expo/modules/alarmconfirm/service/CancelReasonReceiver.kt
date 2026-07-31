package expo.modules.alarmconfirm.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.RemoteInput
import expo.modules.alarmconfirm.data.AlarmSessionRepository
import expo.modules.alarmconfirm.data.AlarmSessionState
import expo.modules.alarmconfirm.notification.AlarmNotificationFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CancelReasonReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return
        val appContext = context.applicationContext
        val reason = RemoteInput.getResultsFromIntent(intent)
            ?.getCharSequence(AlarmNotificationFactory.REMOTE_INPUT_KEY)
            ?.toString()
            .orEmpty()

        val pendingResult = goAsync()
        val repository = AlarmSessionRepository(appContext)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.updateState(sessionId, AlarmSessionState.CANCELLED, reason)
            } finally {
                val stopIntent = Intent(appContext, AlarmSoundService::class.java).apply {
                    action = AlarmSoundService.ACTION_STOP
                    putExtra(AlarmSoundService.EXTRA_SESSION_ID, sessionId)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    appContext.startForegroundService(stopIntent)
                } else {
                    appContext.startService(stopIntent)
                }
                pendingResult.finish()
            }
        }
    }

    companion object {
        const val EXTRA_SESSION_ID = "expo.modules.alarmconfirm.EXTRA_CANCEL_SESSION_ID"
    }
}
