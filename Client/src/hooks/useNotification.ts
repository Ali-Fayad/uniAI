import { useContext } from 'react';
import { NotificationContext } from '../context/NotificationContext';
import type { NotificationContextType } from '../context/NotificationContext';

export const useNotification = (): NotificationContextType => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error('useNotification must be used within a NotificationProvider');
  }
  return context;
};
