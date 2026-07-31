package expo.modules.alarmconfirm.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import kotlin.math.sqrt

class FaceDownDetector(
    context: Context,
    private val onFaceDown: () -> Unit,
    private val onFaceUp: () -> Unit
) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager

    private val sensor: Sensor? =
        sensorManager?.getDefaultSensor(Sensor.TYPE_GRAVITY)
            ?: sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private val handler = Handler(Looper.getMainLooper())

    private var isCurrentlyFaceDown = false
    private var isPending = false

    private val confirmFaceDownRunnable = Runnable {
        isPending = false
        if (!isCurrentlyFaceDown) {
            isCurrentlyFaceDown = true
            onFaceDown()
        }
    }

    private val confirmFaceUpRunnable = Runnable {
        isPending = false
        if (isCurrentlyFaceDown) {
            isCurrentlyFaceDown = false
            onFaceUp()
        }
    }

    fun start() {
        sensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL) }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
        handler.removeCallbacks(confirmFaceDownRunnable)
        handler.removeCallbacks(confirmFaceUpRunnable)
        isPending = false
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val values = event?.values ?: return
        if (values.size < 3) return

        val x = values[0]
        val y = values[1]
        val z = values[2]

        val isFlat = sqrt(x * x + y * y) < XY_FLAT_THRESHOLD
        val isFacingDown = z < Z_THRESHOLD
        val rawFaceDown = isFlat && isFacingDown

        if (rawFaceDown) {
            when {
                isCurrentlyFaceDown && isPending -> {
                    handler.removeCallbacks(confirmFaceUpRunnable)
                    isPending = false
                }
                !isCurrentlyFaceDown && !isPending -> {
                    isPending = true
                    handler.postDelayed(confirmFaceDownRunnable, FACE_DOWN_CONFIRM_DELAY_MS)
                }
            }
        } else {
            when {
                !isCurrentlyFaceDown && isPending -> {
                    handler.removeCallbacks(confirmFaceDownRunnable)
                    isPending = false
                }
                isCurrentlyFaceDown && !isPending -> {
                    isPending = true
                    handler.postDelayed(confirmFaceUpRunnable, FACE_UP_CONFIRM_DELAY_MS)
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    companion object {
        private const val Z_THRESHOLD = -7.0f
        private const val XY_FLAT_THRESHOLD = 3.5f
        private const val FACE_DOWN_CONFIRM_DELAY_MS = 1000L
        private const val FACE_UP_CONFIRM_DELAY_MS = 500L
    }
}
