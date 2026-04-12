import React from 'react';
import type { Notification } from '../../../context/NotificationContext';
import NotificationItem from './Notification';

interface NotificationContainerProps {
  notifications: Notification[];
  position: string;
}

const NotificationContainer: React.FC<NotificationContainerProps> = ({ notifications, position }) => {
  // Currently only supporting top-right, but positioned classes can be added later
  // p-4 provides safe area from viewport edges
  return (
    <div
      className={`fixed z-[9999] p-4 flex flex-col gap-3 pointer-events-none
        ${position === 'top-right' || !position ? 'top-0 right-0 items-end' : ''}
      `}
      aria-live="polite"
      role="status"
    >
      {notifications.map((notification) => (
        <NotificationItem key={notification.id} notification={notification} />
      ))}
    </div>
  );
};

export default NotificationContainer;
