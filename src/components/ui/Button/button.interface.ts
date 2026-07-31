export interface ButtonProps {
    title: string;
    onPress: () => void | Promise<void>;
    buttonClassName?: string;
    textClassName?: string;
}