package expo.modules.alarmconfirm.data

import org.json.JSONObject

data class AlarmSession(
    val id: String,
    val requestCode: Int,
    val hour: Int,
    val minute: Int,
    val faceDownMinutes: Int,
    val label: String,
    val triggerAtMillis: Long,
    val state: AlarmSessionState,
    val cancelReason: String? = null
) {
    fun toJson(): JSONObject = JSONObject().apply {
        put(KEY_ID, id)
        put(KEY_REQUEST_CODE, requestCode)
        put(KEY_HOUR, hour)
        put(KEY_MINUTE, minute)
        put(KEY_FACE_DOWN_MINUTES, faceDownMinutes)
        put(KEY_LABEL, label)
        put(KEY_TRIGGER_AT, triggerAtMillis)
        put(KEY_STATE, state.name)
        put(KEY_CANCEL_REASON, cancelReason)
    }

    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "hour" to hour,
        "minute" to minute,
        "faceDownMinutes" to faceDownMinutes,
        "label" to label,
        "triggerAtMillis" to triggerAtMillis,
        "state" to state.name,
        "cancelReason" to cancelReason
    )

    companion object {
        private const val KEY_ID = "id"
        private const val KEY_REQUEST_CODE = "requestCode"
        private const val KEY_HOUR = "hour"
        private const val KEY_MINUTE = "minute"
        private const val KEY_FACE_DOWN_MINUTES = "faceDownMinutes"
        private const val KEY_LABEL = "label"
        private const val KEY_TRIGGER_AT = "triggerAtMillis"
        private const val KEY_STATE = "state"
        private const val KEY_CANCEL_REASON = "cancelReason"

        fun fromJson(json: JSONObject): AlarmSession = AlarmSession(
            id = json.getString(KEY_ID),
            requestCode = json.getInt(KEY_REQUEST_CODE),
            hour = json.getInt(KEY_HOUR),
            minute = json.getInt(KEY_MINUTE),
            faceDownMinutes = json.getInt(KEY_FACE_DOWN_MINUTES),
            label = json.optString(KEY_LABEL, ""),
            triggerAtMillis = json.getLong(KEY_TRIGGER_AT),
            state = AlarmSessionState.valueOf(json.getString(KEY_STATE)),
            cancelReason = json.optNullableString(KEY_CANCEL_REASON)
        )

        private fun JSONObject.optNullableString(key: String): String? =
            if (has(key) && !isNull(key)) getString(key) else null
    }
}
