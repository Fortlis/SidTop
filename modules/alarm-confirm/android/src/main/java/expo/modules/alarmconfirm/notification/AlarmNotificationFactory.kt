package expo.modules.alarmconfirm.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import expo.modules.alarmconfirm.service.CancelReasonReceiver

class AlarmNotificationFactory(
    private val context: Context,
    private val themeProvider: NotificationThemeProvider = DefaultNotificationThemeProvider()
) {
    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val theme = themeProvider.getTheme(context)
        val channel = NotificationChannel(theme.channelId, theme.channelName, NotificationManager.IMPORTANCE_HIGH)
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    fun build(sessionId: String, requestCode: Int, label: String = ""): Notification {
        val theme = themeProvider.getTheme(context)

        val remoteInput = RemoteInput.Builder(REMOTE_INPUT_KEY)
            .setLabel(theme.cancelReplyHint)
            .build()

        val cancelIntent = Intent(context, CancelReasonReceiver::class.java).apply {
            putExtra(CancelReasonReceiver.EXTRA_SESSION_ID, sessionId)
        }

        val cancelPendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val cancelAction = NotificationCompat.Action.Builder(
            theme.cancelActionIconRes,
            theme.cancelActionLabel,
            cancelPendingIntent
        ).addRemoteInput(remoteInput).build()

        return NotificationCompat.Builder(context, theme.channelId)
            .setContentTitle(label.takeIf { it.isNotBlank() } ?: theme.title)
            .setContentText(theme.text)
            .setSmallIcon(theme.smallIconRes)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setOngoing(true)
            .setAutoCancel(false)
            .addAction(cancelAction)
            .build()
    }

    fun buildIdle(): Notification {
        val theme = themeProvider.getTheme(context)
        return NotificationCompat.Builder(context, theme.channelId)
            .setContentTitle(theme.title)
            .setSmallIcon(theme.smallIconRes)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
    }

    companion object {
        const val REMOTE_INPUT_KEY = "expo.modules.alarmconfirm.CANCEL_REASON"
    }
}
