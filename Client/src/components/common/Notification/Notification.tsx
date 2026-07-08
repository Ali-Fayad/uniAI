import React, { useEffect, useState } from 'react';
import { FaCheckCircle, FaExclamationCircle, FaExclamationTriangle, FaInfoCircle, FaTimes } from 'react-icons/fa';
import type { Notification } from '../../../context/NotificationContext';
import { useNotification } from '../../../hooks/useNotification';

interface NotificationItemProps {
  notification: Notification;
}

const NotificationItem: React.FC<NotificationItemProps> = ({ notification }) => {
  const { hideNotification } = useNotification();
  const [isLeaving, setIsLeaving] = useState(false);

  // Auto-dismiss logic
  useEffect(() => {
    let timer: number;
    let removeTimer: number;
    
    if (notification.duration && notification.duration > 0 && notification.duration !== Infinity) {
      timer = window.setTimeout(() => {
        setIsLeaving(true);
        // Wait for animation, then actually remove
        removeTimer = window.setTimeout(() => hideNotification(notification.id), 300);
      }, notification.duration);
    }
    
    return () => {
      if (timer) window.clearTimeout(timer);
      if (removeTimer) window.clearTimeout(removeTimer);
    };
  }, [notification.duration, notification.id, hideNotification]);

  const handleClose = () => {
    setIsLeaving(true);
    window.setTimeout(() => hideNotification(notification.id), 300);
  };

  const getStyleClasses = (type: string) => {
    switch (type) {
      case 'success':
        return 'bg-green-100 text-green-800 dark:bg-green-900 border-green-500 dark:border-green-600 dark:text-green-50';
      case 'error':
        return 'bg-red-100 text-red-800 dark:bg-red-900 border-red-500 dark:border-red-600 dark:text-red-50';
      case 'warning':
        return 'bg-orange-100 text-orange-800 dark:bg-orange-900 border-orange-500 dark:border-orange-600 dark:text-orange-50';
      case 'info':
      default:
        return 'bg-blue-100 text-blue-800 dark:bg-blue-900 border-blue-500 dark:border-blue-600 dark:text-blue-50';
    }
  };

  const getIcon = (type: string) => {
    const iconClass = 'w-5 h-5 shrink-0 mt-0.5';
    switch (type) {
      case 'success':
        return <FaCheckCircle className={iconClass} />;
      case 'error':
        return <FaExclamationCircle className={iconClass} />;
      case 'warning':
        return <FaExclamationTriangle className={iconClass} />;
      case 'info':
      default:
        return <FaInfoCircle className={iconClass} />;
    }
  };

  const hasProgressBar = notification.duration && notification.duration > 0 && notification.duration !== Infinity;

  // Need to adjust opacity text and borders slightly based on type
  const getProgressBarColor = (type: string) => {
    switch (type) {
      case 'success': return 'bg-green-500';
      case 'error': return 'bg-red-500';
      case 'warning': return 'bg-orange-500';
      case 'info': return 'bg-blue-500';
      default: return 'bg-gray-500';
    }
  };

  return (
    <div
      className={`pointer-events-auto relative overflow-hidden flex flex-col justify-start max-w-sm w-full md:w-96
        border rounded-xl shadow-lg
        ${getStyleClasses(notification.type)}
        transition-all duration-300 transform
        ${isLeaving ? 'translate-x-full opacity-0' : 'animate-in slide-in-from-right-8 fade-in-0 duration-300'}
      `}
    >
      {/* Content wrapper */}
      <div className="flex items-start gap-3 p-4 select-text">
        {getIcon(notification.type)}
        
        {/* Message */}
        <div className="flex-1 text-sm font-medium leading-snug">
          {notification.message}
        </div>

        {/* Close Button */}
        {notification.showCloseButton !== false && (
          <button
            onClick={handleClose}
            aria-label="Close notification"
            className="shrink-0 rounded-full p-1 
              opacity-70 hover:opacity-100 hover:bg-black/10 dark:hover:bg-white/10
              transition-opacity focus:outline-none focus:ring-2 focus:ring-offset-1 focus:ring-black/20"
          >
            <FaTimes className="w-4 h-4" />
          </button>
        )}
      </div>

      {hasProgressBar && (
        <div className="h-1 w-full bg-black/10 dark:bg-white/10 relative mt-auto" aria-hidden="true">
          <div
            className={`h-full absolute left-0 top-0 bottom-0 ${getProgressBarColor(notification.type)}`}
            style={{
              animationName: 'shrinkWidth',
              animationDuration: `${notification.duration}ms`,
              animationTimingFunction: 'linear',
              animationFillMode: 'forwards'
            }}
          />
        </div>
      )}
    </div>
  );
};

export default NotificationItem;
