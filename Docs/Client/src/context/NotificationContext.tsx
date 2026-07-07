import { createContext } from 'react';

export type NotificationType = 'success' | 'error' | 'warning' | 'info';

export interface NotificationOptions {
  type: NotificationType;
  message: string;
  duration?: number;
  showCloseButton?: boolean;
  onClose?: () => void;
  position?: string;
}

export interface Notification extends NotificationOptions {
  id: string;
}

export interface NotificationContextType {
  showNotification: (options: NotificationOptions) => void;
  hideNotification: (id: string, options?: { executeOnClose?: boolean }) => void;
}

export const NotificationContext = createContext<NotificationContextType | undefined>(undefined);
