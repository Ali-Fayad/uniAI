import React, { useState, useCallback } from 'react';
import type { ReactNode } from 'react';
import { NotificationContext } from '../../../context/NotificationContext';
import type { Notification, NotificationOptions } from '../../../context/NotificationContext';
import NotificationContainer from './NotificationContainer';

export const NotificationProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [notifications, setNotifications] = useState<Notification[]>([]);

  const showNotification = useCallback((options: NotificationOptions) => {
    const id = Math.random().toString(36).substr(2, 9) + Date.now();
    const newNotification: Notification = {
      ...options,
      id,
      duration: options.duration !== undefined ? options.duration : 3000,
      showCloseButton: options.showCloseButton !== undefined ? options.showCloseButton : true,
      position: options.position || 'top-right',
    };
    
    setNotifications((prev) => [...prev, newNotification]);
  }, []);

  const hideNotification = useCallback((id: string, options: { executeOnClose?: boolean } = { executeOnClose: true }) => {
    setNotifications((prev) => {
      const notification = prev.find((n) => n.id === id);
      
      if (notification && notification.onClose && options.executeOnClose) {
        // Execute onClose asynchronously to avoid setState side-effects
        setTimeout(() => notification.onClose!(), 0);
      }
      
      return prev.filter((n) => n.id !== id);
    });
  }, []);

  // Filter based on position for future extensibility (currently only rendering top-right container)
  const topRightNotifications = notifications.filter(
    (n) => n.position === 'top-right' || !n.position
  );

  return (
    <NotificationContext.Provider value={{ showNotification, hideNotification }}>
      {children}
      {topRightNotifications.length > 0 && (
        <NotificationContainer position="top-right" notifications={topRightNotifications} />
      )}
    </NotificationContext.Provider>
  );
};
