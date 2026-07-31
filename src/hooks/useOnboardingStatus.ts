import { hasCompletedOnboarding, storage } from "@/services/settings.service";
import { useSyncExternalStore } from "react";

function subscribe(callback: () => void) {
    const listener = storage.addOnValueChangedListener((changedKey) => {
        if (changedKey === 'onboarding_completed') callback()
    })

    return () => listener.remove()
}

export function useOnboardingStatus() {
    return useSyncExternalStore(subscribe, hasCompletedOnboarding)
}