import { useEffect } from "react";
import { useAuth } from "./auth/AuthProvider";
import LoadingAnimation from "./components/Loading/LoadingAnimation";
import Header from "./components/Header";
import Footer from "./components/Footer";
import NotificationSystem, { showError, showNotificationWithTarget } from "./components/Notification/NotificationSystem";
import { NotificationsProvider } from "./components/Notification/NotificationsContext";
import MainPage from "./components/MainPage";
import {getNotificationsServiceUrl} from "./api-client/api"
import SockJS from "sockjs-client";
import { Client } from "@stomp/stompjs";

export default function SubOneApp() {
  const { loading, accessToken } = useAuth();

  useEffect(() => {
    if (!accessToken) return;

    const stompClient = new Client({
      webSocketFactory: () => new SockJS(getNotificationsServiceUrl() + '/ws/notifications'),
      connectHeaders: { Authorization: `Bearer ${accessToken}` },
      onConnect: () => {
        stompClient.subscribe('/user/queue/notifications', (message) => {
          try {
            const data = JSON.parse(message.body);
            console.log(data);  
            if (data.notificationType) {
              showNotificationWithTarget({
                notificationType: data.notificationType,
                notificationTargetType: "GROUP",
                targetId: 1,
                duration: 1000000
              });
            } else {
                console.error(data.message);
              }
            }
        catch (err) {
            console.error("Ошибка парсинга уведомления:", err);
          }
        });
      },
      onStompError: (frame) => {
        console.error("STOMP error:", frame);
        showError("Ошибка WebSocket соединения");
      },
    });
    stompClient.activate();
    return () => stompClient.deactivate();
  }, [accessToken]);

  if (loading) {
    return <LoadingAnimation message="Загрузка приложения" />;
  }

  return (
    <>
    <NotificationsProvider>
      <Header/>
      <MainPage/>
      <NotificationSystem/>
      <Footer />
      </NotificationsProvider>
    </>
  );
}
