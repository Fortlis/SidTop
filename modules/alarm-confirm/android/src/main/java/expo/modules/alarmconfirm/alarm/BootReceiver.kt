package expo.modules.alarmconfirm.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import expo.modules.alarmconfirm.util.AlarmLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
        AlarmLog.d("boot completed, rescheduling pending alarms")

        val pendingResult = goAsync()
        val scheduler = AlarmScheduler(context.applicationContext)

        CoroutineScope(Dispatchers.IO).launch {
            scheduler.rescheduleAllPending()
            pendingResult.finish()
        }
    }
}
