import tailwindConfig from '@/../tailwind.config';
import { setAlarm } from "@/services/alarm-actions.service";
import { getDefaultFaceDownMinutes } from "@/services/settings.service";
import WheelPicker, { withVirtualized } from "@quidone/react-native-wheel-picker";
import cn from 'clsx';
import { useEffect, useState } from "react";
import { Text, TextInput, View } from "react-native";
import resolveConfig from 'tailwindcss/resolveConfig';
import ModalContainer from "../Container/ModalContainer";
import { CreateSessionProps } from "./create-session.interface";

const { theme } = resolveConfig(tailwindConfig)
const colors = theme.colors as any

const hours = Array.from({ length: 24 }, (_, i) => ({ value: i, label: String(i).padStart(2, '0') }));
const minutes = Array.from({ length: 60 }, (_, i) => ({ value: i, label: String(i).padStart(2, '0') }));

const VirtualizedWheelPicker = withVirtualized(WheelPicker)

export default function CreateSessionModal({ isOpen, onClose }: CreateSessionProps) {
    const [hour, setHour] = useState(0)
    const [minute, setMinute] = useState(0)
    const [label, setLabel] = useState("")
    const [error, setError] = useState(false)

    useEffect(() => {
        if (isOpen) {
            setHour(0)
            setMinute(0)
            setLabel("")
            setError(false)
        }
    }, [isOpen])

    const handleSave = async () => {
        const trimmedLabel = label.trim()

        if (!trimmedLabel) {
            setError(true)
            return
        }

        await setAlarm({ hour, minute, label: trimmedLabel, faceDownMinutes: getDefaultFaceDownMinutes() })
        onClose()
    }

    const handleCancel = () => {
        onClose()
    }

    return (
        <ModalContainer isOpen={isOpen} onPressSave={handleSave} onPressCancel={handleCancel}>
            <View className="flex-row items-center justify-center gap-7 mt-10">
                <VirtualizedWheelPicker
                    data={hours}
                    value={hour}
                    onValueChanged={({ item }) => setHour(item.value)}
                    visibleItemCount={3}
                    itemTextStyle={{
                        color: colors['text-primary'],
                        fontSize: 70
                    }}
                    itemHeight={80}
                />
                <Text className="text-text-primary text-7xl mb-3">:</Text>
                <VirtualizedWheelPicker
                    data={minutes}
                    value={minute}
                    onValueChanged={({ item }) => setMinute(item.value)}
                    visibleItemCount={3}
                    itemTextStyle={{
                        color: colors['text-primary'],
                        fontSize: 70
                    }}
                    itemHeight={80}
                />
            </View>
            <TextInput
                placeholder="Enter a name for the session"
                value={label}
                onChangeText={setLabel}
                autoCorrect={false}
                className={cn('text-text-primary mx-10 border-b-2', error ? "border-error" : "border-border")}
            />
        </ModalContainer>
    )
}