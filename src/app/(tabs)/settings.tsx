import { getDefaultFaceDownMinutes, setDefaultFaceDownMinutes } from "@/services/settings.service";
import Slider from '@react-native-community/slider';
import { useState } from "react";
import { Text } from "react-native";
import { SafeAreaView } from "react-native-safe-area-context";
import resolveConfig from "tailwindcss/resolveConfig";
import tailwindConfig from "../../../tailwind.config";

const { theme } = resolveConfig(tailwindConfig)
const colors = theme.colors as any

export default function SettingsScreen() {
    const [minutes, setMinutes] = useState(() => getDefaultFaceDownMinutes())

    return (
        <SafeAreaView className="w-[95%] mx-auto mt-1">
            <Text className="text-text-primary text-xl ml-4">
                Required Facedown Time: {minutes}
            </Text>
            <Slider
                minimumValue={1}
                maximumValue={10}
                minimumTrackTintColor={colors['text-secondary']}
                maximumTrackTintColor={colors['text-secondary']}
                thumbTintColor={colors['text-primary']}
                value={minutes}
                onValueChange={(val) => setMinutes(val)}
                onSlidingComplete={(val) => setDefaultFaceDownMinutes(val)}
                step={1}
            />
        </SafeAreaView>
    )
}