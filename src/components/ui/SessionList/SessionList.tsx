import CancelSessionModal from "@/components/modals/CancelSession/CancelSessionModal";
import { cancelAlarm } from "@/services/alarm-actions.service";
import { useState } from "react";
import { FlatList, Text, View } from "react-native";
import Button from "../Button/Button";
import { SessionCardProps } from "./session-card.interface";
import { SessionListProps } from "./session-list.interface";

export default function SessionList({ sessions, cancelButton = false, state = false, cancelReason = false }: SessionListProps) {
    const [isOpen, setIsOpen] = useState(false)
    const [sessionId, setSessionId] = useState('')
    
    const handleRequestCancel = (id: string) => {
        setSessionId(id)
        setIsOpen(true)
    }

    return (
        <>
            <FlatList
                data={sessions}
                keyExtractor={(session) => session.id}
                renderItem={({ item }) =>
                    <SessionCard
                        session = {item}
                        cancelButton = {cancelButton}
                        state = {state}
                        cancelReason = {cancelReason}
                        onRequestCancel={() => handleRequestCancel(item.id)}
                    />
                }
                ListEmptyComponent={<Text className="text-text-primary text-xl ml-3">The list is empty</Text>}
            />

            <CancelSessionModal
                isOpen={isOpen}
                id={sessionId}
                onClose={() => setIsOpen(false)}
            />
        </>
    )
}

function SessionCard({ session, cancelButton, state, cancelReason, onRequestCancel }: SessionCardProps) {
    const handleCancel = () => {
        if (session.state === 'ACTIVE') {
            onRequestCancel()
        } else {
            cancelAlarm(session.id)
        }
    }

    return (
        <View className="w-[95%] mx-auto border-2 border-border p-5 rounded-3xl mb-4">
            <View className="flex-row justify-between items-center">
                <View>
                    <Text className="text-text-primary text-4xl font-bold">
                        {new Date(session.triggerAtMillis).toLocaleTimeString([], {
                            hour: '2-digit',
                            minute: '2-digit'
                        })}
                    </Text>
                    <Text className="text-text-secondary">
                        {new Date(session.triggerAtMillis).toLocaleDateString([], {
                            day: '2-digit',
                            month: 'long',
                            year: 'numeric'
                        })}
                    </Text>
                </View>
                <View className="items-end">
                    <Text className="text-text-primary font-medium text-2xl">
                        {session.label}
                    </Text>
                    
                    {cancelButton ? (
                        <Button
                            title="Cancel"
                            onPress={handleCancel}
                            buttonClassName="active:bg-primary px-1 rounded-xl"
                            textClassName="text-text-secondary text-xl"
                        />
                    ) : (
                        state && (
                            <Text className="text-text-secondary text-sm">
                                {session.state}
                            </Text>
                        )
                    )}
                </View>
            </View>

            {cancelReason && session.cancelReason && (
                <Text className="text-text-primary text-base mt-2">
                    {session.cancelReason}
                </Text>
            )}
        </View>
    )
}