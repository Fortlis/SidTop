import { ActivityIndicator, Text, View } from 'react-native'

export default function LoadingComponent() {
    return (
        <View className="flex-1 bg-page items-center justify-center px-8">
            <ActivityIndicator size="large" />
            <Text className="text-text-secondary text-base mt-4">
                Setting up the app...
            </Text>
        </View>
    )
}