import { db } from "@/db/client"
import { sessions as sessionsTable } from "@/db/schema"
import alarmConfirm, { AlarmSessionResponse } from "../../modules/alarm-confirm"

type FinishedSession = AlarmSessionResponse & {
  state: 'CANCELLED' | 'COMPLETED'
}

let sessions: AlarmSessionResponse[] = []
let subscription: { remove: () => void } | null = null
let persistQueue: Promise<void> = Promise.resolve()
const listeners = new Set<() => void>()

export function start() {
  if (subscription) return
  subscription = alarmConfirm.addListener('onSessionsChanged', (event) => {
    sessions = event.sessions
    notifyListeners()
    enqueuePersist(event.sessions)
  })
}

export function stop() {
  subscription?.remove()
  subscription = null
}

export function subscribe(listener: () => void) {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

export function getSnapshot(): AlarmSessionResponse[] {
  return sessions
}

function notifyListeners() {
  listeners.forEach((listener) => listener())
}

function enqueuePersist(snapshot: AlarmSessionResponse[]) {
  persistQueue = persistQueue.then(() => persistFinishedSessions(snapshot))
}

async function persistFinishedSessions(snapshot: AlarmSessionResponse[]) {
  const finished = snapshot.filter(isFinishedSession)
  if (finished.length === 0) return

  try {
    await db.insert(sessionsTable).values(finished).onConflictDoNothing()
    await alarmConfirm.clearFinishedSessions()
  } catch (err) {
    console.error('Failed to persist finished alarm sessions', err)
  }
}

function isFinishedSession(s: AlarmSessionResponse): s is FinishedSession {
  return s.state === 'COMPLETED' || s.state === 'CANCELLED'
}