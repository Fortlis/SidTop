import { NativeModule, requireNativeModule } from 'expo-modules-core'

import {
  AlarmConfirmModuleEvents,
  AlarmSessionResponse,
  SetAlarmOptions,
} from './AlarmConfirm.types'

declare class AlarmConfirmModule extends NativeModule<AlarmConfirmModuleEvents> {
  setAlarm(options: SetAlarmOptions): Promise<AlarmSessionResponse>
  cancelAlarm(alarmId: string, cancelReason?: string): Promise<void>
  cancelAllAlarms(): Promise<void>
  clearSession(alarmId: string): Promise<void>
  clearFinishedSessions(): Promise<void>
}

export default requireNativeModule<AlarmConfirmModule>('AlarmConfirm')