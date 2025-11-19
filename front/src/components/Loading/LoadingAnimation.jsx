import "../../styles/LoadingAnimation.css"
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
    </div>
  );
}
