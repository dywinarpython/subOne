import { useState, useRef, useEffect } from "react";
import { Settings, LogOut, Bell} from "lucide-react";
import {useApis} from "../api-client/api"
import { useAuth } from "../auth/AuthProvider";
import "../App.css";
import "../styles/Header.css";

export default function Header() {
  const { user, logout, accessToken } = useAuth();
  const {notificationsApi} = useApis(accessToken);
  const [open, setOpen] = useState(false);
  const [scrolled, setScrolled] = useState(false);
  const ddRef = useRef(null);
  const [countNotification, setCountNotification] = useState(0);

  useEffect(() => {
    if (!notificationsApi) return;
    fetchCountNotification(notificationsApi)
      .then((response) => {
                setCountNotification(response.count);
            })
  }, [notificationsApi])

  useEffect(() => {
    const onDoc = (e) => {
      if (!ddRef.current) return;
      if (!ddRef.current.contains(e.target)) setOpen(false);
    };
    
    document.addEventListener("click", onDoc);
    
    const onScroll = () => setScrolled(window.scrollY > 20);
    window.addEventListener("scroll", onScroll);
    
    return () => {
      document.removeEventListener("click", onDoc);
      window.removeEventListener("scroll", onScroll);
    };
  }, []);

  const initials = user
    ? `${user.name?.[0] || ""}${user.surname?.[0] || ""}`.toUpperCase()
    : "U";

  return (
    <header className={`header-root ${scrolled ? "scrolled" : ""}`}>
      <div className="header-container">
        <div className="header-left">
          <div className="header-logo">
            <div className="logo-mark">
              <span className="logo-gradient">S</span>
            </div>
            <div className="logo-text-group">
              <span className="logo-text">SubOne</span>
            </div>
          </div>
        </div>

        <nav className="header-nav">
          <button className="nav-btn">Подписки</button>
          <button className="nav-btn">Аналитика</button>
        </nav>

        <div className="header-right">
          <button className="icon-btn notification-btn" aria-label="Уведомления">
            <Bell size={20} />
            {countNotification > 0 && (
              <span className="notification-dot">
                <span className="notification-count">{countNotification > 10 ? "9+" : countNotification}</span>
              </span>
            )}
          </button>


          <div className="header-user-wrap" ref={ddRef}>
            <button
              className="header-user-btn"
              onClick={() => setOpen(v => !v)}
              aria-haspopup="menu"
              aria-expanded={open}
            >
              <div className="user-avatar">
                <span>{initials}</span>
                <div className="avatar-ring"></div>
              </div>
              <div className="user-info">
                <div className="user-name">
                  {user ? `${user.name} ${user.surname}` : "Гость"}
                </div>
                <div className="user-email">
                  {user ? user.email : "Неавторизован"}
                </div>
              </div>
              <svg className={`caret ${open ? "open" : ""}`} width="12" height="8" viewBox="0 0 12 8">
                <path d="M1 1L6 6L11 1" stroke="currentColor" strokeWidth="2" fill="none" strokeLinecap="round"/>
              </svg>
            </button>

            <div className={`dropdown ${open ? "show" : ""}`}>
              <div className="dropdown-content">
                <button className="dropdown-item">
                  <Settings size={16} />
                  <span>Настройки</span>
                </button>
                <div className="dropdown-divider"></div>
                <button className="dropdown-item logout" onClick={() => {
                  setOpen(false);
                  if (logout) logout();
                }}>
                  <LogOut size={16} />
                  <span>Выйти</span>
                </button>
              </div>
            </div>
          </div>
        </div>
      </div>
    </header>
  );
}


async function fetchCountNotification(notificationsApi) {
  const response = await notificationsApi.getCountNotificationsNew();;
  return response.data;
}
