export interface ModalContainerProps {
    isOpen: boolean;
    children: React.ReactNode;
    onPressSave: () => void | Promise<void>
    onPressCancel: () => void | Promise<void>
}