export default function Spinner({ size = 44, text = "Cargando..." }) {
  return (
    <div style={{
      display: "flex", flexDirection: "column", alignItems: "center",
      justifyContent: "center", padding: "40px 20px", gap: 14,
    }}>
      <div style={{
        width: size, height: size,
        border: `4px solid var(--border, #e2e8f0)`,
        borderTopColor: "var(--rrhh, #2563eb)",
        borderRadius: "50%",
        animation: "spin-circle 0.75s linear infinite",
      }} />
      {text && (
        <span style={{ fontSize: 13, color: "var(--text-3, #94a3b8)" }}>{text}</span>
      )}
    </div>
  );
}
