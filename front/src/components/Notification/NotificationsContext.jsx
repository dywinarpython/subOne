import { createContext, useContext, useState, useCallback, useEffect } from "react";
import { useApis } from "../../api-client/api";

const NotificationsContext = createContext(null);

export const NotificationsProvider = ({ children }) => {
  const apis = useApis();
  const { notificationsApi } = apis || {};

  const [countNotification, setCount] = useState(0);

  const refreshCount = useCallback(async () => {
    if (!notificationsApi) return;
    try {
      const res = await notificationsApi.getCountNotificationsNew();
      setCount(res.data?.count || 0);
    } catch (e) {
      console.error("refreshCount error:", e);
    }
  }, [notificationsApi]);

  const markAsRead = useCallback(async (ids = []) => {
    if (!notificationsApi || !ids.length) return;
    try {
      await notificationsApi.updateNotifications({ ids });
      setCount(prev => Math.max(0, prev - ids.length));
    } catch (e) {
      console.error("markAsRead error:", e);
      await refreshCount();
    }
  }, [notificationsApi, refreshCount]);

  useEffect(() => {
    refreshCount();
  }, [refreshCount]);

  return (
    <NotificationsContext.Provider value={{ countNotification, setCount, refreshCount, markAsRead }}>
      {children}
    </NotificationsContext.Provider>
  );
};

export const useNotifications = () => {
  const ctx = useContext(NotificationsContext);
  if (!ctx) throw new Error("useNotifications must be used inside NotificationsProvider");
  return ctx;
};
