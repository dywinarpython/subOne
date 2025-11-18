import React, { createContext, useContext, useEffect, useState } from "react";
import keycloak from "../keycloak";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
    const [loading, setLoading] = useState(true);
    const [accessToken, setAccessToken] = useState(null);

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
                setLoading(false);

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

    const logout = () => keycloak.logout();

    return (
        <AuthContext.Provider
            value={{
                loading,
                accessToken,
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
