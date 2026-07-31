import Button from "@/components/ui/Button/Button"
import { ensureNotificationPermission } from "@/services/alarm-actions.service"
import { markOnboardingComplete } from "@/services/settings.service"
import { router } from "expo-router"
import { Text } from "react-native"
import { SafeAreaView } from "react-native-safe-area-context"

export default function OnboardingScreen() {
    const handleContinue = async () => {
        await ensureNotificationPermission()
        markOnboardingComplete()
        router.replace('/')
    }

    return (
        <SafeAreaView className="flex-1 bg-page px-5 py-10 gap-2 items-start">
            <Text className="text-text-primary text-3xl font-medium">
                Enable Notifications
            </Text>
            <Text className="text-text-secondary text-xl">
                Notifications are the core feature of this app, and it simply won't work without them
            </Text>
            <Button
                title="Continue"
                onPress={handleContinue}
                textClassName="text-text-primary text-2xl"
                buttonClassName="border-border border-2 rounded-lg px-2 active:bg-primary"
            />
        </SafeAreaView>
    )
}