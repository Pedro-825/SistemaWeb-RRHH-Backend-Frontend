import { useState, useEffect, useCallback } from "react";
import { Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import { getUser, getIdEmpleado, getSolicitudesPorEmpleado, getMiAsistenciaHoy, getAsignacionesEmpleado, getMiSaldoVacaciones } from "../services/api";

const EMP_CACHE_TTL_MS = 60000;
const ESTADOS_SOLICITUD_PENDIENTE = new Set(["PENDIENTE", "OBSERVADO", "PROCESADO"]);
const ESTADOS_SOLICITUD_RESUELTA = new Set(["APROBADA", "RECHAZADA"]);

function normalizarEstadoSolicitud(estado) {
  return String(estado || "").toUpperCase();
}

function claseBadgeEntrada(asistenciaHoy, asistenciaCargada) {
  const estado = String(asistenciaHoy?.estado || "").toUpperCase();
  if (estado === "INASISTENCIA") return "badge-danger";
  if (!asistenciaHoy?.horaEntrada) return asistenciaCargada ? "badge-gray" : "badge-info";
  if (estado === "TARDANZA") return "badge-warning";
  if (estado === "JUSTIFICADA") return "badge-info";
  return "badge-success";
}

function claseBadgeSalida(asistenciaHoy) {
  if (asistenciaHoy?.horaSalida) return "badge-success";
  if (asistenciaHoy?.horaEntrada) return "badge-info";
  return "badge-gray";
}

function formatearEstadoAsistencia(estado) {
  const normalizado = String(estado || "").toUpperCase();
  const labels = {
    TARDANZA: "Tardanza",
    JUSTIFICADA: "Justificada",
    PUNTUAL: "Puntual",
    REGISTRADA: "Registrada",
    INASISTENCIA: "Inasistencia",
  };
  return labels[normalizado] || estado || "Registrada";
}

function leerCacheEmpleado(idEmpleado) {
  if (!idEmpleado) return null;
  try {
    const raw = sessionStorage.getItem(`empleado_dashboard_${idEmpleado}`);
    if (!raw) return null;
    const data = JSON.parse(raw);
    return data && Date.now() - data.ts < EMP_CACHE_TTL_MS ? data : null;
  } catch {
    return null;
  }
}

function guardarCacheEmpleado(idEmpleado, patch) {
  if (!idEmpleado) return;
  try {
    const previo = JSON.parse(sessionStorage.getItem(`empleado_dashboard_${idEmpleado}`) || "{}");
    sessionStorage.setItem(`empleado_dashboard_${idEmpleado}`, JSON.stringify({ ...previo, ...patch, ts: Date.now() }));
  } catch { /* silencioso */ }
}

export default function DashboardEmpleado() {
  const user = getUser();
  const idEmpleado = getIdEmpleado();

  const [solicitudesPendientes, setSolicitudesPendientes] = useState(null);
  const [notificaciones, setNotificaciones] = useState(0);
  const [asistenciaHoy, setAsistenciaHoy] = useState(null);
  const [horarioVigente, setHorarioVigente] = useState(null);
  const [asistenciaCargada, setAsistenciaCargada] = useState(false);
  const [saldoVacaciones, setSaldoVacaciones] = useState(null);
  const [saldoVacacionesError, setSaldoVacacionesError] = useState(false);

  const cargarSolicitudes = useCallback(async () => {
    if (!idEmpleado) {
      setSolicitudesPendientes(0);
      setNotificaciones(0);
      return;
    }
    try {
      const solicitudes = await getSolicitudesPorEmpleado(idEmpleado);
      if (Array.isArray(solicitudes)) {
        const pendientes = solicitudes.filter(
          s => ESTADOS_SOLICITUD_PENDIENTE.has(normalizarEstadoSolicitud(s.estado))
        ).length;
        setSolicitudesPendientes(pendientes);

        const notifKey = `notif_vistas_${idEmpleado}`;
        const vistas = JSON.parse(localStorage.getItem(notifKey) || "[]");
        const nuevas = solicitudes.filter(
          s => ESTADOS_SOLICITUD_RESUELTA.has(normalizarEstadoSolicitud(s.estado)) &&
               !vistas.includes(s.idSolicitud)
        ).length;
        setNotificaciones(nuevas);
        guardarCacheEmpleado(idEmpleado, { solicitudesPendientes: pendientes, notificaciones: nuevas });
      }
    } catch { /* silently fail */ }
  }, [idEmpleado]);

  const cargarAsistencia = useCallback(async () => {
    setAsistenciaCargada(false);
    try {
      const asistencia = await getMiAsistenciaHoy();
      setAsistenciaHoy(asistencia);
      guardarCacheEmpleado(idEmpleado, { asistenciaHoy: asistencia });
    } catch {
      setAsistenciaHoy(null);
    } finally {
      setAsistenciaCargada(true);
    }
  }, [idEmpleado]);

  const cargarHorario = useCallback(async () => {
    if (idEmpleado) {
      try {
        const asignaciones = await getAsignacionesEmpleado(idEmpleado);
        if (Array.isArray(asignaciones)) {
          const hoy = new Date().toISOString().slice(0, 10);
          const vigente = asignaciones.find(a =>
            a.activo &&
            a.fechaDesde <= hoy &&
            (a.fechaHasta == null || a.fechaHasta >= hoy)
          ) || asignaciones.find(a => a.activo);
          setHorarioVigente(vigente || null);
          guardarCacheEmpleado(idEmpleado, { horarioVigente: vigente || null });
        }
      } catch { /* silently fail */ }
    }
  }, [idEmpleado]);

  useEffect(() => {
    const cached = leerCacheEmpleado(idEmpleado);
    if (cached) {
      if ("asistenciaHoy" in cached) setAsistenciaHoy(cached.asistenciaHoy);
      if ("solicitudesPendientes" in cached) setSolicitudesPendientes(cached.solicitudesPendientes);
      if ("notificaciones" in cached) setNotificaciones(cached.notificaciones);
      if ("horarioVigente" in cached) setHorarioVigente(cached.horarioVigente);
      if ("saldoVacaciones" in cached) setSaldoVacaciones(cached.saldoVacaciones);
      setAsistenciaCargada(true);
    }
    cargarAsistencia();
    cargarSolicitudes();
    cargarHorario();
    getMiSaldoVacaciones()
      .then(data => {
        setSaldoVacaciones(data?.saldoVacaciones ?? null);
        guardarCacheEmpleado(idEmpleado, { saldoVacaciones: data?.saldoVacaciones ?? null });
      })
      .catch(() => setSaldoVacacionesError(true));
  }, [idEmpleado, cargarAsistencia, cargarSolicitudes, cargarHorario]);

  const displayName = user?.username || "Usuario";
  const horaEntrada = asistenciaHoy?.horaEntrada ? asistenciaHoy.horaEntrada.substring(0, 5) : "—:—";
  const horaSalida = asistenciaHoy?.horaSalida ? asistenciaHoy.horaSalida.substring(0, 5) : "—:—";
  const estadoEntrada = asistenciaHoy?.horaEntrada
    ? formatearEstadoAsistencia(asistenciaHoy.estado)
    : String(asistenciaHoy?.estado || "").toUpperCase() === "INASISTENCIA"
      ? "Inasistencia"
      : (asistenciaCargada ? "Pendiente" : "Cargando");
  const estadoSalida = asistenciaHoy?.horaSalida ? "Registrada" : (asistenciaHoy?.horaEntrada ? "Jornada en curso" : "Sin registro");
  const badgeEntrada = claseBadgeEntrada(asistenciaHoy, asistenciaCargada);
  const badgeSalida = claseBadgeSalida(asistenciaHoy);

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
            <Link to="/empleado/solicitudes" className="emp-topbar-notif" title="Ver estado de solicitudes" aria-label="Ver notificaciones">
              🔔
              {notificaciones > 0 && (
                <span className="emp-notif-dot">{notificaciones}</span>
              )}
            </Link>
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

            {/* Turno asignado */}
            {horarioVigente && (
              <div style={{
                display: "flex", alignItems: "center", gap: 8,
                background: "#f0fdf4", borderRadius: 8, padding: "6px 10px",
                marginBottom: 10, fontSize: 12, color: "#166534",
              }}>
                <span>🗓</span>
                <span>
                  <strong>{horarioVigente.nombreTurno}</strong>
                  {" — "}
                  {horarioVigente.horaEntrada?.slice(0, 5)} - {horarioVigente.horaSalida?.slice(0, 5)}
                </span>
              </div>
            )}

            <div className="emp-asist-body">
              <div className="emp-asist-col">
                <div className="emp-asist-sub">Hora de Entrada</div>
                <div className="emp-asist-time">{horaEntrada}</div>
                <span className={`badge ${badgeEntrada}`} style={{ fontSize: "11px", marginTop: "4px" }}>
                  {estadoEntrada}
                </span>
              </div>
              <div className="emp-asist-col">
                <div className="emp-asist-sub">Hora de Salida</div>
                <div className={`emp-asist-time emp-asist-salida-real ${asistenciaHoy?.horaSalida ? "" : "emp-asist-muted"}`}>{horaSalida}</div>
                <span className={`badge ${badgeSalida}`} style={{ fontSize: "11px", marginTop: "4px" }}>
                  {estadoSalida}
                </span>
              </div>
            </div>
          </div>

          {/* Mis Solicitudes Pendientes */}
          <div className="emp-stat-card emp-stat-orange">
            <div className="emp-stat-card-header" style={{ position: "relative" }}>
              <div className="emp-stat-icon emp-stat-icon-orange">📋</div>
              <h3>Mis Solicitudes Pendientes</h3>
              {solicitudesPendientes !== null && (
                <span className="emp-count-badge emp-count-orange">{solicitudesPendientes}</span>
              )}
            </div>
            <div className="emp-stat-center-body">
              <div className="emp-stat-circle emp-circle-orange">📋</div>
              <span className="emp-stat-center-label">
                {solicitudesPendientes === null ? "Cargando solicitudes" : "Solicitudes Pendientes"}
              </span>
            </div>
          </div>

          {/* Mis Vacaciones Disponibles */}
          <div className="emp-stat-card emp-stat-green">
            <div className="emp-stat-card-header" style={{ position: "relative" }}>
              <div className="emp-stat-icon emp-stat-icon-green">🌴</div>
              <h3>Mis Vacaciones Disponibles</h3>
            </div>
            <div className="emp-stat-center-body">
              <div className="emp-stat-circle emp-circle-green">{saldoVacaciones ?? "—"}</div>
              <span className="emp-stat-center-label">
                {saldoVacacionesError ? "No disponible" : saldoVacaciones === null ? "Cargando..." : "Días disponibles"}
              </span>
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

            <Link to="/empleado/asistencia" className="emp-acceso-item">
              <div className="emp-acceso-icon emp-acceso-green">✍️</div>
              <span className="emp-acceso-label">Justificaciones</span>
            </Link>

          </div>
        </div>

      </main>
    </div>
  );
}
