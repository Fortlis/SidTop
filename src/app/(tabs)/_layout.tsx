import Navigation from "@/components/navigation/Navigation";
import { Tabs } from "expo-router";
import { Clock4, RotateCcwClock, Settings } from 'lucide-react-native';

export default function TabLayout() {
    return (
        <Tabs
            tabBar={(props) => <Navigation {...props} />}
            screenOptions={{
                headerShown: false,
                sceneStyle: {
                    backgroundColor: '#000000'
                },
                tabBarStyle: {
                    position: 'absolute'
                }
            }}
        >
            <Tabs.Screen
                name='index'
                options={{
                    title: 'Home',
                    tabBarIcon: ({ color, size }) => <Clock4 size={size} color={color} />
                }}
            />
            <Tabs.Screen
                name='history'
                options={{
                    title: 'History',
                    tabBarIcon: ({ color, size }) => <RotateCcwClock size={size} color={color} />
                }}
            />
            <Tabs.Screen
                name='settings'
                options={{
                    title: 'Settings',
                    tabBarIcon: ({ color, size }) => <Settings size={size} color={color} />
                }}
            />
        </Tabs>
    )
}