import Button from '@/components/ui/Button/Button'
import * as Updates from 'expo-updates'
import { Text, View } from 'react-native'

export default function Error({ message }: { message?: string }) {
    const handleRetry = () => {
        Updates.reloadAsync().catch((err) => {
            console.error('Failed to reload app', err)
        })
    }

    return (
        <View className="flex-1 bg-page items-center justify-center px-8">
            <Text className="text-error text-2xl font-bold mb-2">
                Something went wrong
            </Text>
            <Text className="text-text-secondary text-base text-center mb-6">
                {message ?? 'Failed to set up the app database. Please try again.'}
            </Text>
            <Button
                title="Retry"
                onPress={handleRetry}
                buttonClassName="rounded-xl px-6 py-3 border-2 border-border active:bg-primary"
                textClassName="text-text-primary text-xl font-bold"
            />
        </View>
    )
}