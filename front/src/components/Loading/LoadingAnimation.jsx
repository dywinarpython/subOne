import React from "react";

export default function LoadingAnimation({
  message = "Загрузка...",
  size = 110,      
  theme = "auto", 
}) {
  const style = { "--la-size": `${size}px` };

  return (
    <div
      className="la-root"
      data-la-theme={theme}
      style={style}
      role="status"
      aria-live="polite"
    >
      <div className="la-ring" aria-hidden="true">
        <div className="la-core" />
      </div>

      <div className="la-orbs" aria-hidden="true">
        <span className="la-orb" style={{ "--i": 0 }} />
        <span className="la-orb" style={{ "--i": 1 }} />
        <span className="la-orb" style={{ "--i": 2 }} />
      </div>

      <div className="la-message">{message}</div>

      <style>{`
        .la-root {
          --la-size: 110px;
          --radius-gap: 14%;
          --accent1: #00d4ff;
          --accent2: #6f00ff;
          --accent3: #ff5ea3;
          --text-light: #1f2937;
          --text-dark: #e6eefb;
          display: flex;
          flex-direction: column;
          align-items: center;
          justify-content: center;
          height: 100vh;
          gap: 18px;
          font-family: Inter, system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial;
          -webkit-font-smoothing: antialiased;
          color: var(--text-light);
          background: transparent;
        }

        /* color themes */
        .la-root[data-la-theme="dark"],
        @media (prefers-color-scheme: dark) {
          .la-root[data-la-theme="auto"] {
            --accent1: #56f0ff;
            --accent2: #9a6bff;
            --accent3: #ff79b0;
            --text-light: #e6eefb;
            --text-dark: #0b1220;
            color: var(--text-light);
          }
        }

        .la-root[data-la-theme="light"],
        .la-root[data-la-theme="auto"] {
          /* light defaults already set above */
        }

        .la-ring {
          width: var(--la-size);
          height: var(--la-size);
          border-radius: 50%;
          position: relative;
          display: grid;
          place-items: center;
          /* конусный градиент + размытие для glow-эффекта */
          background: conic-gradient(from 0deg,
            rgba(0,0,0,0) 0deg,
            rgba(0,0,0,0) 20deg,
            var(--accent1),
            var(--accent2),
            var(--accent3),
            var(--accent1)
          );
          filter: blur(10px);
          transform: rotate(0deg);
          animation: la-spin 2.2s linear infinite;
        }

        .la-ring .la-core {
          width: calc(var(--la-size) * 0.72);
          height: calc(var(--la-size) * 0.72);
          border-radius: 50%;
          background: linear-gradient(180deg, rgba(255,255,255,0.85), rgba(255,255,255,0.65));
          box-shadow: 0 6px 30px rgba(15,23,42,0.06) inset;
          backdrop-filter: blur(2px);
        }

        .la-orbs {
          display:flex;
          gap: 14px;
          align-items:center;
          justify-content:center;
          margin-top: 3px;
        }

        .la-orb {
          --size: 14px;
          width: var(--size);
          height: var(--size);
          border-radius: 50%;
          display: inline-block;
          background: radial-gradient(circle at 30% 30%, rgba(255,255,255,0.9), transparent 30%),
                      linear-gradient(135deg, var(--accent1), var(--accent2));
          box-shadow: 0 6px 20px rgba(99,102,241,0.18), 0 2px 6px rgba(0,0,0,0.12);
          transform: translateY(0) scale(1);
          animation: la-orb-bob 1.25s cubic-bezier(.2,.8,.2,1) infinite;
          animation-delay: calc(var(--i) * 0.18s);
        }

        @keyframes la-orb-bob {
          0%   { transform: translateY(0) scale(0.95); opacity: 0.95; }
          40%  { transform: translateY(-14px) scale(1.18); opacity: 1; }
          70%  { transform: translateY(-6px)  scale(1.04); }
          100% { transform: translateY(0)   scale(0.98); opacity: 0.95; }
        }

        @keyframes la-spin {
          to { transform: rotate(360deg); }
        }

        /* подпись (сообщение) */
        .la-message {
          font-size: 15px;
          letter-spacing: 0.1px;
          color: inherit;
          opacity: 0.95;
        }

        /* reduced motion accessibility */
        @media (prefers-reduced-motion: reduce) {
          .la-ring, .la-orb { animation: none; }
        }

        @media (max-width: 420px) {
          .la-root { gap: 12px; }
          .la-root { font-size: 14px; }
          .la-ring { width: calc(var(--la-size) * 0.8); height: calc(var(--la-size) * 0.8); filter: blur(8px); }
        }
      `}</style>
    </div>
  );
}
