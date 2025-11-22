import { useState, useEffect} from "react";
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

  const [isExiting, setIsExiting] = useState(false);

  useEffect(() => {
    if (!autoClose) return;

    const timer = setTimeout(() => {
      setIsExiting(true);
    }, duration);

    return () => clearTimeout(timer);
  }, [autoClose, duration]);

  // Анимация выхода
  useEffect(() => {
    if (isExiting) {
      const exitTimer = setTimeout(() => onClose(id), 300); 
      return () => clearTimeout(exitTimer);
    }
  }, [isExiting, id, onClose]);

  const icons = {
    success: <CheckCircle size={20} />,
    error: <AlertCircle size={20} />,
    warning: <AlertTriangle size={20} />,
    info: <Info size={20} />,
  };

  const handleClick = () => {
    if (targetId && notificationTargetType) {
      onNavigate({ targetId, notificationTargetType });
      setIsExiting(true);
    }
  };

  return (
    <div
      className={`notification-item notification-${type} ${
        targetId && notificationTargetType ? "clickable" : ""
      } ${isExiting ? "exiting" : ""}`}
      onClick={handleClick}
      role="alert"
      aria-live="polite"
    >
      <div className="notification-icon">{icons[type]}</div>

      <div className="notification-content">
        <div className="notification-message">{message}</div>
        {targetId && notificationTargetType && (
          <div className="notification-action">Нажмите, чтобы перейти →</div>
        )}
      </div>

      <button
        className="notification-close"
        onClick={(e) => {
          e.stopPropagation();
          setIsExiting(true);
        }}
        aria-label="Закрыть уведомление"
      >
        <X size={16} />
      </button>

      {autoClose && !isExiting && (
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
    console.log("Переход к:", { targetId, notificationTargetType });
    // Здесь можно добавить реальную навигацию
    // navigate(`/${notificationTargetType}/${targetId}`);
  };

  useEffect(() => {
    const handleAddNotification = (e) => addNotification(e.detail);
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

  window.showNotification?.({
    type,
    message,
    targetId,
    notificationTargetType,
    autoClose: true,
    duration: 6000,
    ...options,
  });
};

const getNotificationText = (type) => {
  const texts = {
    DELETE_MEMBER: "Вы были удалены из группы",
    ADD_MEMBER: "Пользователь добавлен в команду",
    CHANGE_OWNER: "Владелец команды изменён",
    CREATE_USER: "Новый пользователь создан",
    PAYEMNT_SUBSCRIPTION: "Оплата подписки прошла успешно",
    ALREADY_PAYEMNT_SUBS: "Подписка уже активна",
  };
  return texts[type] || `Событие: ${type}`;
};