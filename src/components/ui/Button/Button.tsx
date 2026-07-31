import cn from 'clsx';
import { Pressable, Text } from "react-native";
import { ButtonProps } from "./button.interface";

export default function Button({ title, onPress, buttonClassName, textClassName }: ButtonProps) {
    return (
        <Pressable onPress={onPress} className={cn(buttonClassName)}>
            <Text className={cn(textClassName)}>
                {title}
            </Text>
        </Pressable>
    )
}