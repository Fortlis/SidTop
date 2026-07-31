import Button from '@/components/ui/Button/Button'
import { Modal, View } from 'react-native'
import { ModalContainerProps } from './modal-container.interface'

export default function ModalContainer({ isOpen, children, onPressSave, onPressCancel }: ModalContainerProps) {
    return (
        <Modal
            visible={isOpen}
            transparent
            animationType="fade"
            onRequestClose={onPressCancel}
        >
            <View className="flex-1 bg-page">
                <View className="flex-1">
                    {children}
                </View>
                <View className="flex-row mb-20 justify-center">
                    <Button
                        title="Cancel"
                        onPress={onPressCancel}
                        buttonClassName="rounded-tl-xl rounded-bl-xl px-6 py-3 border-2 border-border active:bg-primary"
                        textClassName="text-text-primary text-2xl font-bold"
                    />
                    <Button
                        title="Save"
                        onPress={onPressSave}
                        buttonClassName="rounded-tr-xl rounded-br-xl px-6 py-3 border-2 border-border active:bg-primary"
                        textClassName="text-text-primary text-2xl font-bold"
                    />
                </View>
            </View>
        </Modal>
    )
}
