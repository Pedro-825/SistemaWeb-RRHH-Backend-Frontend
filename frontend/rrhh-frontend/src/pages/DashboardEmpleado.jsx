import { useState, useEffect } from "react";
import { Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import { getUser, getIdEmpleado, getSolicitudesPorEmpleado } from "../services/api";

export default function DashboardEmpleado() {
  const user = getUser();
  const idEmpleado = getIdEmpleado();

  const [solicitudesPendientes, setSolicitudesPendientes] = useState(null);
  const [asistenciaHoy,         setAsistenciaHoy]         = useState(null);
  const [vacacionesDias,        setVacacionesDias]        = useState(null);
  const [hora, setHora] = useState(new Date());

  useEffect(() => {
    const t = setInterval(() => setHora(new Date()), 60000);
    return () => clearInterval(t);
  }, []);

  useEffect(() => {
    if (idEmpleado) cargarDatos();
  }, [idEmpleado]);

  const cargarDatos = async () => {
    try {
      const solicitudes = await getSolicitudesPorEmpleado(idEmpleado);
      if (Array.isArray(solicitudes)) {
        const pendientes = solicitudes.filter(
          s => s.estado === "PENDIENTE" || s.estado === "PROCESADO"
        ).length;
        setSolicitudesPendientes(pendientes);
      }
    } catch { /* silently fail */ }
  };

  const horaFmt = hora.toLocaleTimeString("es-PE", { hour: "2-digit", minute: "2-digit", hour12: true });
  const displayName = user?.username || "Usuario";

  return (
    <div className="dashboard">
      <Sidebar rol="EMPLEADO" />

      <main className="main-content">

        {/* ── TOP BAR ── */}
        <div className="emp-topbar">
          <div className="emp-topbar-brand">
            <div className="emp-topbar-plus">✚</div>
            <span className="emp-topbar-text">HOSPITAL SAN GABRIEL</span>
          </div>
          <div className="emp-topbar-right">
            <span className="emp-topbar-username">{displayName}</span>
            <span className="role-pill role-pill-empleado">Empleado</span>
            <div className="emp-topbar-avatar">👤</div>
          </div>
        </div>

        <h1 className="emp-welcome-title">Bienvenido</h1>

        {/* ── TARJETAS DE RESUMEN ── */}
        <div className="emp-stats-grid">

          {/* Mi Asistencia Hoy */}
          <div className="emp-stat-card emp-stat-teal">
            <div className="emp-stat-card-header">
              <div className="emp-stat-icon emp-stat-icon-teal">🕐</div>
              <h3>Mi Asistencia Hoy</h3>
            </div>
            <div className="emp-asist-body">
              <div className="emp-asist-col">
                <div className="emp-asist-sub">Hora de Entrada</div>
                <div className="emp-asist-time">{asistenciaHoy?.horaEntrada || horaFmt}</div>
                <span className="badge badge-success" style={{ fontSize: "11px", marginTop: "4px" }}>
                  A tiempo
                </span>
              </div>
              <div className="emp-asist-col">
                <div className="emp-asist-sub">Hora de Salida</div>
                <div className="emp-asist-time emp-asist-muted">—:—</div>
                <div className="emp-asist-remaining">
                  {asistenciaHoy?.restante || "Jornada en curso"}
                </div>
              </div>
            </div>
          </div>

          {/* Mis Solicitudes Pendientes */}
          <div className="emp-stat-card emp-stat-orange">
            <div className="emp-stat-card-header" style={{ position: "relative" }}>
              <div className="emp-stat-icon emp-stat-icon-orange">📋</div>
              <h3>Mis Solicitudes Pendientes</h3>
              {solicitudesPendientes !== null && solicitudesPendientes > 0 && (
                <span className="emp-count-badge emp-count-orange">{solicitudesPendientes}</span>
              )}
            </div>
            <div className="emp-stat-center-body">
              <div className="emp-stat-circle emp-circle-orange">📋</div>
              <span className="emp-stat-center-label">Solicitudes Pendientes</span>
            </div>
          </div>

          {/* Mis Vacaciones Disponibles */}
          <div className="emp-stat-card emp-stat-green">
            <div className="emp-stat-card-header" style={{ position: "relative" }}>
              <div className="emp-stat-icon emp-stat-icon-green">🌴</div>
              <h3>Mis Vacaciones Disponibles</h3>
              {vacacionesDias !== null && (
                <span className="emp-count-badge emp-count-green">{vacacionesDias}</span>
              )}
            </div>
            <div className="emp-stat-center-body">
              <div className="emp-stat-circle emp-circle-green">📋</div>
              <span className="emp-stat-center-label">Días Disponibles</span>
            </div>
          </div>
        </div>

        {/* ── ACCESOS DIRECTOS ── */}
        <div className="section-card">
          <h2>Accesos Directos</h2>
          <div className="emp-accesos-grid">

            <Link to="/empleado/asistencia" className="emp-acceso-item">
              <div className="emp-acceso-icon emp-acceso-blue">🕐</div>
              <span className="emp-acceso-label">Registrar Asistencia</span>
            </Link>

            <Link to="/empleado/solicitudes" className="emp-acceso-item">
              <div className="emp-acceso-icon emp-acceso-sand">📋</div>
              <span className="emp-acceso-label">Solicitar Permiso</span>
            </Link>

            <Link to="/empleado/nomina" className="emp-acceso-item">
              <div className="emp-acceso-icon emp-acceso-blue">💲</div>
              <span className="emp-acceso-label">Mi Nómina</span>
            </Link>

            <Link to="/empleado/solicitudes" className="emp-acceso-item" style={{ position: "relative" }}>
              <div className="emp-acceso-icon emp-acceso-sand" style={{ position: "relative" }}>
                🔔
                <span className="emp-notif-dot">1</span>
              </div>
              <span className="emp-acceso-label">Ver Notificaciones</span>
            </Link>

          </div>
        </div>

      </main>
    </div>
  );
}
