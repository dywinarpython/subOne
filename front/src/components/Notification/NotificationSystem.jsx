import { useState, useEffect } from "react";
import { X, CheckCircle, AlertCircle, AlertTriangle, Info } from "lucide-react";
import "../../styles/Notification.css";

function NotificationItem({ notification, onClose, onNavigate }) {
  const {
    id,
    type = "info",
    message,
    targetId,
    notificationTargetType,
    autoClose = true,
    duration = 5000,
  } = notification;

  useEffect(() => {
    if (!autoClose) return;
    const timer = setTimeout(() => onClose(id), duration);
    return () => clearTimeout(timer);
  }, [autoClose, duration, id, onClose]);

  const icons = {
    success: <CheckCircle size={20} />,
    error: <AlertCircle size={20} />,
    warning: <AlertTriangle size={20} />,
    info: <Info size={20} />,
  };

  const handleClick = () => {
    if (targetId && notificationTargetType) {
      onNavigate({ targetId, notificationTargetType });
      onClose(id);
    }
  };

  return (
    <div
      className={`notification-item notification-${type} ${
        targetId && notificationTargetType ? "clickable" : ""
      }`}
      onClick={handleClick}
      role="alert"
      aria-live="polite"
    >
      <div className="notification-icon">{icons[type]}</div>

      <div className="notification-content">
        <div className="notification-message">{message}</div>
        {targetId && notificationTargetType && (
          <div className="notification-action">Нажмите, чтобы просмотреть</div>
        )}
      </div>

      <button
        className="notification-close"
        onClick={(e) => {
          e.stopPropagation();
          onClose(id);
        }}
        aria-label="Закрыть уведомление"
      >
        <X size={16} />
      </button>

      {autoClose && (
        <div className="notification-progress">
          <div
            className="notification-progress-bar"
            style={{ animationDuration: `${duration}ms` }}
          />
        </div>
      )}
    </div>
  );
}

export default function NotificationSystem({ position = "top-right" }) {
  const [notifications, setNotifications] = useState([]);

  const addNotification = (notification) => {
    const id = Date.now() + Math.random();
    setNotifications((prev) => [...prev, { ...notification, id }]);
  };

  const removeNotification = (id) => {
    setNotifications((prev) => prev.filter((n) => n.id !== id));
  };

  const handleNavigate = ({ targetId, notificationTargetType }) => {
    console.log("Навигация к ресурсу:", { targetId, notificationTargetType });
    // navigate(`/${notificationTargetType}/${targetId}`);
  };

  useEffect(() => {
    const handleAddNotification = (event) => addNotification(event.detail);
    window.addEventListener("addNotification", handleAddNotification);
    window.showNotification = addNotification;

    return () => {
      window.removeEventListener("addNotification", handleAddNotification);
      delete window.showNotification;
    };
  }, []);

  if (!notifications.length) return null;

  return (
    <div className={`notification-container notification-${position}`}>
      {notifications.map((notification) => (
        <NotificationItem
          key={notification.id}
          notification={notification}
          onClose={removeNotification}
          onNavigate={handleNavigate}
        />
      ))}
    </div>
  );
}

export const showSuccess = (message, options = {}) =>
  window.showNotification?.({ type: "success", message, ...options });

export const showError = (message, options = {}) =>
  window.showNotification?.({ type: "error", message, ...options });

export const showWarning = (message, options = {}) =>
  window.showNotification?.({ type: "warning", message, ...options });

export const showInfo = (message, options = {}) =>
  window.showNotification?.({ type: "info", message, ...options });

export const showNotificationWithTarget = ({
  notificationType,
  notificationTargetType,
  targetId,
  type = "info",
  ...options
}) => {
  const message = getNotificationText(notificationType);

  if (!targetId || !notificationTargetType) {
    showWarning(message, options);
  } else {
    window.showNotification?.({
      type,
      message,
      targetId,
      notificationTargetType,
      ...options,
    });
  }
};

const getNotificationText = (notificationType) => {
  switch (notificationType) {
    case "DELETE_MEMBER":
      return "Вы были удалены из группы";
    case "ADD_MEMBER":
      return "Пользователь был добавлен в команду";
    case "CHANGE_OWNER":
      return "Владелец команды изменён";
    case "CREATE_USER":
      return "Создан новый пользователь";
    case "PAYEMNT_SUBSCRIPTION":
      return "Оплата подписки прошла успешно";
    case "ALREADY_PAYEMNT_SUBS":
      return "Подписка уже оплачена";
    default:
      return `Получено уведомление: ${notificationType || "неизвестный тип"}`;
  }
};
