import { getSnapshot, subscribe } from '@/services/alarm-sync.service'
import { useMemo, useSyncExternalStore } from 'react'

export function useUpcomingAlarms() {
  const sessions = useSyncExternalStore(subscribe, getSnapshot)

  return useMemo(() => {
    return sessions
      .filter((s) => s.state === 'ACTIVE' || s.state === 'SCHEDULED')
      .sort((a, b) => {
        if (a.state !== b.state) return a.state === 'ACTIVE' ? -1 : 1
        return a.triggerAtMillis - b.triggerAtMillis
      })
  }, [sessions])
}