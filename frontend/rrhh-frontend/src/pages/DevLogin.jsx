/**
 * ╔══════════════════════════════════════════════════════════════╗
 * ║  ARCHIVO DE PRUEBA — ELIMINAR ANTES DE PRODUCCIÓN           ║
 * ║  Ruta: /dev                                                  ║
 * ║  Propósito: simular sesión + datos sin backend               ║
 * ╚══════════════════════════════════════════════════════════════╝
 */
import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { saveSession, clearSession } from "../services/api";

const USUARIOS_PRUEBA = [
  {
    rol: "RRHH",
    label: "Acceder como RRHH",
    icon: "👔",
    color: "#2563eb",
    bg: "#eff6ff",
    border: "#bfdbfe",
    username: "rrhh_test",
    token: "placeholder-token-rrhh",
    idEmpleado: null,
    descripcion: "Acceso completo: empleados, asistencia, nómina, solicitudes, reportes.",
    redirect: "/rrhh",
  },
  {
    rol: "EMPLEADO",
    label: "Acceder como Empleado",
    icon: "👤",
    color: "#0891b2",
    bg: "#ecfeff",
    border: "#a5f3fc",
    username: "empleado_test",
    token: "placeholder-token-empleado",
    idEmpleado: 1,
    descripcion: "Acceso: mi asistencia, mis solicitudes, mi nómina.",
    redirect: "/empleado",
  },
  {
    rol: "GERENCIA",
    label: "Acceder como Gerencia",
    icon: "🏛️",
    color: "#7c3aed",
    bg: "#f5f3ff",
    border: "#ddd6fe",
    username: "gerencia_test",
    token: "placeholder-token-gerencia",
    idEmpleado: null,
    descripcion: "Acceso: aprobación final de solicitudes de permisos.",
    redirect: "/gerencia",
  },
];

const cardStyle = (u) => ({
  background: u.bg,
  border: `2px solid ${u.border}`,
  borderRadius: "14px",
  padding: "24px",
  display: "flex",
  flexDirection: "column",
  gap: "10px",
  flex: "1 1 220px",
  maxWidth: "300px",
});

const btnStyle = (color) => ({
  marginTop: "8px",
  padding: "11px 20px",
  background: color,
  color: "#fff",
  border: "none",
  borderRadius: "8px",
  fontWeight: 700,
  fontSize: "14px",
  cursor: "pointer",
  fontFamily: "inherit",
  width: "100%",
});

export default function DevLogin() {
  const navigate = useNavigate();
  const [mockActivo, setMockActivo] = useState(
    sessionStorage.getItem('devMode') === 'true'
  );

  const handleAcceder = (u) => {
    sessionStorage.setItem('devMode', 'true');
    clearSession();
    saveSession(u.token, u.rol, u.username, u.idEmpleado);
    navigate(u.redirect, { replace: true });
  };

  const toggleMock = () => {
    if (mockActivo) {
      sessionStorage.removeItem('devMode');
      setMockActivo(false);
    } else {
      sessionStorage.setItem('devMode', 'true');
      setMockActivo(true);
      window.location.reload(); // recarga para que el interceptor se active
    }
  };

  return (
    <div style={{
      minHeight: "100vh",
      background: "#f8fafc",
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      padding: "32px 16px",
      fontFamily: "'Poppins', system-ui, sans-serif",
    }}>
      <div style={{ maxWidth: "860px", width: "100%" }}>

        {/* AVISO */}
        <div style={{
          background: "#fff7ed",
          border: "2px solid #fed7aa",
          borderRadius: "12px",
          padding: "16px 20px",
          marginBottom: "28px",
          display: "flex",
          gap: "12px",
          alignItems: "flex-start",
        }}>
          <span style={{ fontSize: "22px" }}>⚠️</span>
          <div>
            <strong style={{ color: "#9a3412", fontSize: "14px" }}>
              ARCHIVO DE PRUEBA — ELIMINAR ANTES DE PRODUCCIÓN
            </strong>
            <p style={{ margin: "4px 0 0", fontSize: "13px", color: "#92400e" }}>
              Esta página simula una sesión con tokens placeholder. No hay autenticación real.
              Elimina <code>DevLogin.jsx</code> y su ruta <code>/dev</code> en <code>App.jsx</code> antes de desplegar.
            </p>
          </div>
        </div>

        <h1 style={{ fontSize: "22px", fontWeight: 700, color: "#1e293b", marginBottom: "6px" }}>
          Usuarios de Prueba
        </h1>
        <p style={{ fontSize: "13px", color: "#64748b", marginBottom: "24px" }}>
          Selecciona un rol para ingresar directamente al panel correspondiente (sin 2FA ni backend).
        </p>

        {/* TARJETAS */}
        <div style={{ display: "flex", gap: "18px", flexWrap: "wrap", justifyContent: "center" }}>
          {USUARIOS_PRUEBA.map((u) => (
            <div key={u.rol} style={cardStyle(u)}>
              <div style={{ fontSize: "32px" }}>{u.icon}</div>
              <div>
                <div style={{ fontWeight: 700, fontSize: "15px", color: "#1e293b" }}>{u.rol}</div>
                <div style={{ fontSize: "12px", color: "#64748b", marginTop: "2px" }}>
                  Usuario: <code style={{ background: "#f1f5f9", padding: "1px 5px", borderRadius: "4px" }}>{u.username}</code>
                </div>
                <div style={{ fontSize: "12px", color: "#64748b", marginTop: "2px" }}>
                  ID Empleado: <code style={{ background: "#f1f5f9", padding: "1px 5px", borderRadius: "4px" }}>
                    {u.idEmpleado ?? "null"}
                  </code>
                </div>
              </div>
              <p style={{ margin: 0, fontSize: "12.5px", color: "#475569", lineHeight: 1.5 }}>
                {u.descripcion}
              </p>
              <button style={btnStyle(u.color)} onClick={() => handleAcceder(u)}>
                {u.label}
              </button>
            </div>
          ))}
        </div>

        {/* MOCK TOGGLE */}
        <div style={{
          marginTop: "24px",
          background: mockActivo ? "#f0fdf4" : "#fef9c3",
          border: `2px solid ${mockActivo ? "#86efac" : "#fde047"}`,
          borderRadius: "10px",
          padding: "16px 20px",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          gap: "16px",
          flexWrap: "wrap",
        }}>
          <div>
            <strong style={{ color: mockActivo ? "#15803d" : "#854d0e", fontSize: "13px" }}>
              {mockActivo ? "✅ Datos de prueba ACTIVOS" : "⚠️ Datos de prueba INACTIVOS"}
            </strong>
            <p style={{ margin: "3px 0 0", fontSize: "12px", color: mockActivo ? "#166534" : "#713f12" }}>
              {mockActivo
                ? "El interceptor mockea todas las llamadas al backend con datos falsos."
                : "Las páginas llamarán al backend real (localhost:8080). Los botones de acceso activan el mock."}
            </p>
          </div>
          <button onClick={toggleMock} style={{
            padding: "9px 18px",
            background: mockActivo ? "#dc2626" : "#16a34a",
            color: "#fff",
            border: "none",
            borderRadius: "8px",
            fontWeight: 700,
            fontSize: "13px",
            cursor: "pointer",
            fontFamily: "inherit",
            whiteSpace: "nowrap",
          }}>
            {mockActivo ? "Desactivar mock" : "Activar mock"}
          </button>
        </div>

        {/* INFO TOKENS */}
        <div style={{
          marginTop: "16px",
          background: "#f1f5f9",
          borderRadius: "10px",
          padding: "14px 18px",
          fontSize: "12px",
          color: "#64748b",
          lineHeight: 1.7,
        }}>
          <strong style={{ color: "#334155" }}>Tokens placeholder usados:</strong>
          <ul style={{ margin: "6px 0 0", paddingLeft: "18px" }}>
            <li>RRHH → <code>placeholder-token-rrhh</code></li>
            <li>EMPLEADO → <code>placeholder-token-empleado</code> (idEmpleado: 1)</li>
            <li>GERENCIA → <code>placeholder-token-gerencia</code></li>
          </ul>
        </div>

        <p style={{ textAlign: "center", marginTop: "16px", fontSize: "12px", color: "#94a3b8" }}>
          Para volver aquí: <code>/dev</code> · Para cerrar sesión: usa el botón dentro del panel.
        </p>
      </div>
    </div>
  );
}
