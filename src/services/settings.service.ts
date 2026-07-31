import { createMMKV } from 'react-native-mmkv'

export const storage = createMMKV()

const KEYS = {
    onboardingCompleted: 'onboarding_completed',
    defaultFaceDownMinutes: 'default_face_down_minutes'
} as const

export function hasCompletedOnboarding() {
    return storage.getBoolean(KEYS.onboardingCompleted) ?? false
}

export function markOnboardingComplete() {
    storage.set(KEYS.onboardingCompleted, true)
}

export function getDefaultFaceDownMinutes() {
    return storage.getNumber(KEYS.defaultFaceDownMinutes) ?? 5
}

export function setDefaultFaceDownMinutes(minutes: number) {
    storage.set(KEYS.defaultFaceDownMinutes, minutes)
}