import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import Sidebar from "../components/Sidebar";
import "../styles/Dashboard.css";
import { getSolicitudesPorEstado, listarEmpleados, getUser } from "../services/api";

const toCount = (data) => {
  if (Array.isArray(data)) return data.length;
  if (data?.totalElements != null) return data.totalElements;
  if (Array.isArray(data?.content)) return data.content.length;
  return 0;
};

export default function DashboardGerencia() {
  const user = getUser();
  const displayName = user?.username || "gerencia";
  const [notificaciones,   setNotificaciones]   = useState(0);
  const [totalEmpleados,   setTotalEmpleados]   = useState("—");
  const [porAprobar,       setPorAprobar]       = useState("—");
  const [aprobadas,        setAprobadas]        = useState("—");
  const [rechazadas,       setRechazadas]       = useState("—");

  useEffect(() => {
    const mesActual = new Date().getMonth();
    const anioActual = new Date().getFullYear();

    getSolicitudesPorEstado("PROCESADO")
      .then(data => { const n = toCount(data); setNotificaciones(n); setPorAprobar(n); })
      .catch(() => { setNotificaciones(0); setPorAprobar(0); });

    getSolicitudesPorEstado("APROBADA")
      .then(data => {
        const lista = Array.isArray(data) ? data : (data?.content ?? []);
        const delMes = lista.filter(s => {
          const f = new Date(s.actualizadoEl ?? s.creadoEl ?? "");
          return f.getMonth() === mesActual && f.getFullYear() === anioActual;
        });
        setAprobadas(delMes.length);
      })
      .catch(() => setAprobadas(0));

    getSolicitudesPorEstado("RECHAZADA")
      .then(data => {
        const lista = Array.isArray(data) ? data : (data?.content ?? []);
        const delMes = lista.filter(s => {
          const f = new Date(s.actualizadoEl ?? s.creadoEl ?? "");
          return f.getMonth() === mesActual && f.getFullYear() === anioActual;
        });
        setRechazadas(delMes.length);
      })
      .catch(() => setRechazadas(0));

    listarEmpleados(0, 1)
      .then(data => {
        const total = data?.totalElements ?? (Array.isArray(data) ? data.length : "—");
        setTotalEmpleados(total);
      })
      .catch(() => setTotalEmpleados("—"));
  }, []);

  return (
    <div className="dashboard">
      <Sidebar rol="GERENCIA" />

      <main className="main-content">
        <div className="emp-topbar">
          <div className="emp-topbar-brand">
            <div className="emp-topbar-plus">+</div>
            <span className="emp-topbar-text">HOSPITAL SAN GABRIEL</span>
          </div>
          <div className="emp-topbar-right">
            <Link to="/gerencia/solicitudes" className="emp-topbar-notif" title="Ver notificaciones" aria-label="Ver notificaciones">
              Notificaciones
              {notificaciones > 0 && <span className="emp-notif-dot">{notificaciones}</span>}
            </Link>
            <span className="emp-topbar-username">{displayName}</span>
            <span className="role-pill role-pill-gerencia">Gerencia</span>
            <div className="emp-topbar-avatar">👤</div>
          </div>
        </div>

        <header className="main-header header-gerencia">
          <div className="header-left">
            <span className="page-breadcrumb">Panel Ejecutivo</span>
            <h1>Bienvenido, Gerencia</h1>
          </div>
          <span className="role-pill role-pill-gerencia">Gestión Ejecutiva</span>
        </header>

        <div className="stats-row">
          <div className="stat-box accent-purple">
            <div className="stat-icon">👥</div>
            <div className="stat-info">
              <div className="stat-value">{totalEmpleados}</div>
              <div className="stat-label">Total Empleados</div>
            </div>
          </div>
          <div className="stat-box accent-orange">
            <div className="stat-icon">⏳</div>
            <div className="stat-info">
              <div className="stat-value">{porAprobar}</div>
              <div className="stat-label">Por Aprobar</div>
            </div>
          </div>
          <div className="stat-box accent-green">
            <div className="stat-icon">✅</div>
            <div className="stat-info">
              <div className="stat-value">{aprobadas}</div>
              <div className="stat-label">Aprobadas (Mes)</div>
            </div>
          </div>
          <div className="stat-box accent-red">
            <div className="stat-icon">❌</div>
            <div className="stat-info">
              <div className="stat-value">{rechazadas}</div>
              <div className="stat-label">Rechazadas (Mes)</div>
            </div>
          </div>
        </div>

        <div className="cards-grid">
          <Link to="/gerencia/solicitudes" className="card">
            <span className="card-icon">📅</span>
            <h3>Solicitudes Pendientes</h3>
            <p>Aprobar o rechazar solicitudes de permisos y vacaciones</p>
          </Link>
        </div>

      </main>
    </div>
  );
}
