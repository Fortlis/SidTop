package expo.modules.alarmconfirm.notification

import expo.modules.alarmconfirm.R
import android.content.Context

data class NotificationTheme(
    val channelId: String,
    val channelName: String,
    val title: String,
    val text: String,
    val smallIconRes: Int,
    val cancelActionIconRes: Int,
    val cancelActionLabel: String,
    val cancelReplyHint: String
)

fun interface NotificationThemeProvider {
    fun getTheme(context: Context): NotificationTheme
}

class DefaultNotificationThemeProvider : NotificationThemeProvider {
    override fun getTheme(context: Context): NotificationTheme = NotificationTheme(
        channelId = "alarm_channel",
        channelName = "Alarm notifications",
        title = "Reminder",
        text = "The session has started",
        smallIconRes = R.drawable.ic_stat_notify,
        cancelActionIconRes = android.R.drawable.ic_menu_close_clear_cancel,
        cancelActionLabel = "Cancel",
        cancelReplyHint = "Reason for cancelling"
    )
}
