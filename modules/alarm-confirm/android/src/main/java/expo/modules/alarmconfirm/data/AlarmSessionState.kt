package expo.modules.alarmconfirm.data

enum class AlarmSessionState {
    SCHEDULED,
    ACTIVE,
    CANCELLED,
    COMPLETED
}

val AlarmSessionState.isPending: Boolean
    get() = this == AlarmSessionState.SCHEDULED || this == AlarmSessionState.ACTIVE
