export type AlarmSessionState = 'SCHEDULED' | 'ACTIVE' | 'CANCELLED' | 'COMPLETED'

export interface AlarmSessionResponse {
  id: string
  hour: number
  minute: number
  faceDownMinutes: number
  label: string
  triggerAtMillis: number
  state: AlarmSessionState
  cancelReason: string | null
}

export interface SetAlarmOptions {
  hour: number
  minute: number
  faceDownMinutes: number
  label: string
}

export type SessionsChangedEvent = {
  sessions: AlarmSessionResponse[]
}


export type AlarmConfirmModuleEvents = {
  onSessionsChanged: (event: SessionsChangedEvent) => void
}