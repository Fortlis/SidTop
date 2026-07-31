package expo.modules.alarmconfirm.util

import android.util.Log
import expo.modules.alarmconfirm.BuildConfig

object AlarmLog {
    private const val TAG = "AlarmConfirm"

    fun d(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, message)
        }
    }

    fun w(message: String, throwable: Throwable? = null) {
        if (BuildConfig.DEBUG) {
            if (throwable != null) Log.w(TAG, message, throwable)
            else Log.w(TAG, message)
        }
    }

    fun e(message: String, throwable: Throwable? = null) {
        if (throwable != null) Log.e(TAG, message, throwable)
        else Log.e(TAG, message)
    }
}