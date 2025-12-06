import { useState, useEffect, useCallback, useRef, useMemo } from "react";
import { Plus, TrendingUp, Users, Bell, ChevronRight, Check } from "lucide-react";
import { showError, showSuccess, getNotificationText } from "./Notification/NotificationSystem";
import LoadingAnimation from "../components/Loading/LoadingAnimation";
import { useApis } from "../api-client/api";
import { useAuth } from "../auth/AuthProvider";
import { useNotifications } from "../components/Notification/NotificationsContext";
import "../styles/MainPage.css";

const GROUPS_PAGE_SIZE = 10;
const NOTIFICATIONS_PAGE_SIZE = 10;
const SCROLL_THRESHOLD = 120;

export default function MainPage({ notificationsRedirectUrl = "/notifications" }) {
  const { user } = useAuth();
  const apis = useApis();
  const { userApi, notificationsApi, subscriptionApi } = apis || {};
  const { countNotification, setCount } = useNotifications();

  const [loading, setLoading] = useState(true);
  const [stats, setStats] = useState({ totalGroups: 0, totalSubscriptions: 0, monthlyExpense: 0 });

  const [groups, setGroups] = useState([]);
  const [groupsPage, setGroupsPage] = useState(0);
  const groupsHasMoreRef = useRef(true);
  const groupsLoadingRef = useRef(false);
  const groupsContainerRef = useRef(null);

  const [notifications, setNotifications] = useState([]);
  const notificationsLoadingRef = useRef(false);
  const [showNotificationsMoreButton, setShowNotificationsMoreButton] = useState(false);

  const [selectedNotifications, setSelectedNotifications] = useState(new Set());

  const loadStats = useCallback(async () => {
    if (!userApi || !subscriptionApi) return;
    
    try {
      const [groupCountRes, analyticRes] = await Promise.all([
        userApi.getGroupCount(),
        subscriptionApi.getTotalAnalyticGroups()
      ]);
      
      setStats({
        totalGroups: groupCountRes?.data ?? 0,
        totalSubscriptions: analyticRes?.data?.count ?? 0,
        monthlyExpense: analyticRes?.data?.totalSum ?? 0
      });
    } catch (error) {
      console.error("Error loading stats:", error);
    }
  }, [userApi, subscriptionApi]);

  const loadGroups = useCallback(async (page = 0, replace = false) => {
    if (!userApi || groupsLoadingRef.current) return;
    if (!groupsHasMoreRef.current && !replace) return;

    groupsLoadingRef.current = true;
    
    try {
      const resp = await userApi.getGroupsUserIsMember(page);
      const newGroups = resp?.data?.groups || [];

      if (replace) {
        setGroups(newGroups);
        groupsHasMoreRef.current = newGroups.length >= GROUPS_PAGE_SIZE;
        setGroupsPage(page);
      } else {
        setGroups(prev => {
          const existing = new Map(prev.map(g => [g.id, g]));
          newGroups.forEach(g => existing.set(g.id, g));
          return Array.from(existing.values());
        });
        groupsHasMoreRef.current = newGroups.length >= GROUPS_PAGE_SIZE;
        if (newGroups.length > 0) setGroupsPage(page);
      }
    } catch (err) {
      console.error("Error loading groups:", err);
      showError("Не удалось загрузить группы");
      groupsHasMoreRef.current = false;
    } finally {
      groupsLoadingRef.current = false;
    }
  }, [userApi]);

  const loadNotifications = useCallback(async (page = 0, replace = false, checkOnly = false) => {
    if (!notificationsApi || notificationsLoadingRef.current) return false;

    notificationsLoadingRef.current = true;
    
    try {
      const resp = await notificationsApi.getNewNotifications(page);
      const items = resp?.data?.notifications || [];
      const total = resp?.data?.count ?? resp?.data?.total ?? resp?.data?.totalCount;
      
      if (typeof setCount === "function" && total != null) {
        setCount(total);
      }

      if (checkOnly) {
        return items.length > 0;
      }

      if (replace) {
        setNotifications(items);
      } else {
        setNotifications(prev => {
          const existing = new Map(prev.map(n => [n.id, n]));
          items.forEach(n => existing.set(n.id, n));
          return Array.from(existing.values());
        });
      }
      
      return items.length >= NOTIFICATIONS_PAGE_SIZE;
    } catch (err) {
      console.error("Error loading notifications:", err);
      showError("Ошибка при загрузке уведомлений");
      return false;
    } finally {
      notificationsLoadingRef.current = false;
    }
  }, [notificationsApi, setCount]);

  const loadDashboard = useCallback(async () => {
    setLoading(true);
    groupsHasMoreRef.current = true;
    setShowNotificationsMoreButton(false);

    try {
      await Promise.all([
        loadStats(),
        loadGroups(0, true),
        loadNotifications(0, true).then(async () => {
          const hasMore = await loadNotifications(1, false, true);
          setShowNotificationsMoreButton(Boolean(hasMore));
        })
      ]);
    } catch (err) {
      console.error("Error loading dashboard:", err);
      showError("Ошибка загрузки данных");
    } finally {
      setLoading(false);
    }
  }, [loadStats, loadGroups, loadNotifications]);

  useEffect(() => {
    if (userApi && notificationsApi && subscriptionApi) {
      loadDashboard();
    }
  }, [userApi, notificationsApi, subscriptionApi, loadDashboard]);

  useEffect(() => {
    const el = groupsContainerRef.current;
    if (!el) return;

    let ticking = false;
    const onScroll = () => {
      if (groupsLoadingRef.current || !groupsHasMoreRef.current || ticking) return;
      
      ticking = true;
      requestAnimationFrame(() => {
        const { scrollTop, clientHeight, scrollHeight } = el;
        if (scrollTop + clientHeight >= scrollHeight - SCROLL_THRESHOLD) {
          loadGroups(groupsPage + 1);
        }
        ticking = false;
      });
    };

    el.addEventListener("scroll", onScroll, { passive: true });
    return () => el.removeEventListener("scroll", onScroll);
  }, [groupsPage, loadGroups]);

  const handleNavigateToNotifications = useCallback(() => {
    window.location.href = notificationsRedirectUrl;
  }, [notificationsRedirectUrl]);

  const toggleNotificationSelection = useCallback((notificationId) => {
    setSelectedNotifications(prev => {
      const updated = new Set(prev);
      if (updated.has(notificationId)) {
        updated.delete(notificationId);
      } else {
        updated.add(notificationId);
      }
      return updated;
    });
  }, []);

  const toggleSelectAll = useCallback(() => {
    setSelectedNotifications(prev => {
      if (prev.size === notifications.length) {
        return new Set();
      }
      return new Set(notifications.map(n => n.id));
    });
  }, [notifications]);

  const markSelectedAsRead = useCallback(async () => {
    if (!notificationsApi) return;
    
    const ids = Array.from(selectedNotifications);
    if (ids.length === 0) return;

    const previousNotifications = notifications;
    const previousCount = countNotification;

    setNotifications(prev => prev.filter(n => !selectedNotifications.has(n.id)));
    if (typeof setCount === "function") {
      setCount(prev => Math.max(0, (prev || 0) - ids.length));
    }
    setSelectedNotifications(new Set());

    try {
      await notificationsApi.updateNotifications({ ids });
      showSuccess(`Прочитано уведомлений: ${ids.length}`);
    } catch (err) {
      console.error("Error marking notifications:", err);
      showError("Не удалось отметить уведомления как прочитанные");
      
      setNotifications(previousNotifications);
      if (typeof setCount === "function") {
        setCount(previousCount);
      }
    }
  }, [notificationsApi, selectedNotifications, notifications, countNotification, setCount]);

  const formattedExpense = useMemo(() => {
    return stats.monthlyExpense.toFixed(2);
  }, [stats.monthlyExpense]);

  const allSelected = useMemo(() => {
    return notifications.length > 0 && selectedNotifications.size === notifications.length;
  }, [notifications.length, selectedNotifications.size]);

  if (loading) {
    return <LoadingAnimation message="Загрузка приложения" />;
  }

  return (
    <div className="main-page">
      <section className="hero-section">
        <div className="hero-content">
          <h1 className="hero-title">Привет, {user?.name || 'Пользователь'}! 👋</h1>
          <p className="hero-subtitle">Обзор подписок и активности</p>
        </div>
        <div className="quick-actions">
          <button 
            className="btn btn-primary" 
            onClick={() => showSuccess("Функция создания группы скоро будет добавлена")}
            aria-label="Создать новую группу"
          >
            <Plus size={20} aria-hidden="true" /> Создать группу
          </button>
          <button 
            className="btn btn-primary" 
            onClick={() => showSuccess("Функция добавления подписки скоро будет добавлена")}
            aria-label="Добавить новую подписку"
          >
            <Plus size={20} aria-hidden="true" /> Добавить подписку
          </button>
        </div>
      </section>

      <section className="stats-section">
        <div className="stats-grid">
          <div className="stat-card">
            <div className="stat-icon" aria-hidden="true">
              <Users size={24} />
            </div>
            <div className="stat-content">
              <div className="stat-value">{stats.totalGroups}</div>
              <div className="stat-label">Групп</div>
            </div>
          </div>
          <div className="stat-card">
            <div className="stat-icon" aria-hidden="true">
              <TrendingUp size={24} />
            </div>
            <div className="stat-content">
              <div className="stat-value">{stats.totalSubscriptions}</div>
              <div className="stat-label">Подписок</div>
            </div>
          </div>
          <div className="stat-card highlight">
            <div className="stat-icon" aria-hidden="true">
              <svg xmlns="http://www.w3.org/2000/svg" fill="#ffffff" width="24" height="24" viewBox="0 0 36 36">
                <path d="M20.57,20A8.23,8.23,0,0,0,29,12a8.23,8.23,0,0,0-8.43-8H12a1,1,0,0,0-1,1V18H9a1,1,0,0,0,0,2h2v2H9a1,1,0,0,0,0,2h2v7a1,1,0,0,0,2,0V24h9a1,1,0,0,0,0-2H13V20ZM13,6h7.57A6.24,6.24,0,0,1,27,12a6.23,6.23,0,0,1-6.43,6H13Z" />
              </svg>
            </div>
            <div className="stat-content">
              <div className="stat-value">{formattedExpense}</div>
              <div className="stat-label">Всего оплачено</div>
            </div>
          </div>
        </div>
      </section>

      <div className="main-content-grid">
        <section className="groups-section">
          <div className="section-header">
            <h2>Вы состоите в этих группах</h2>
            <button className="view-all-btn" aria-label="Показать все группы">
              Все группы <ChevronRight size={16} aria-hidden="true" />
            </button>
          </div>

          <div
            ref={groupsContainerRef}
            className="groups-list-scrollable"
            role="list"
            aria-label="Список групп"
          >
            {groups.length === 0 ? (
              <div className="empty-state">
                <Users size={48} className="empty-icon" aria-hidden="true" />
                <p>Вы не в одной группе</p>
                <button 
                  className="btn btn-primary" 
                  onClick={() => showSuccess("Функция добавления участника скоро будет добавлена")}
                >
                  Войти в группу
                </button>
              </div>
            ) : (
              groups.map(group => (
                <div 
                  key={group.id} 
                  className="group-card" 
                  onClick={() => console.log("Navigate to group:", group.id)}
                  role="listitem"
                  tabIndex={0}
                  onKeyPress={(e) => e.key === 'Enter' && console.log("Navigate to group:", group.id)}
                >
                  <div className="group-avatar" aria-hidden="true">
                    {group.name?.[0]?.toUpperCase() || "G"}
                  </div>
                  <div className="group-info">
                    <h3>{group.name}</h3>
                    <p className="group-date">
                      Создана {new Date(group.createdAt).toLocaleDateString('ru-RU')}
                    </p>
                  </div>
                  <ChevronRight size={20} className="group-arrow" aria-hidden="true" />
                </div>
              ))
            )}

            {groupsLoadingRef.current && (
              <div className="loading-more" aria-live="polite">Загрузка...</div>
            )}
            {!groupsHasMoreRef.current && groups.length > 0 && (
              <div className="end-of-list">Больше групп нет</div>
            )}
          </div>
        </section>

        <section className="notifications-section">
          <div className="section-header">
            <h2>
              Уведомления
              {countNotification > 0 && (
                <span className="notification-badge" aria-label={`${countNotification} новых уведомлений`}>
                  {countNotification}
                </span>
              )}
            </h2>
            <button 
              className="view-all-btn" 
              onClick={handleNavigateToNotifications}
              aria-label="Показать все уведомления"
            >
              Все <ChevronRight size={16} aria-hidden="true" />
            </button>
          </div>

          {notifications.length > 0 && (
            <div className="notifications-actions">
              <button className="action-btn" onClick={toggleSelectAll}>
                {allSelected ? 'Снять все' : 'Выбрать все'}
              </button>
              {selectedNotifications.size > 0 && (
                <button className="action-btn primary" onClick={markSelectedAsRead}>
                  <Check size={16} aria-hidden="true" /> Прочитать ({selectedNotifications.size})
                </button>
              )}
            </div>
          )}

          {notifications.length === 0 ? (
            <div className="empty-state">
              <Bell size={48} className="empty-icon" aria-hidden="true" />
              <p>Нет новых уведомлений</p>
            </div>
          ) : (
            <div className="notifications-list" role="list" aria-label="Список уведомлений">
              {notifications.map(notif => {
                const isSelected = selectedNotifications.has(notif.id);
                const notifText = getNotificationText(notif.notificationType);
                
                return (
                  <div 
                    key={notif.id} 
                    className={`notification-item ${isSelected ? 'selected' : ''}`}
                    role="listitem"
                  >
                    <div className="notification-checkbox" onClick={(e) => e.stopPropagation()}>
                      <input
                        id={`notif-${notif.id}`}
                        type="checkbox"
                        checked={isSelected}
                        onChange={() => toggleNotificationSelection(notif.id)}
                        aria-label={`Выбрать уведомление: ${notifText}`}
                      />
                    </div>

                    <div className="notification-body">
                      <div className="notification-content">
                        <div className="notification-message" title={notifText}>
                          {notifText}
                        </div>
                        <div className="notification-meta">
                          {new Date(notif.createdAt).toLocaleString('ru-RU', {
                            day: '2-digit',
                            month: '2-digit',
                            year: 'numeric',
                            hour: '2-digit',
                            minute: '2-digit'
                          })}
                        </div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>
          )}
          
          {showNotificationsMoreButton && (
            <div style={{ marginTop: 12 }}>
              <button className="btn btn-secondary" onClick={handleNavigateToNotifications}>
                Показать ещё → Управление уведомлениями
              </button>
            </div>
          )}
        </section>
      </div>
    </div>
  );
}