export default function ApiAlert({ type = "info", message, status, style }) {
  if (!message) return null;

  const text = typeof message === "string"
    ? message
    : message?.message || "Ocurrio un error inesperado.";
  const httpStatus = status ?? (typeof message === "object" ? message?.status : null);

  const iconByType = {
    success: "✅",
    error: "⚠️",
    warning: "⚠️",
    info: "ℹ️",
  };

  return (
    <div className={`api-alert api-alert--${type}`} style={style}>
      <span className="api-alert__icon">{iconByType[type] || iconByType.info}</span>
      <span className="api-alert__content">
        {httpStatus != null ? (
          <span>
            <strong className="api-alert__status">{Number(httpStatus) === 0 ? "Fetch" : `HTTP ${httpStatus}`}</strong> - {text}
          </span>
        ) : (
          <span>{text}</span>
        )}
      </span>
    </div>
  );
}
