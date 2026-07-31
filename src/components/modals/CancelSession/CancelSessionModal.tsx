import { cancelAlarm } from "@/services/alarm-actions.service";
import cn from 'clsx';
import { useEffect, useState } from "react";
import { TextInput } from "react-native";
import ModalContainer from "../Container/ModalContainer";
import { CancelSessionProps } from "./cancel-session.interface";

export default function CancelSessionModal({ isOpen, id, onClose }: CancelSessionProps) {
    const [cancelReason, setCancelReason] = useState('')
    const [error, setError] = useState(false)
    
    useEffect(() => {
        if (isOpen) {
            setCancelReason('')
            setError(false)
        }
    }, [isOpen])

    const handleSave = () => {
        const trimmedReason = cancelReason.trim()

        if (!trimmedReason) {
            setError(true)
            return
        }

        cancelAlarm(id, trimmedReason)
            .then(() => onClose())
            .catch((err) => {
                console.error('Failed to cancel alarm', err)
        })
    }

    const handleCancel = () => {
        onClose()
    }

    return (
        <ModalContainer isOpen={isOpen} onPressCancel={handleCancel} onPressSave={handleSave}>
            <TextInput
                placeholder="Enter a reason for cancellation"
                value={cancelReason}
                onChangeText={setCancelReason}
                autoCorrect={false}
                className={cn("text-text-primary mt-20 mx-10 border-b-2", error ? "border-error" : "border-border")}
            />
        </ModalContainer>
    )
}