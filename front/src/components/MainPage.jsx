import { useState, useEffect, useCallback } from "react";
import { Plus, TrendingUp, Users, Bell, ChevronRight } from "lucide-react";
import { showError, showSuccess, showNotificationWithTarget, showInfo } from "./Notification/NotificationSystem";
import LoadingAnimation from "../components/Loading/LoadingAnimation";
import { useApis } from "../api-client/api";
import { useAuth } from "../auth/AuthProvider";
import { useNotifications } from "../components/Notification/NotificationsContext";
import "../styles/MainPage.css";

export default function MainPage() {
  const [loading, setLoading] = useState(true);
  const [groups, setGroups] = useState([]);
  const [notifications, setNotifications] = useState([]);
  const { countNotification, setCount } = useNotifications();
  const [stats, setStats] = useState({
    totalGroups: 0,
    totalSubscriptions: 0,
    monthlyExpense: 0
  });

  const [processingIds, setProcessingIds] = useState(new Set());

  const { user } = useAuth();
  const apis = useApis();
  const { userApi, notificationsApi, subscriptionApi } = apis || {};

  const loadGroupsStatistics = useCallback(async (groupsList) => {
    if (!subscriptionApi) return;
    try {
      let totalSubscriptionsCount = 0;
      let totalMonthlyExpense = 0;

      for (const group of groupsList) {
        try {
          const subsResponse = await subscriptionApi.getSubscriptionsGroup(group.id, 0);
          const subscriptions = subsResponse.data?.subscriptions || [];
          totalSubscriptionsCount += subscriptions.length;

          const analyticResponse = await subscriptionApi.getAlreadyPaidGroup(group.id);
          const alreadyPaid = analyticResponse.data?.alreadyPaid || 0;
          totalMonthlyExpense += alreadyPaid;
        } catch (err) {
          console.error(`Ошибка загрузки данных для группы ${group.id}:`, err);
        }
      }

      setStats({
        totalGroups: groupsList.length,
        totalSubscriptions: totalSubscriptionsCount,
        monthlyExpense: totalMonthlyExpense
      });
    } catch (error) {
      console.error("Ошибка загрузки статистики:", error);
    }
  }, [subscriptionApi]);

  const loadGroups = useCallback(async () => {
    if (!userApi || !subscriptionApi) return;

    try {
      const ownerGroupsResponse = await userApi.getGroups();
      const ownerGroups = ownerGroupsResponse.data.groups || [];
      const memberGroupsResponse = await userApi.getGroupsUserIsMember(0);
      const memberGroups = memberGroupsResponse.data.groups || [];

      const allGroups = [...ownerGroups, ...memberGroups];
      const uniqueGroups = Array.from(new Map(allGroups.map(g => [g.id, g])).values());
      setGroups(uniqueGroups);

      await loadGroupsStatistics(uniqueGroups);
    } catch (error) {
      console.error("Ошибка загрузки групп:", error);
      showError("Не удалось загрузить группы");
    }
  }, [userApi, subscriptionApi, loadGroupsStatistics]);

  // ========================================
  // Загрузка уведомлений
  // ========================================
  const loadNotifications = useCallback(async () => {
    if (!notificationsApi) return;

    try {
      const response = await notificationsApi.getNewNotifications(0);
      const notifs = response.data?.notifications || [];
      const total =
        response.data?.count ??
        response.data?.total ??
        response.data?.totalCount ??
        notifs.length;

      setNotifications(notifs.slice(0, 5));
      // защита: если setCount не передан, проверяем
      if (typeof setCount === "function") {
        setCount(total);
      }
    } catch (error) {
      console.error("Ошибка загрузки уведомлений:", error);
    }
  }, [notificationsApi, setCount]);

  const loadDashboardData = useCallback(async () => {
    setLoading(true);
    try {
      await Promise.all([loadGroups(), loadNotifications()]);
    } catch (error) {
      console.error("Ошибка загрузки данных:", error);
      showError("Ошибка загрузки данных дашборда");
    } finally {
      setLoading(false);
    }
  }, [loadGroups, loadNotifications]);

  useEffect(() => {
    if (userApi && notificationsApi) {
      loadDashboardData();
    }
  }, [userApi, notificationsApi, loadDashboardData]);

  const handleNotificationClick = useCallback(async (notification) => {
    if (!notificationsApi || !notification) return;

    const id = notification.id;
    // если уже обрабатывается — игнорируем повторный клик
    if (processingIds.has(id)) return;

    // сохраняем текущее состояние для возможного отката
    const prevNotifications = notifications;
    const prevCount = typeof countNotification === "number" ? countNotification : (prevNotifications.length ?? 0);

    // оптимистично обновляем UI: убираем уведомление и уменьшаем счётчик
    setNotifications((prev) => prev.filter(n => n.id !== id));
    if (typeof setCount === "function") {
      setCount((prev) => Math.max(0, (typeof prev === "number" ? prev : prevCount) - 1));
    }

    // помечаем как обрабатываемое
    setProcessingIds((prev) => {
      const clone = new Set(prev);
      clone.add(id);
      return clone;
    });

    try {
      await notificationsApi.updateNotifications({ ids: [id] });
      // успешно — убираем id из processing
      setProcessingIds((prev) => {
        const clone = new Set(prev);
        clone.delete(id);
        return clone;
      });
      showInfo("Уведомление прочитано");
    } catch (error) {
      console.error("Ошибка обработки уведомления:", error);
      // откатим изменения в UI
      setNotifications(prev => {
        // если уведомление уже вернулось (например, было параллельно загружено) — не дублируем
        if (prev.some(n => n.id === id)) return prev;
        // восстанавливаем в начало списка
        return [notification, ...prev].slice(0, 5);
      });
      if (typeof setCount === "function") {
        setCount(prev => {
          // если prev число — увеличим, иначе вернём prevCount
          return (typeof prev === "number") ? prev + 1 : prevCount;
        });
      }
      setProcessingIds((prev) => {
        const clone = new Set(prev);
        clone.delete(id);
        return clone;
      });
      showError("Ошибка при обработке уведомления");
    }
  }, [notificationsApi, notifications, processingIds, setCount, countNotification]);

  const getNotificationMessage = (notification) => {
    const typeMessages = {
      'DELETE_MEMBER': 'Участник удален из группы',
      'ADD_MEMBER': 'Новый участник добавлен в группу',
      'CHANGE_OWNER': 'Изменен владелец группы',
      'CREATE_USER': 'Пользователь создан',
      'PAYEMNT_SUBSCRIPTION': 'Требуется оплата подписки',
      'ALREADY_PAYEMNT_SUBS': 'Подписка уже оплачена'
    };
    return typeMessages[notification.notificationType] || 'Новое уведомление';
  };

  const handleCreateGroup = () => showSuccess("Функция создания группы будет добавлена");
  const handleAddSubscription = () => showSuccess("Функция добавления подписки будет добавлена");
  const handleViewGroup = (groupId) => {
    console.log("Переход к группе:", groupId);
    showNotificationWithTarget({
      message: "Переход к группе",
      type: "info",
      targetId: groupId,
      resourceType: "group"
    });
  };

  if (loading) {
    return <LoadingAnimation message="Загрузка приложения" />;
  }

  return (
    <div className="main-page">
      <section className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">
            Привет, {user?.name || 'Пользователь'}! 👋
          </h1>
          <p className="hero-subtitle">
            Вот обзор ваших подписок и активности
          </p>
        </div>

        <div className="quick-actions">
          <button className="btn btn-primary" onClick={handleCreateGroup}>
            <Plus size={20} />
            Создать группу
          </button>
          <button className="btn btn-primary" onClick={handleAddSubscription}>
            <Plus size={20} />
            Добавить подписку
          </button>
        </div>
      </section>

      <section className="stats-section">
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon">
              <Users size={24} />
            </div>
            <div className="stat-content">
              <div className="stat-value">{stats.totalGroups}</div>
              <div className="stat-label">Групп</div>
            </div>
          </div>

          <div className="stat-card">
            <div className="stat-icon">
              <TrendingUp size={24} />
            </div>
            <div className="stat-content">
              <div className="stat-value">{stats.totalSubscriptions}</div>
              <div className="stat-label">Подписок</div>
            </div>
          </div>

          <div className="stat-card highlight">
            <svg width="80px" height="80px" viewBox="0 0 24 24" fill="none">
              <path d="M9 14H12" stroke="#1C274C" strokeWidth="1.5" strokeLinecap="round"/>
              <path d="M10 12V8.2C10 8.0142 10 7.9213 10.0123 7.84357C10.0801 7.41567 10.4157 7.08008 10.8436 7.01231C10.9213 7 11.0142 7 11.2 7H13.5C14.8807 7 16 8.11929 16 9.5C16 10.8807 14.8807 12 13.5 12H10ZM10 12V17M10 12H9" stroke="#1C274C" strokeWidth="1.5" strokeLinecap="round"/>
              <path d="M7 3.33782C8.47087 2.48697 10.1786 2 12 2C17.5228 2 22 6.47715 22 12C22 17.5228 17.5228 22 12 22C6.47715 22 2 17.5228 2 12C2 10.1786 2.48697 8.47087 3.33782 7" stroke="#1C274C" strokeWidth="1.5" strokeLinecap="round"/>
            </svg>
            <div className="stat-content">
              <div className="stat-value">{stats.monthlyExpense.toFixed(2)}</div>
              <div className="stat-label">Всего оплачено</div>
            </div>
          </div>
        </div>
      </section>

      <div className="main-content-grid">
        <section className="groups-section">
          <div className="section-header">
            <h2>Мои группы</h2>
            <button className="view-all-btn" onClick={() => console.log("Показать все группы")}>
              Все группы
              <ChevronRight size={16} />
            </button>
          </div>

          {groups.length === 0 ? (
            <div className="empty-state">
              <Users size={48} className="empty-icon" />
              <p>У вас пока нет групп</p>
              <button className="btn btn-primary" onClick={handleCreateGroup}>
                Создать первую группу
              </button>
            </div>
          ) : (
            <div className="groups-list">
              {groups.slice(0, 6).map(group => (
                <div
                  key={group.id}
                  className="group-card"
                  onClick={() => handleViewGroup(group.id)}
                >
                  <div className="group-avatar">
                    {group.name?.[0]?.toUpperCase() || 'G'}
                  </div>
                  <div className="group-info">
                    <h3>{group.name}</h3>
                    <p className="group-date">
                      Создана {new Date(group.createdAt).toLocaleDateString('ru-RU')}
                    </p>
                  </div>
                  <ChevronRight size={20} className="group-arrow" />
                </div>
              ))}
            </div>
          )}
        </section>

        <section className="notifications-section">
          <div className="section-header">
            <h2>
              Уведомления
              {countNotification > 0 && (
                <span className="notification-badge">{countNotification}</span>
              )}
            </h2>
            <button className="view-all-btn" onClick={() => console.log("Показать все уведомления")}>
              Все
              <ChevronRight size={16} />
            </button>
          </div>

          {notifications.length === 0 ? (
            <div className="empty-state">
              <Bell size={48} className="empty-icon" />
              <p>Нет новых уведомлений</p>
            </div>
          ) : (
            <div className="notifications-list">
              {notifications.map(notif => {
                const isProcessing = processingIds.has(notif.id);
                return (
                  <div
                    key={notif.id}
                    className={`notification-item ${isProcessing ? "is-processing" : ""}`}
                    onClick={() => handleNotificationClick(notif)}
                    style={{ opacity: isProcessing ? 0.6 : 1, pointerEvents: isProcessing ? "none" : "auto" }}
                  >
                    <div className="notification-icon">
                      <Bell size={18} />
                    </div>
                    <div className="notification-content">
                      <div className="notification-message">
                        {getNotificationMessage(notif)}
                      </div>
                      <div className="notification-time">
                        {new Date(notif.createdAt).toLocaleDateString('ru-RU')}
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </section>
      </div>
    </div>
  );
}
