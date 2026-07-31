package expo.modules.alarmconfirm.service

import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import androidx.core.app.NotificationManagerCompat
import expo.modules.alarmconfirm.data.AlarmSessionRepository
import expo.modules.alarmconfirm.data.AlarmSessionState
import expo.modules.alarmconfirm.notification.AlarmNotificationFactory
import expo.modules.alarmconfirm.sensor.FaceDownDetector
import expo.modules.alarmconfirm.util.AlarmLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmSoundService : Service() {
    private val ringingSessions = mutableMapOf<String, RingingSession>()
    private val handler = Handler(Looper.getMainLooper())

    private var faceDownDetector: FaceDownDetector? = null
    private var primarySessionId: String? = null

    private lateinit var repository: AlarmSessionRepository
    private lateinit var notificationFactory: AlarmNotificationFactory

    override fun onCreate() {
        super.onCreate()
        repository = AlarmSessionRepository(applicationContext)
        notificationFactory = AlarmNotificationFactory(applicationContext)
        notificationFactory.ensureChannel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
                if (sessionId != null && ringingSessions.containsKey(sessionId)) {
                    stopSession(sessionId, persistState = null)
                } else if (ringingSessions.isEmpty()) {
                    exitWithoutRinging()
                }
            }
            ACTION_START -> {
                val sessionId = intent.getStringExtra(EXTRA_SESSION_ID) ?: return START_NOT_STICKY
                val requestCode = intent.getIntExtra(EXTRA_REQUEST_CODE, sessionId.hashCode())
                val faceDownMinutes = intent.getIntExtra(EXTRA_FACE_DOWN_MINUTES, DEFAULT_FACE_DOWN_MINUTES)
                val label = intent.getStringExtra(EXTRA_LABEL) ?: ""
                startSession(sessionId, requestCode, faceDownMinutes, label)
            }
        }
        return START_NOT_STICKY
    }

    private fun exitWithoutRinging() {
        AlarmLog.w("service (re)started for a stop action with no matching ringing session, exiting")
        startForeground(IDLE_NOTIFICATION_ID, notificationFactory.buildIdle())
        stopForegroundCompat()
        stopSelf()
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        faceDownDetector?.stop()
        faceDownDetector = null
        ringingSessions.values.forEach { releasePlayer(it) }
        ringingSessions.clear()
        super.onDestroy()
    }

    private fun startSession(id: String, requestCode: Int, faceDownMinutes: Int, label: String) {
        if (ringingSessions.containsKey(id)) return

        val session = RingingSession(
            id = id,
            requestCode = requestCode,
            label = label,
            faceDownDurationMs = faceDownMinutes.coerceAtLeast(0) * 60_000L,
            mediaPlayer = createMediaPlayer(),
            onConfirmed = { stopSession(id, AlarmSessionState.COMPLETED) }
        )
        ringingSessions[id] = session
        session.mediaPlayer?.start()

        val notification = notificationFactory.build(id, requestCode, label)
        if (primarySessionId == null) {
            primarySessionId = id
            startForeground(notificationIdFor(id), notification)
        } else {
            NotificationManagerCompat.from(this).notify(notificationIdFor(id), notification)
        }

        if (faceDownDetector == null) {
            faceDownDetector = FaceDownDetector(
                context = this,
                onFaceDown = ::handleFaceDown,
                onFaceUp = ::handleFaceUp
            ).also { it.start() }
        }

        AlarmLog.d("startSession id=$id label=$label ringingCount=${ringingSessions.size} primary=$primarySessionId")
    }

    private fun stopSession(id: String, persistState: AlarmSessionState?) {
        val session = ringingSessions.remove(id) ?: return
        handler.removeCallbacks(session.confirmationRunnable)
        releasePlayer(session)

        val wasPrimary = id == primarySessionId
        if (!wasPrimary) {
            NotificationManagerCompat.from(this).cancel(notificationIdFor(id))
        }

        if (persistState != null) {
            CoroutineScope(Dispatchers.IO).launch { repository.updateState(id, persistState) }
        }

        if (wasPrimary) {
            promotePrimary()
        }

        AlarmLog.d("stopSession id=$id persistState=$persistState remainingRinging=${ringingSessions.size}")

        if (ringingSessions.isEmpty()) {
            faceDownDetector?.stop()
            faceDownDetector = null
            stopForegroundCompat()
            stopSelf()
        }
    }

    private fun promotePrimary() {
        val nextId = ringingSessions.keys.firstOrNull()
        primarySessionId = nextId
        AlarmLog.d("promotePrimary next=$nextId")
        val nextSession = nextId?.let { ringingSessions[it] } ?: return
        startForeground(notificationIdFor(nextId), notificationFactory.build(nextId, nextSession.requestCode, nextSession.label))
    }

    private fun handleFaceDown() {
        ringingSessions.values.forEach { session ->
            session.isWaitingForConfirmation = true
            session.mediaPlayer?.apply { if (isPlaying) pause() }
            handler.removeCallbacks(session.confirmationRunnable)
            handler.postDelayed(session.confirmationRunnable, session.faceDownDurationMs)
        }
    }

    private fun handleFaceUp() {
        ringingSessions.values.forEach { session ->
            if (!session.isWaitingForConfirmation) return@forEach
            session.isWaitingForConfirmation = false
            handler.removeCallbacks(session.confirmationRunnable)
            session.mediaPlayer?.apply { if (!isPlaying) start() }
        }
    }

    private fun createMediaPlayer(): MediaPlayer {
        val alarmUri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        return MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            setDataSource(this@AlarmSoundService, alarmUri)
            isLooping = true
            prepare()
        }
    }

    private fun releasePlayer(session: RingingSession) {
        session.mediaPlayer?.apply {
            if (isPlaying) stop()
            release()
        }
    }

    private fun stopForegroundCompat() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE)
        } else {
            @Suppress("DEPRECATION")
            stopForeground(true)
        }
    }

    private fun notificationIdFor(sessionId: String): Int = sessionId.hashCode() and 0x7FFFFFFF

    private class RingingSession(
        val id: String,
        val requestCode: Int,
        val label: String,
        val faceDownDurationMs: Long,
        var mediaPlayer: MediaPlayer?,
        onConfirmed: () -> Unit
    ) {
        var isWaitingForConfirmation = false
        val confirmationRunnable = Runnable { onConfirmed() }
    }

    companion object {
        const val ACTION_START = "expo.modules.alarmconfirm.ACTION_START"
        const val ACTION_STOP = "expo.modules.alarmconfirm.ACTION_STOP"

        const val EXTRA_SESSION_ID = "expo.modules.alarmconfirm.EXTRA_SESSION_ID"
        const val EXTRA_REQUEST_CODE = "expo.modules.alarmconfirm.EXTRA_REQUEST_CODE"
        const val EXTRA_FACE_DOWN_MINUTES = "expo.modules.alarmconfirm.EXTRA_FACE_DOWN_MINUTES"
        const val EXTRA_LABEL = "expo.modules.alarmconfirm.EXTRA_LABEL"

        private const val DEFAULT_FACE_DOWN_MINUTES = 5
        private const val IDLE_NOTIFICATION_ID = 1
    }
}
