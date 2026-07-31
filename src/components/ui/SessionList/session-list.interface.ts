import { AlarmSessionResponse } from "../../../../modules/alarm-confirm"

export interface SessionListProps {
    sessions: AlarmSessionResponse[]
    cancelButton?: boolean
    state?: boolean
    cancelReason?: boolean
}