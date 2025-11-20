import { createContext, useContext, useEffect, useState } from "react";
import keycloak from "../keycloak";
import {useApis} from "../api-client/api"

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [loading, setLoading] = useState(true);
    const [accessToken, setAccessToken] = useState(null);
    const [user, setUser] = useState(null);
    const {userApi} = useApis(accessToken);

    useEffect(() => {
        keycloak
            .init({
                onLoad: "login-required",
                pkceMethod: "S256"
            })
            .then((auth) => {
                if (auth) {
                    setAccessToken(keycloak.token);
                }
                const timer = setInterval(() => {
                    keycloak
                        .updateToken(60)
                        .then((refreshed) => {
                            if (refreshed) {
                                setAccessToken(keycloak.accessToken);
                            }
                        })
                        .catch(() => {
                            keycloak.logout();
                        });
                }, 30000);
                return () => clearInterval(timer);
            });
    }, []);

    useEffect(() => {
        if (!accessToken) return;
        fetchUser(userApi)
            .then((userResponse) => {
                setUser(userResponse);
            })
            .catch((err) => {
                console.error("Ошибка получения пользователя", err);
            })
            .finally(() => {
                setLoading(false);
            });
    }, [accessToken, userApi]);

    const refreshUser = () => {
        if (!accessToken) return;
        fetchUser(userApi)
            .then((userResponse) => {
                setUser(userResponse);
            })
            .catch((err) => {
                console.error("Ошибка обновления пользователя", err);
            })
            .finally(() => {
                setLoading(false);
            });
    };

    const logout = () => keycloak.logout();

    return (
        <AuthContext.Provider
            value={{
                loading,
                accessToken,
                user, 
                refreshUser, 
                logout
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

export function useAuth() {
    return useContext(AuthContext);
}


async function fetchUser (userApi) {
    const response = await userApi.getUserById(); 
    return response.data;
};

