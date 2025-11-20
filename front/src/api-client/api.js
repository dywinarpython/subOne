import { useEffect, useState } from "react";
import { Configuration as UserConfiguration } from "./user-service/configuration.ts";
import { DefaultApi as UserApi } from "./user-service/api.ts";
import { Configuration as SubscriptionConfiguration} from "./subscription-service/configuration.ts";
import { DefaultApi as SubscriptionApi} from "./subscription-service/api.ts"
import { Configuration as NotificationsConfiguration} from "./notifications-service/configuration.ts";
import { DefaultApi as NotificationsApi } from "./notifications-service/api.ts";


let userServiceApi = null;
let subscriptionApi = null;
let notificationsApi  = null;

export const useApis = (accessToken) => {
  const [apis, setApis] = useState({
    userApi: userServiceApi,
    subscriptionApi: subscriptionApi,
    notificationsApi: notificationsApi,
  });

  useEffect(() => {
    if (!accessToken) return;
    if (!userServiceApi) {
      userServiceApi = new UserApi(new UserConfiguration({ basePath: "http://localhost:8000", accessToken: () => accessToken || "" }));
    }
    if (!subscriptionApi) {
      subscriptionApi = new SubscriptionApi(new SubscriptionConfiguration({ basePath: "http://localhost:8001", accessToken: () => accessToken || "" }));
    }
    if (!notificationsApi) {
      notificationsApi = new NotificationsApi(new NotificationsConfiguration({ basePath: "http://localhost:8002", accessToken: () => accessToken || "" }));
    }
    userServiceApi.configuration.accessToken = () => accessToken || "";
    subscriptionApi.configuration.accessToken = () => accessToken || "";
    notificationsApi.configuration.accessToken = () => accessToken || "";

    setApis({
      userApi: userServiceApi,
      subscriptionApi: subscriptionApi,
      notificationsApi: notificationsApi,
    });
  }, [accessToken]);

  return apis;
};

export const getUserServiceUrl = () => userServiceApi.configuration.basePath;
export const getSubscriptionServiceUrl = () => subscriptionApi.configuration.basePath;
export const getNotificationsServiceUrl = () => notificationsApi.configuration.basePath;
