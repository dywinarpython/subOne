import { useEffect, useState} from "react";
import { useAuth } from "./auth/AuthProvider";
import LoadingAnimation from "./components/Loading/LoadingAnimation"

export default function SubOneApp() {
    const {loading, accessToken} = useAuth();
    const [user, setUser] = useState(null);
    const [loadUser, setLoad] = useState(true);
    useEffect(() => {
    if (!accessToken) return;
    fetchUserData(accessToken)
      .then((user) => setUser(user))
      .finally(() => setLoad(false));
    }, [accessToken]);

    if (loading || loadUser) {
        return (
            <LoadingAnimation message="Загрузка приложения"/>
        );
    }
    return (
        <div className="app-container">
        <h1>Привет, {user.name} {user.surname}</h1>
        <h2>Ваша почта: {user.email}</h2>
        </div>
  );
}



async function fetchUserData(accessToken) {
  const response = await fetch("http://localhost:8000/api/v1/users/me", {
    method: "GET",
    headers: {
      "Authorization": `Bearer ${accessToken}`,
      "Content-Type": "application/json",
    },
  });

  if (!response.ok) {
    throw new Error(`Ошибка HTTP: ${response.status}`);
  }
  const user = await response.json();
  return user;
}