import cn from 'clsx';
import { Tabs } from 'expo-router';
import { ComponentProps } from 'react';
import { Pressable, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import resolveConfig from 'tailwindcss/resolveConfig';
import tailwindConfig from '../../../tailwind.config';

type TabBarProps = Parameters<NonNullable<ComponentProps<typeof Tabs>['tabBar']>>[0];
const { theme } = resolveConfig(tailwindConfig)
const colors = theme.colors as any


export default function Navigation({ state, descriptors, navigation }: TabBarProps) {
    const insets = useSafeAreaInsets();
    
    return (
        <View
            style={{ bottom: insets.bottom + 12 }}
            className="absolute left-[24%] right-[24%] py-1 rounded-3xl flex-row items-center justify-center"
        >
            {state.routes.map((route, index) => {
                const { options } = descriptors[route.key];
                const isFocused = state.index === index;

                const onPress = () => {
                    const event = navigation.emit({
                        type: 'tabPress',
                        target: route.key,
                        canPreventDefault: true,
                    });

                    if (!isFocused && !event.defaultPrevented) {
                        navigation.navigate(route.name);
                    }
                };

                return (
                    <Pressable
                        key={route.key}
                        onPress={onPress}
                        accessibilityRole='button'
                        accessibilityLabel={options.title ?? route.name}
                        className={cn("items-center justify-center px-5 py-3 rounded-3xl active:bg-primary",
                            isFocused ? "bg-primary" : ''
                        )}
                    >
                        {options.tabBarIcon?.({ focused: isFocused, color: colors['text-primary'], size: 30 })}
                    </Pressable>
                );
            })}
        </View>
    );
}