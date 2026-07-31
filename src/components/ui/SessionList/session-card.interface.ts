import { AlarmSessionResponse } from "../../../../modules/alarm-confirm"

export interface SessionCardProps {
    session: AlarmSessionResponse
    cancelButton: boolean
    state: boolean
    cancelReason: boolean
    onRequestCancel: () => void
}