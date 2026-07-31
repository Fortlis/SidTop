import * as Notifications from 'expo-notifications';
import alarmConfirm, { AlarmSessionResponse, SetAlarmOptions } from "../../modules/alarm-confirm";

export async function setAlarm(options: SetAlarmOptions): Promise<AlarmSessionResponse> {
    return alarmConfirm.setAlarm(options)
}

export async function cancelAlarm(alarmId: string, cancelReason?: string): Promise<void> {
    alarmConfirm.cancelAlarm(alarmId, cancelReason)
}

export async function ensureNotificationPermission(): Promise<boolean> {
  const { status: existing } = await Notifications.getPermissionsAsync()
  if (existing === 'granted') return true

  const { status } = await Notifications.requestPermissionsAsync()
  return status === 'granted'
}