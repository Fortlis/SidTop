import CreateSessionModal from "@/components/modals/CreateSession/CreateSessionModal";
import SessionList from "@/components/ui/SessionList/SessionList";
import { useUpcomingAlarms } from "@/hooks/useUpcomingAlarms";
import { Plus } from "lucide-react-native";
import { useState } from "react";
import { Pressable, Text, View } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import resolveConfig from "tailwindcss/resolveConfig";
import tailwindConfig from "../../../tailwind.config";

const { theme } = resolveConfig(tailwindConfig)
const colors = theme.colors as any

export default function HomeScreen() {
    const [isOpen, setIsOpen] = useState(false)
    const sessions = useUpcomingAlarms()

    return (
        <SafeAreaView className="flex-1 mt-1">
            <View className="flex-row justify-between items-center w-[90%] mx-auto mb-1.5">
                <Text className="text-text-primary text-3xl font-bold">
                    Sessions
                </Text>
                <Pressable onPress={() => setIsOpen(true)} className="active:bg-primary rounded-3xl">
                    <Plus color={colors["text-primary"]} size={32}/>
                </Pressable>
            </View>
            <CreateSessionModal isOpen={isOpen} onClose={() => setIsOpen(false)}/>
            <SessionList sessions={sessions} cancelButton={true} state={true}/>
        </SafeAreaView>
    )
}
